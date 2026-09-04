package koolkrafter5.questrep.handlers;

import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.stats.StatList;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import koolkrafter5.questrep.QuestingReputation;
import koolkrafter5.questrep.deaths.DeathData;
import koolkrafter5.questrep.reputation.ReputationData;
import koolkrafter5.questrep.tasks.TaskDeaths;

@SuppressWarnings("unused")
public class QREventHandlers {

    /**
     * Update death number on server if missing and sync reputations to player.
     */
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP player) || player.worldObj.isRemote) return;

        ReputationData.get()
            .updateReputationTasks(player);
        UUID id = player.getUniqueID();

        if (!DeathData.get()
            .containsPlayer(id)) {
            int deathCount = player.func_147099_x()
                .writeStat(StatList.deathsStat); // Yes, writeStat is what reads the stat.
            DeathData.get()
                .setDeaths(id, deathCount);
        }

        ReputationData.get()
            .syncAllFactions(player);
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
        QuestingReputation.proxy.deathChange(player);
        updateDeathTasks(player);
    }

    private void updateDeathTasks(EntityPlayer player) {
        ParticipantInfo pInfo = new ParticipantInfo(player);
        for (Map.Entry<UUID, IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB)
            .filterKeys(pInfo.getSharedQuests())
            .entrySet()) {
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
