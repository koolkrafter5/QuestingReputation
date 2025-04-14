package koolkrafter5.questrep.tasks;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsFile;
import net.minecraft.util.ResourceLocation;

import org.apache.logging.log4j.Level;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import koolkrafter5.questrep.QuestingReputation;
import koolkrafter5.questrep.client.gui.editors.GuiEditTaskDeaths;
import koolkrafter5.questrep.client.gui.tasks.PanelTaskDeaths;
import koolkrafter5.questrep.tasks.factory.FactoryTaskDeaths;

public class TaskDeaths implements ITask {

    private final Set<UUID> completeUsers = new TreeSet<>();
    public int target = 1;
    public int progress = 0;

    @Override
    public String getUnlocalisedName() {
        return "questrep.gui.deaths.name";
    }

    @Override
    public void detect(ParticipantInfo participant, DBEntry<IQuest> quest) {
        UUID playerID = QuestingAPI.getQuestingUUID(participant.PLAYER);
        if (isComplete(playerID)) return;

        progress = 0;
        System.out.println("Active players: " + participant.ACTIVE_PLAYERS);
        for (EntityPlayer player : participant.ACTIVE_PLAYERS) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            StatisticsFile file = playerMP.func_147099_x();
            progress += file.writeStat(StatList.deathsStat); // WHY IS THE READER METHOD CALLED WRITESTAT???????????
        }

        if (progress >= target) {
            for (UUID player : participant.ACTIVE_UUIDS) {
                setComplete(player);
            }
        }
    }

    public void setTarget(int target) {
        this.target = target;
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

    @SideOnly(Side.CLIENT)
    public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return new PanelTaskDeaths(rect, this);
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
    public void resetUser(@Nullable UUID uuid) {
        if (uuid == null) {
            completeUsers.clear();
        } else {
            completeUsers.remove(uuid);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getTaskEditor(GuiScreen parent, DBEntry<IQuest> quest) {
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
