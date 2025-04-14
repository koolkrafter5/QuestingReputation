package koolkrafter5.questrep.tasks.factory;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import koolkrafter5.questrep.QuestingReputation;
import koolkrafter5.questrep.tasks.TaskReputation;

public class FactoryTaskReputation implements IFactoryData<ITask, NBTTagCompound> {

    public static final FactoryTaskReputation INSTANCE = new FactoryTaskReputation();

    @Override
    public TaskReputation loadFromData(NBTTagCompound json) {
        TaskReputation task = new TaskReputation();
        task.readFromNBT(json);
        return task;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(QuestingReputation.MODID + ":reputation");
    }

    @Override
    public TaskReputation createNew() {
        return new TaskReputation();
    }
}
