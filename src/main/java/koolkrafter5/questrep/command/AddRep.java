package koolkrafter5.questrep.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;

import betterquesting.api.questing.party.IParty;
import betterquesting.api2.storage.DBEntry;
import betterquesting.questing.party.PartyManager;
import koolkrafter5.questrep.reputation.FactionData;
import koolkrafter5.questrep.reputation.ReputationData;

public class AddRep extends CommandBase {

    @Override
    public String getCommandName() {
        return "addRep";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "/addRep";
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
        if (p_71515_1_ instanceof EntityPlayer) {
            EntityPlayerMP playerMP = (EntityPlayerMP) p_71515_1_;

            DBEntry<IParty> party = PartyManager.INSTANCE.getParty(playerMP.getUniqueID());
            if (party == null) {
                p_71515_1_.addChatMessage(new ChatComponentTranslation("Party is null, adding to player rep"));
            } else {
                p_71515_1_
                    .addChatMessage(new ChatComponentTranslation("Party ID, adding to party rep: " + party.getID()));
            }
            ReputationData.get()
                .addReputation(playerMP.getUniqueID(), "knights", 5);
            p_71515_1_.addChatMessage(
                new ChatComponentTranslation(
                    "Current rep: " + ReputationData.get()
                        .getReputation(playerMP.getUniqueID(), "knights")));
            p_71515_1_.addChatMessage(
                new ChatComponentTranslation(
                    "Current standing: " + FactionData.getTierName(
                        "knights",
                        ReputationData.get()
                            .getReputation(playerMP.getUniqueID(), "knights"))));
        } else p_71515_1_.addChatMessage(new ChatComponentTranslation("gui.oreDiscovery.notPlayer"));
    }
}
