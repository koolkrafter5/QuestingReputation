package koolkrafter5.questrep.rewards;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import koolkrafter5.questrep.client.gui.editors.GuiEditReputationReward;
import koolkrafter5.questrep.client.gui.rewards.PanelRewardReputation;
import koolkrafter5.questrep.network.PacketClaimReputationReward;
import koolkrafter5.questrep.network.PacketHandler;
import koolkrafter5.questrep.reputation.FactionData;
import koolkrafter5.questrep.reputation.ReputationData;
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

    public boolean canClaim(EntityPlayer player, DBEntry<IQuest> quest) {
        return true;
    }

    public void claimReward(EntityPlayer player, DBEntry<IQuest> quest) {
        if (player.worldObj.isRemote) {
            // Client side — send packet to server
            PacketHandler.INSTANCE.sendToServer(new PacketClaimReputationReward(player.getUniqueID(), faction, amount));
        } else {
            ReputationData.get()
                .addReputation(player, faction, amount);
        }
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
    public IGuiPanel getRewardGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return new PanelRewardReputation(rect, this);
    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getRewardEditor(GuiScreen screen, DBEntry<IQuest> quest) {
        return new GuiEditReputationReward(screen, quest, this);
    }
}
