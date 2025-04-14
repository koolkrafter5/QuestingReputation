package koolkrafter5.questrep.tasks;

import java.util.List;
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
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import koolkrafter5.questrep.QuestingReputation;
import koolkrafter5.questrep.client.gui.editors.GuiEditTaskReputation;
import koolkrafter5.questrep.client.gui.tasks.PanelTaskReputation;
import koolkrafter5.questrep.tasks.factory.FactoryTaskReputation;

public class TaskReputation implements ITask {

    private final Set<UUID> completeUsers = new TreeSet<>();
    public String faction = "none";
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
    public void detect(ParticipantInfo participant, DBEntry<IQuest> dbEntry) {
        UUID playerID = QuestingAPI.getQuestingUUID(participant.PLAYER);
        if (isComplete(playerID)) return;

    }

    @Override
    public boolean isComplete(UUID uuid) {
        return completeUsers.contains(uuid);
    }

    @Override
    public void setComplete(UUID uuid) {
        completeUsers.add(uuid);
    }

    @Override
    public void resetUser(@Nullable UUID uuid) {
        if (uuid == null) {
            completeUsers.clear();
        } else {
            completeUsers.remove(uuid);
        }
    }

    @SideOnly(Side.CLIENT)
    public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return new PanelTaskReputation(rect, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getTaskEditor(GuiScreen parent, DBEntry<IQuest> quest) {
        return new GuiEditTaskReputation(parent, quest, this);
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
}
