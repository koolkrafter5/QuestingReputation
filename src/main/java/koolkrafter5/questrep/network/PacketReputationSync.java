package koolkrafter5.questrep.network;

import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import koolkrafter5.questrep.reputation.ClientReputationCache;

public class PacketReputationSync implements IMessage {

    private NBTTagCompound data;

    public PacketReputationSync() {}

    public PacketReputationSync(NBTTagCompound data) {
        this.data = data;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, data);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        data = ByteBufUtils.readTag(buf);
    }

    public static class Handler implements IMessageHandler<PacketReputationSync, IMessage> {

        @Override
        public IMessage onMessage(PacketReputationSync message, MessageContext ctx) {
            ClientReputationCache.syncAllFactions(message.data);
            return null;
        }
    }
}
