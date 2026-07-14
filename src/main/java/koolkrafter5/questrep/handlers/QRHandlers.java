package koolkrafter5.questrep.handlers;

import java.util.UUID;

import koolkrafter5.questrep.network.DelayedSyncHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.stats.StatList;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import koolkrafter5.questrep.deaths.DeathData;
import koolkrafter5.questrep.reputation.ReputationData;
import koolkrafter5.questrep.tasks.TaskDeaths;

public class QRHandlers {

    /**
     * Initialize death number for players who join for the first time after the mod is added.
     */
    @SubscribeEvent
    public void onPlayerJoin(EntityJoinWorldEvent event) {
        if (!(event.entity instanceof EntityPlayer player)) return;

        if (player.worldObj.isRemote) return;

        EntityPlayerMP playerMP = (EntityPlayerMP) player;
        ReputationData.get()
            .updateReputationTasks(player);
        UUID id = player.getUniqueID();

        if (DeathData.get()
            .containsPlayer(id)) {
            return;
        }
        int deathCount = playerMP.func_147099_x()
            .writeStat(StatList.deathsStat); // Yes, writeStat is a reader.
        DeathData.get()
            .setDeaths(id, deathCount);
        DelayedSyncHandler.queueSync(player.getUniqueID());
    }

    /**
     * Increment death count, change reputation, update death tasks.
     */
    @SubscribeEvent
    public void onLivingDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (event.isCanceled()) return;

        if (!(event.entity instanceof EntityPlayer player)) return;

        if (player.worldObj.isRemote) return;

        UUID uuid = player.getUniqueID();
        DeathData.get()
            .addDeath(uuid);
        ReputationData.get()
            .deathChange(player);
        updateDeathTasks(player);
    }

    private void updateDeathTasks(EntityPlayer player) {
        ParticipantInfo pInfo = new ParticipantInfo(player);
        for (DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB)
            .bulkLookup(pInfo.getSharedQuests())) {
            for (DBEntry<ITask> task : (entry.getValue()).getTasks()
                .getEntries()) {
                if (task.getValue() instanceof TaskDeaths) {
                    task.getValue()
                        .detect(pInfo, entry);
                }
            }
        }
    }

}
