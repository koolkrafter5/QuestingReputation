package koolkrafter5.questrep.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsFile;
import net.minecraft.util.ChatComponentTranslation;

public class SeeJson extends CommandBase {

    @Override
    public String getCommandName() {
        return "seeJson";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "/seeJson";
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
        if (p_71515_1_ instanceof EntityPlayer) {
            EntityPlayerMP playerMP = (EntityPlayerMP) p_71515_1_;
            StatisticsFile file = playerMP.func_147099_x();
            p_71515_1_.addChatMessage(new ChatComponentTranslation("Stats file: " + file.toString()));
            p_71515_1_.addChatMessage(new ChatComponentTranslation("Deaths: " + file.writeStat(StatList.deathsStat)));
        } else p_71515_1_.addChatMessage(new ChatComponentTranslation("gui.oreDiscovery.notPlayer"));
    }
}
