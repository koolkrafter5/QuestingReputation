package koolkrafter5.questrep.rewards.factory;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.registry.IFactoryData;
import koolkrafter5.questrep.rewards.RewardReputation;

public class FactoryRewardReputation implements IFactoryData<IReward, NBTTagCompound> {

    public static final FactoryRewardReputation INSTANCE = new FactoryRewardReputation();

    public FactoryRewardReputation() {}

    public ResourceLocation getRegistryName() {
        return new ResourceLocation("questrep", "reputation");
    }

    public RewardReputation createNew() {
        return new RewardReputation();
    }

    public RewardReputation loadFromData(NBTTagCompound json) {
        RewardReputation reward = new RewardReputation();
        reward.readFromNBT(json);
        return reward;
    }
}
