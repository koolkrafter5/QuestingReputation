package koolkrafter5.questrep.network;

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("questrep");
    private static int packetId = 0;

    public static void init() {
        register(PacketReputationSync.class, PacketReputationSync.Handler.class, Side.CLIENT);
        register(PacketClaimReputationReward.class, PacketClaimReputationReward.Handler.class, Side.CLIENT);
    }

    private static <REQ extends IMessage, REPLY extends IMessage> void register(Class<REQ> messageClass,
        Class<? extends cpw.mods.fml.common.network.simpleimpl.IMessageHandler<REQ, REPLY>> handlerClass, Side side) {

        INSTANCE.registerMessage(handlerClass, messageClass, packetId++, side);
    }

    // Convenience method
    public static void sendToPlayer(IMessage msg, EntityPlayerMP player) {
        INSTANCE.sendTo(msg, player);
    }
}
