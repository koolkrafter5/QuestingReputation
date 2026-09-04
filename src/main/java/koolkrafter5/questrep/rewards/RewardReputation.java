package koolkrafter5.questrep.rewards;

import java.util.Map;
import java.util.UUID;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import koolkrafter5.questrep.QuestingReputation;
import koolkrafter5.questrep.client.gui.editors.GuiEditReputationReward;
import koolkrafter5.questrep.client.gui.rewards.PanelRewardReputation;
import koolkrafter5.questrep.reputation.FactionData;
import koolkrafter5.questrep.rewards.factory.FactoryRewardReputation;

public class RewardReputation implements IReward {

    public String faction = FactionData.getDefaultFaction();
    public int amount = 0;

    public RewardReputation() {}

    public ResourceLocation getFactoryID() {
        return FactoryRewardReputation.INSTANCE.getRegistryName();
    }

    public String getUnlocalisedName() {
        return "questrep.reward.reputation";
    }

    public boolean canClaim(EntityPlayer player, Map.Entry<UUID, IQuest> quest) {
        return true;
    }

    public void claimReward(EntityPlayer player, Map.Entry<UUID, IQuest> quest) {
        QuestingReputation.proxy.addReputation(player, faction, amount);
    }

    public void readFromNBT(NBTTagCompound json) {
        this.faction = json.getString("faction");
        this.amount = json.getInteger("amount");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound json) {
        json.setString("faction", this.faction);
        json.setInteger("amount", this.amount);
        return json;
    }

    @SideOnly(Side.CLIENT)
    public IGuiPanel getRewardGui(IGuiRect rect, Map.Entry<UUID, IQuest> quest) {
        return new PanelRewardReputation(rect, this);
    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getRewardEditor(GuiScreen screen, Map.Entry<UUID, IQuest> quest) {
        return new GuiEditReputationReward(screen, quest, this);
    }
}
