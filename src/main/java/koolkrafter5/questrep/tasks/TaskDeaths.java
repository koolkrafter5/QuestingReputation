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
import koolkrafter5.questrep.client.gui.editors.tasks.GuiEditTaskDeaths;
import koolkrafter5.questrep.client.gui.tasks.PanelTaskDeaths;
import koolkrafter5.questrep.deaths.DeathData;
import koolkrafter5.questrep.tasks.factory.FactoryTaskDeaths;

public class TaskDeaths extends TaskProgressableBase<int[]> {

    private final Set<UUID> completeUsers = new TreeSet<>();
    public int target = 1;
    public int progress = 0;

    @Override
    public String getUnlocalisedName() {
        return "questrep.gui.deaths.name";
    }

    @Override
    public void detect(ParticipantInfo participant, Map.Entry<UUID, IQuest> quest) {
        UUID playerID = QuestingAPI.getQuestingUUID(participant.PLAYER);
        if (isComplete(playerID)) return;

        progress = getProgress(participant);
        if (progress >= target) {
            setComplete(participant.ALL_UUIDS);
            participant.markDirtyParty(quest.getKey());
        }
    }

    public int getProgress(ParticipantInfo participant) {
        return DeathData.get()
            .getDeaths(participant.ALL_UUIDS);
    }

    @Override
    public ResourceLocation getFactoryID() {
        return FactoryTaskDeaths.INSTANCE.getRegistryName();
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
        for (UUID id : uuid) {
            setComplete(id);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IGuiPanel getTaskGui(IGuiRect rect, Map.Entry<UUID, IQuest> quest) {
        return new PanelTaskDeaths(rect, this);
    }

    @Override
    public NBTTagCompound writeProgressToNBT(NBTTagCompound nbt, @Nullable List<UUID> users) {
        NBTTagList userTag = new NBTTagList();

        if (users != null) {
            users.forEach(
                (uuid) -> { if (completeUsers.contains(uuid)) userTag.appendTag(new NBTTagString(uuid.toString())); });
        } else {
            completeUsers.forEach((uuid) -> userTag.appendTag(new NBTTagString(uuid.toString())));
        }
        nbt.setTag("completeUsers", userTag);
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
    public int[] getUsersProgress(UUID uuid) {
        return new int[] { DeathData.get()
            .getDeaths(uuid) };
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
    public GuiScreen getTaskEditor(GuiScreen parent, Map.Entry<UUID, IQuest> quest) {
        return new GuiEditTaskDeaths(parent, quest, this);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound json) {
        json.setInteger("target", target);
        json.setInteger("progress", progress);
        return json;
    }

    @Override
    public void readFromNBT(NBTTagCompound json) {
        target = json.getInteger("target");
        progress = json.getInteger("progress");
    }
}
