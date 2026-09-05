package koolkrafter5.questrep;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.importers.IImportRegistry;
import betterquesting.api.client.importers.IImporter;
import bq_standard.importers.hqm.HQMQuestImporter;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import koolkrafter5.questrep.importers.hqm.QuestRepHQMQuestImporter;
import koolkrafter5.questrep.reputation.ClientReputationCache;

public class ClientProxy extends CommonProxy {

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        IImportRegistry api = QuestingAPI.getAPI(ApiReference.IMPORT_REG);
        List<IImporter> importers = api.getImporters();
        for (int i = 0; i < importers.size(); i++) {
            if (importers.get(i) instanceof HQMQuestImporter) {
                importers.remove(i);
                break;
            }
        }
        api.registerImporter(QuestRepHQMQuestImporter.INSTANCE);
    }

    /**
     * Get the player's (party's) reputation level for every faction. No defaults are generated if the player (or
     * party) has no reputation with a given faction.
     */
    @Override
    public Map<String, Integer> getAllReputations(EntityPlayer player) {
        return ClientReputationCache.getAllReputations();
    }

    /**
     * Get the player's (party's) reputation level for every faction. No defaults are generated if the player (or
     * party) has no reputation with a given faction.
     */
    @Override
    public Map<String, Integer> getAllReputations(UUID uuid) {
        return ClientReputationCache.getAllReputations();
    }

    /**
     * Gets the player's (party's) reputation level for the given faction. Defaults to 0 if the player (or party)
     * has no reputation with that faction.
     *
     * @param player  The player to check.
     * @param faction The reputation faction to query.
     */
    @Override
    public int getReputation(EntityPlayer player, String faction) {
        return ClientReputationCache.getReputation(faction);
    }

    /**
     * Gets the player's (party's) reputation level for the given faction. Defaults to 0 if the player (or party)
     * has no reputation with that faction.
     *
     * @param uuid    The UUID of the player to check.
     * @param faction The reputation faction to query.
     */
    @Override
    public int getReputation(UUID uuid, String faction) {
        return ClientReputationCache.getReputation(faction);
    }

    @Override
    public void addReputation(EntityPlayer player, String faction, int amount) {
        ClientReputationCache.addReputation(faction, amount);
    }
}
