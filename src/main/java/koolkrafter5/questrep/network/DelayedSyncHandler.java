package koolkrafter5.questrep.network;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import koolkrafter5.questrep.reputation.ReputationData;

public class DelayedSyncHandler {

    private static final Set<UUID> pendingSync = new HashSet<>();

    public static void queueSync(UUID uuid) {
        pendingSync.add(uuid);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (!pendingSync.isEmpty()) {
            List<EntityPlayerMP> players = MinecraftServer.getServer()
                .getConfigurationManager().playerEntityList;
            for (EntityPlayerMP player : players) {
                if (pendingSync.contains(player.getUniqueID())) {
                    ReputationData.get()
                        .syncTo(player);
                }
            }
            pendingSync.clear();
        }
    }
}
