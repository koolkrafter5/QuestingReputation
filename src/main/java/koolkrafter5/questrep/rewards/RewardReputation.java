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
import koolkrafter5.questrep.client.gui.rewards.PanelRewardReputation;
import koolkrafter5.questrep.rewards.factory.FactoryRewardReputation;

public class RewardReputation implements IReward {

    public String faction = "Reputation";
    public String type = "dummy";
    public boolean relative = true;
    public int value = 1;

    public RewardReputation() {}

    public ResourceLocation getFactoryID() {
        return FactoryRewardReputation.INSTANCE.getRegistryName();
    }

    public String getUnlocalisedName() {
        return "bq_standard.reward.scoreboard";
    }

    public boolean canClaim(EntityPlayer player, DBEntry<IQuest> quest) {
        return true;
    }

    public void claimReward(EntityPlayer player, DBEntry<IQuest> quest) {

    }

    public void readFromNBT(NBTTagCompound json) {
        this.faction = json.getString("faction");
        this.type = json.getString("type");
        this.value = json.getInteger("value");
        this.relative = json.getBoolean("relative");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound json) {
        json.setString("faction", this.faction);
        json.setString("type", "dummy");
        json.setInteger("value", this.value);
        json.setBoolean("relative", this.relative);
        return json;
    }

    @SideOnly(Side.CLIENT)
    public IGuiPanel getRewardGui(IGuiRect rect, DBEntry<IQuest> quest) {
        return new PanelRewardReputation(rect, this);
    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getRewardEditor(GuiScreen screen, DBEntry<IQuest> quest) {
        return null;
    }
}
