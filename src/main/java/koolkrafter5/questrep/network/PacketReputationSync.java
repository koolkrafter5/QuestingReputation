package koolkrafter5.questrep.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import koolkrafter5.questrep.QuestingReputation;

public class PacketReputationSync implements IMessage {

    private NBTTagCompound data;

    public PacketReputationSync() {}

    public PacketReputationSync(NBTTagCompound data) {
        this.data = data;
    }

    public void init() {}

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(data, baos);
            byte[] bytes = baos.toByteArray();
            buf.writeInt(bytes.length);
            buf.writeBytes(bytes);
        } catch (IOException e) {
            QuestingReputation.log.error(e);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            int length = buf.readInt();
            byte[] bytes = new byte[length];
            buf.readBytes(bytes);
            data = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            QuestingReputation.log.error(e);
        }
    }

    public static class Handler implements IMessageHandler<PacketReputationSync, IMessage> {

        @Override
        public IMessage onMessage(PacketReputationSync message, MessageContext ctx) {
            ClientReputationCache.updateFromNBT(message.data);
            return null;
        }
    }
}
