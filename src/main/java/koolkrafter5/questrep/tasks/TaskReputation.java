package koolkrafter5.questrep.tasks;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.apache.logging.log4j.Level;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.utils.ParticipantInfo;
import bq_standard.tasks.base.TaskProgressableBase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import koolkrafter5.questrep.QuestingReputation;
import koolkrafter5.questrep.client.gui.editors.tasks.GuiEditTaskReputation;
import koolkrafter5.questrep.client.gui.tasks.PanelTaskReputation;
import koolkrafter5.questrep.reputation.FactionData;
import koolkrafter5.questrep.reputation.ReputationData;
import koolkrafter5.questrep.reputation.ReputationTier;
import koolkrafter5.questrep.tasks.factory.FactoryTaskReputation;

public class TaskReputation extends TaskProgressableBase<int[]> {

    private final Set<UUID> completeUsers = new TreeSet<>();
    public String faction = FactionData.getDefaultFaction();
    public int lowerBound = Integer.MIN_VALUE;
    public int upperBound = Integer.MAX_VALUE;
    public boolean invert = false;

    @Override
    public String getUnlocalisedName() {
        return "questrep.gui.reputation.name";
    }

    @Override
    public ResourceLocation getFactoryID() {
        return FactoryTaskReputation.INSTANCE.getRegistryName();
    }

    @Override
    public void detect(ParticipantInfo participant, Map.Entry<UUID, IQuest> quest) {
        UUID playerID = QuestingAPI.getQuestingUUID(participant.PLAYER);
        if (isComplete(playerID)) return;

        int reputation = ReputationData.get()
            .getReputation(participant.PLAYER, faction);
        if (checkReputation(reputation)) {
            setComplete(participant.ALL_UUIDS);
            participant.markDirtyParty(quest.getKey());
        }
    }

    public boolean checkReputation(int reputation) {
        return !invert && (lowerBound <= reputation && reputation <= upperBound)
            || invert && (lowerBound > reputation || reputation > upperBound);
    }

    @Override
    public boolean isComplete(UUID uuid) {
        return completeUsers.contains(uuid);
    }

    @Override
    public void setComplete(UUID uuid) {
        completeUsers.add(uuid);
    }

    public void setComplete(List<UUID> uuid) {
        completeUsers.addAll(uuid);
    }

    @Override
    public int[] getUsersProgress(UUID uuid) {
        return new int[] { ReputationData.get()
            .getReputation(uuid, faction) };
    }

    @Override
    public void resetUser(@Nullable UUID uuid) {
        if (uuid == null) {
            completeUsers.clear();
        } else {
            completeUsers.remove(uuid);
        }
    }

    @Override
    public int[] readUserProgressFromNBT(NBTTagCompound nbt) {
        return new int[] { nbt.getInteger("data") };
    }

    @Override
    public void writeUserProgressToNBT(NBTTagCompound nbt, int[] progress) {
        nbt.setInteger("data", progress[0]);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IGuiPanel getTaskGui(IGuiRect rect, Map.Entry<UUID, IQuest> quest) {
        return new PanelTaskReputation(rect, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getTaskEditor(GuiScreen parent, Map.Entry<UUID, IQuest> quest) {
        return new GuiEditTaskReputation(parent, quest, this);
    }

    public void targetText(StringBuilder sb) {
        if (lowerBound == upperBound) {
            sb.append(
                invert ? tr("questrep.task.target.not", lowerBound) : tr("questrep.task.target.exact", lowerBound));
        } else if (lowerBound > upperBound) {
            sb.append(StatCollector.translateToLocal("questrep.task.target.invalid"));
        } else if (lowerBound == Integer.MIN_VALUE) {
            if (upperBound == Integer.MAX_VALUE) {
                sb.append(StatCollector.translateToLocal("questrep.task.target.unconfigured"));
            } else {
                sb.append(
                    invert ? tr("questrep.task.target.over", upperBound)
                        : tr("questrep.task.target.at_most", upperBound));
            }
        } else if (upperBound == Integer.MAX_VALUE) {
            sb.append(
                invert ? tr("questrep.task.target.under", lowerBound)
                    : tr("questrep.task.target.at_least", lowerBound));
        } else if (invert) {
            sb.append(tr("questrep.task.target.under", lowerBound));
            sb.append("\n");
            sb.append(tr("questrep.task.target.or_over", upperBound));
        } else {
            sb.append(tr("questrep.task.target.at_least", lowerBound));
            sb.append("\n");
            sb.append(tr("questrep.task.target.and_at_most", upperBound));
        }
        sb.append("\n");
    }

    private static String tr(String key, int arg) {
        return StatCollector.translateToLocalFormatted(key, arg);
    }

    /**
     * Formats the value in the form Tiername (Value) if it matches a tier. Otherwise, it just returns the value as a
     * String.
     */
    public String formatRepAsTier(int value) {
        List<ReputationTier> tiers = FactionData.getTiers(faction);
        if (tiers != null) {
            for (ReputationTier tier : tiers) {
                if (tier.value() == value) {
                    return tier.name() + " (" + value + ")";
                }
            }
        }
        return Integer.toString(value);
    }

    @Override
    public NBTTagCompound writeProgressToNBT(NBTTagCompound nbt, @Nullable List<UUID> users) {
        NBTTagList jArray = new NBTTagList();

        if (users != null) {
            users.forEach(
                (uuid) -> { if (completeUsers.contains(uuid)) jArray.appendTag(new NBTTagString(uuid.toString())); });
        } else {
            completeUsers.forEach((uuid) -> jArray.appendTag(new NBTTagString(uuid.toString())));
        }
        nbt.setTag("completeUsers", jArray);
        return nbt;
    }

    @Override
    public void readProgressFromNBT(NBTTagCompound nbt, boolean merge) {
        if (!merge) {
            completeUsers.clear();
        }

        NBTTagList cList = nbt.getTagList("completeUsers", 8);
        for (int i = 0; i < cList.tagCount(); i++) {
            try {
                completeUsers.add(UUID.fromString(cList.getStringTagAt(i)));
            } catch (Exception e) {
                QuestingReputation.log.log(Level.ERROR, "Unable to load UUID for task", e);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound json) {
        json.setString("reputation", faction);
        json.setInteger("lowerBound", lowerBound);
        json.setInteger("upperBound", upperBound);
        json.setBoolean("invert", invert);
        return json;
    }

    @Override
    public void readFromNBT(NBTTagCompound json) {
        faction = json.getString("reputation");
        lowerBound = json.getInteger("lowerBound");
        upperBound = json.getInteger("upperBound");
        invert = json.getBoolean("invert");
    }

    public String targetText() {
        StringBuilder sb = new StringBuilder();
        targetText(sb);
        return sb.toString();
    }
}
