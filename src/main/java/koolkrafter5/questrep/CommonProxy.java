package koolkrafter5.questrep;

import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import koolkrafter5.questrep.reputation.FactionData;
import koolkrafter5.questrep.reputation.ReputationData;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {}

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {}

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {}

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}

    /**
     * Get the player's (party's) reputation level for every faction. No defaults are generated if the player (or
     * party) has no reputation with a given faction.
     */
    public Map<String, Integer> getAllReputations(EntityPlayer player) {
        return ReputationData.get()
            .getAllReputations(player);
    }

    /**
     * Get the player's (party's) reputation level for every faction. No defaults are generated if the player (or
     * party) has no reputation with a given faction.
     */
    public Map<String, Integer> getAllReputations(UUID uuid) {
        return ReputationData.get()
            .getAllReputations(uuid);
    }

    /**
     * Gets the player's (party's) reputation level for the given faction. Defaults to 0 if the player (or party)
     * has no reputation with that faction.
     *
     * @param player  The player to check.
     * @param faction The reputation faction to query.
     */
    public int getReputation(EntityPlayer player, String faction) {
        return ReputationData.get()
            .getReputation(player, faction);
    }

    /**
     * Gets the player's (party's) reputation level for the given faction. Defaults to 0 if the player (or party)
     * has no reputation with that faction.
     *
     * @param uuid    The UUID of the player to check.
     * @param faction The reputation faction to query.
     */
    public int getReputation(UUID uuid, String faction) {
        return ReputationData.get()
            .getReputation(uuid, faction);
    }

    public void addReputation(EntityPlayer player, String faction, int amount) {
        ReputationData.get()
            .addReputation(player, faction, amount);
    }

    /**
     * Handles reputation changes from death for the given player.
     */
    public synchronized void deathChange(EntityPlayer player) {
        for (String faction : FactionData.getAllFactions()) {
            int deathChange = FactionData.getDeathChange(faction);
            if (deathChange != 0) addReputation(player, faction, deathChange);
        }
    }
}
