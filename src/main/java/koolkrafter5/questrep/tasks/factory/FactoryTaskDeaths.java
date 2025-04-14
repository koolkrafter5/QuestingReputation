package koolkrafter5.questrep.tasks.factory;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import koolkrafter5.questrep.QuestingReputation;
import koolkrafter5.questrep.tasks.TaskDeaths;

public class FactoryTaskDeaths implements IFactoryData<ITask, NBTTagCompound> {

    public static final FactoryTaskDeaths INSTANCE = new FactoryTaskDeaths();

    @Override
    public TaskDeaths loadFromData(NBTTagCompound json) {
        TaskDeaths task = new TaskDeaths();
        task.readFromNBT(json);
        return task;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(QuestingReputation.MODID + ":deaths");
    }

    @Override
    public TaskDeaths createNew() {
        return new TaskDeaths();
    }
}
