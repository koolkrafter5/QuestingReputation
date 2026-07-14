package koolkrafter5.questrep.network;

import java.io.IOException;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import koolkrafter5.questrep.reputation.ReputationData;

public class PacketClaimReputationReward implements IMessage {

    private UUID uuid;
    private String faction;
    private int amount;

    public PacketClaimReputationReward() {}

    public PacketClaimReputationReward(UUID uuid, String faction, int amount) {
        this.uuid = uuid;
        this.faction = faction;
        this.amount = amount;
    }

    public void init() {}

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeLong(uuid.getMostSignificantBits());
        pb.writeLong(uuid.getLeastSignificantBits());
        try {
            pb.writeStringToBuffer(faction);
        } catch (IOException ignored) {}
        pb.writeInt(amount);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        uuid = new UUID(pb.readLong(), pb.readLong());
        try {
            faction = pb.readStringFromBuffer(32767);
        } catch (IOException e) {
            faction = "unknown";
        }
        amount = pb.readInt();
    }

    public static class Handler implements IMessageHandler<PacketClaimReputationReward, IMessage> {

        @Override
        public IMessage onMessage(PacketClaimReputationReward message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player.getUniqueID()
                .equals(message.uuid)) {
                ReputationData.get()
                    .addReputation(player, message.faction, message.amount);
            }
            return null;
        }
    }
}
