package koolkrafter5.questrep.reputation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.util.StatCollector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import betterquesting.api.utils.BigItemStack;
import koolkrafter5.questrep.QuestingReputation;

public class FactionData {

    public static final List<ReputationTier> UNKNOWN_TIERS = Collections.singletonList(ReputationTier.UNKNOWN);

    private static final Map<String, Faction> factions = new LinkedHashMap<>();

    private static final String configPath = "config/questingreputation/factions.json";

    public static void loadFactions() {
        File configFile = new File(configPath);

        if (!configFile.exists()) {
            createDefaultConfig(configFile);
        }

        Gson gson = new GsonBuilder().registerTypeAdapter(BigItemStack.class, new BigItemStackAdapter())
            .create();

        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            Type type = new TypeToken<Map<String, Faction>>() {}.getType();
            getFactions().clear();
            getFactions().putAll(gson.fromJson(br, type));
        } catch (Exception e) {
            QuestingReputation.log.error("Failed to load faction reputation config: {}", e);
        }
    }

    private static void setDisplayName(String factionID, String displayName) {
        getFactions().get(factionID).name = displayName;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void createDefaultConfig(File configFile) {
        File parentDir = configFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        JsonObject root = new JsonObject();

        JsonObject knights = new JsonObject();
        knights.addProperty("name", "Order of the Knights");
        knights.addProperty("item", "minecraft:iron_sword");
        knights.addProperty("deathChange", -10);
        JsonArray knightTiers = new JsonArray();

        knightTiers.add(createTier("Hated", -100));
        knightTiers.add(createTier("Disliked", -50));
        knightTiers.add(createTier("Neutral", 0));
        knightTiers.add(createTier("Friendly", 50));
        knightTiers.add(createTier("Honored", 100));
        knightTiers.add(createTier("Exalted", 200));

        knights.add("tiers", knightTiers);
        root.add("knights", knights);

        JsonObject mages = new JsonObject();
        JsonArray mageTiers = new JsonArray();

        mageTiers.add(createTier("Outsider", -50));
        mageTiers.add(createTier("Unproven", 0));
        mageTiers.add(createTier("Initiate", 25));
        mageTiers.add(createTier("Adept", 50));
        mageTiers.add(createTier("Master", 100));

        mages.addProperty("name", "Mages' Guild");
        mages.addProperty("item", "minecraft:stick");
        mages.addProperty("defaultReputation", -50);
        mages.add("tiers", mageTiers);
        root.add("mages", mages);

        try (Writer writer = new FileWriter(configFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting()
                .create();
            gson.toJson(root, writer);
            QuestingReputation.log.info("Created default faction config at {}", configFile.getPath());
        } catch (IOException e) {
            QuestingReputation.log.error("Failed to write default config: {}", e);
        }
    }

    /**
     * Gets the value of the tier the given reputation is part of for the given faction.
     * A reputation value is part of a tier if it is greater than the tier value (if above zero), below the
     * tier value (if below zero), or neutral if equal to zero.
     */
    public static ReputationTier getTier(String faction, int rep) {
        if (rep == Integer.MAX_VALUE) {
            return ReputationTier.MAX_INT;
        } else if (rep == Integer.MIN_VALUE) {
            return ReputationTier.MIN_INT;
        }
        List<ReputationTier> tiers = getTiers(faction);
        if (tiers == null || tiers.isEmpty()) return ReputationTier.UNKNOWN;

        tiers.sort(Comparator.comparingInt(ReputationTier::value));

        if (rep == 0) {
            for (ReputationTier tier : tiers) {
                if (tier.value() == 0) {
                    return tier;
                }
            }
        } else if (rep > 0) {
            ReputationTier best = null;
            for (ReputationTier tier : tiers) {
                if (tier.value() <= rep) {
                    best = tier;
                } else {
                    break;
                }
            }
            return best != null ? best : ReputationTier.UNKNOWN;
        } else {
            for (ReputationTier tier : tiers) {
                if (tier.value() >= rep) {
                    return tier;
                }
            }
        }

        return ReputationTier.UNKNOWN;
    }

    private static JsonObject createTier(String name, int value) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", name);
        obj.addProperty("value", value);
        return obj;
    }

    /**
     * Gets the name of the tier the given reputation is part of for the given faction.
     * A reputation value is part of a tier if it is greater than the tier value (if above zero), below the
     * tier value (if below zero), or neutral if equal to zero.
     * Translation keys are automatically translated to local.
     */
    public static String getTierName(String faction, int rep) {
        return StatCollector.translateToLocal(getTier(faction, rep).name());
    }

    /**
     * Gets the name of the tier the given reputation is part of for the given faction.
     * A reputation value is part of a tier if it is greater than the tier value (if above zero), below the
     * tier value (if below zero), or neutral if equal to zero.
     */
    public static int getTierValue(String faction, int rep) {
        return getTier(faction, rep).value();
    }

    /**
     * Gets a list of the ReputationTiers for the given faction.
     *
     * @param faction The faction to query.
     * @return A List of ReputationTiers.
     */
    public static List<ReputationTier> getTiers(String faction) {
        Faction f = getFactions().get(faction);
        return f != null ? f.tiers : UNKNOWN_TIERS;
    }

    /**
     * Adds all tiers to the given faction.
     *
     * @param faction The faction to set tiers for.
     * @param tiers   A list of ReputationTiers to add.
     */
    public static void setAllTiers(String faction, List<ReputationTier> tiers) {
        getFactions().computeIfAbsent(faction, Faction::new);
        clearTiers(faction);
        getFactions().get(faction).tiers.addAll(tiers);
        getFactions().get(faction).tiers.sort(Comparator.comparingInt(ReputationTier::value));
    }

    private static synchronized Map<String, Faction> getFactions() {
        return factions;
    }

    /**
     * Clears tiers for every faction.
     */
    public static void clearAllTiers() {
        for (String faction : getFactions().keySet()) {
            clearTiers(faction);
        }
    }

    /**
     * Clears set tiers for the given faction.
     *
     * @param faction The faction's tiers to clear
     */
    public static void clearTiers(String faction) {
        if (faction != null && getFactions().containsKey(faction)) {
            getFactions().get(faction).tiers.clear();
        }
    }

    /**
     * Returns the display name for the given faction.
     * Translation keys are automatically translated to local.
     */
    public static String getDisplayName(String faction) {
        if (!getFactions().containsKey(faction)) {
            QuestingReputation.log.warn("Tried to getDisplayName for nonexistent faction {}", faction);
            return Faction.UNKNOWN.name;
        }
        return StatCollector.translateToLocal(getFactions().get(faction).name);
    }

    /**
     * Returns a set that contains every faction ID.
     */
    public static Set<String> getAllFactions() {
        return getFactions().keySet();
    }

    /**
     * Returns the value that reputation will change by for each death for the given faction.
     */
    public static int getDeathChange(String faction) {
        if (!getFactions().containsKey(faction)) {
            QuestingReputation.log.warn("Tried to getDeathChange for nonexistent faction {}", faction);
            return Faction.UNKNOWN.deathChange;
        }
        return getFactions().get(faction).deathChange;
    }

    /**
     * Set the value that reputation will change by for each death to the given value for the given faction.
     */
    public static void setDeathChange(String faction, int value) {
        if (!getFactions().containsKey(faction)) {
            QuestingReputation.log.warn("Tried to setDeathChange for nonexistent faction {}", faction);
            return;
        }
        getFactions().get(faction).deathChange = value;
    }

    public static Integer getDefaultReputation(String faction) {
        if (!getFactions().containsKey(faction)) {
            QuestingReputation.log.warn("Tried to getDefaultReputation for nonexistent faction {}", faction);
            return Faction.UNKNOWN.defaultReputation;
        }
        return getFactions().get(faction).defaultReputation;
    }

    public static void setDefaultReputation(String faction, int value) {
        if (!getFactions().containsKey(faction)) {
            QuestingReputation.log.warn("Tried to setDefaultReputation for nonexistent faction {}", faction);
            return;
        }
        getFactions().get(faction).defaultReputation = value;
    }

    public static String getDefaultFaction() {
        Set<String> factions = getAllFactions();
        if (factions.isEmpty()) {
            return Faction.UNKNOWN.name;
        }
        return (String) factions.toArray()[0];
    }

    /**
     * Returns the item stack to display for the given faction in quests.
     */
    public static BigItemStack getRepresentativeStack(String faction) {
        if (!getFactions().containsKey(faction)) {
            QuestingReputation.log.warn("Tried to getRepresentativeStack for nonexistent faction {}", faction);
            return Faction.UNKNOWN.item;
        }
        return getFactions().get(faction).item;
    }

    /**
     * Sets the item stack to display for the given faction in quests.
     */
    public static void setRepresentativeStack(String faction, BigItemStack value) {
        if (!getFactions().containsKey(faction)) {
            QuestingReputation.log.warn("Tried to setRepresentativeStack for nonexistent faction {}", faction);
            return;
        } else if (value == null) {
            QuestingReputation.log.warn("Tried to setRepresentativeStack with null itemstack for faction {}", faction);
            return;
        }
        getFactions().get(faction).item = value;
    }
}
