package koolkrafter5.questrep.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import koolkrafter5.questrep.reputation.ClientReputationCache;

public class PacketReputationUpdate implements IMessage {

    String faction;
    int reputation;

    public PacketReputationUpdate() {}

    public PacketReputationUpdate(String faction, int reputation) {
        this.faction = faction;
        this.reputation = reputation;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(reputation);
        ByteBufUtils.writeUTF8String(buf, faction);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        reputation = buf.readInt();
        faction = ByteBufUtils.readUTF8String(buf);
    }

    public static class Handler implements IMessageHandler<PacketReputationUpdate, IMessage> {

        @Override
        public IMessage onMessage(PacketReputationUpdate message, MessageContext ctx) {
            ClientReputationCache.updateFaction(message.faction, message.reputation);
            return null;
        }
    }
}
