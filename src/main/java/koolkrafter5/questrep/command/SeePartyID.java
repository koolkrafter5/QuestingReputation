package koolkrafter5.questrep.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;

import betterquesting.api.questing.party.IParty;
import betterquesting.api2.storage.DBEntry;
import betterquesting.questing.party.PartyManager;

public class SeePartyID extends CommandBase {

    @Override
    public String getCommandName() {
        return "seePartyID";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "/seePartyID";
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
        if (p_71515_1_ instanceof EntityPlayer) {
            EntityPlayerMP playerMP = (EntityPlayerMP) p_71515_1_;

            DBEntry<IParty> party = PartyManager.INSTANCE.getParty(playerMP.getUniqueID());
            if (party == null) {
                p_71515_1_.addChatMessage(new ChatComponentTranslation("Party is null!"));
                return;
            }
            p_71515_1_.addChatMessage(new ChatComponentTranslation("Party ID: " + party.getID()));
            p_71515_1_.addChatMessage(new ChatComponentTranslation("Party: " + party));
        } else p_71515_1_.addChatMessage(new ChatComponentTranslation("gui.oreDiscovery.notPlayer"));
    }
}
