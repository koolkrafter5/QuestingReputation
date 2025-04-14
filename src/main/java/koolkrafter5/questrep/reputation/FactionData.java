package koolkrafter5.questrep.reputation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.util.StatCollector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import koolkrafter5.questrep.QuestingReputation;

public class FactionData {

    private static final Map<String, List<ReputationTier>> tiers = new HashMap<>();
    private static final Map<String, String> names = new HashMap<>();
    private static final String configPath = "config/questingreputation/factions.json";

    public static void loadFactions() {
        File configFile = new File(configPath);

        if (!configFile.exists()) {
            createDefaultConfig(configFile);
            return;
        }

        try (Reader reader = new FileReader(configFile)) {
            JsonObject json = new JsonParser().parse(reader)
                .getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                String factionName = entry.getKey();
                JsonObject factionObject = entry.getValue()
                    .getAsJsonObject();
                JsonArray tiersArray = factionObject.getAsJsonArray("tiers");

                String displayName = factionObject.has("name") ? factionObject.get("name")
                    .getAsString() : factionName;
                names.put(factionName, displayName);

                List<ReputationTier> tierList = new ArrayList<>();

                for (JsonElement element : tiersArray) {
                    JsonObject tierObj = element.getAsJsonObject();
                    ReputationTier tier = new ReputationTier();
                    tier.name = tierObj.get("name")
                        .getAsString();
                    tier.value = tierObj.get("value")
                        .getAsInt();
                    tierList.add(tier);
                }

                tierList.sort(Comparator.comparingInt(t -> t.value));
                tiers.put(factionName, tierList);
            }

        } catch (Exception e) {
            QuestingReputation.log.error("Failed to load faction reputation config: {}", e);
        }
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

        mages.add("tiers", mageTiers);
        mages.addProperty("name", "Mages' Guild");
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

        tiers.sort(Comparator.comparingInt(t -> t.value));

        if (rep == 0) {
            for (ReputationTier tier : tiers) {
                if (tier.value == 0) {
                    return tier;
                }
            }
        } else if (rep > 0) {
            ReputationTier best = null;
            for (ReputationTier tier : tiers) {
                if (tier.value <= rep) {
                    best = tier;
                } else {
                    break;
                }
            }
            return best != null ? best : ReputationTier.UNKNOWN;
        } else {
            for (ReputationTier tier : tiers) {
                if (tier.value >= rep) {
                    return tier;
                }
            }
        }

        return ReputationTier.UNKNOWN;
    }

    /**
     * Gets the name of the tier the given reputation is part of for the given faction.
     * A reputation value is part of a tier if it is greater than the tier value (if above zero), below the
     * tier value (if below zero), or neutral if equal to zero.
     */
    public static String getTierName(String faction, int rep) {
        return getTier(faction, rep).name;
    }

    /**
     * Gets the name of the tier the given reputation is part of for the given faction.
     * A reputation value is part of a tier if it is greater than the tier value (if above zero), below the
     * tier value (if below zero), or neutral if equal to zero.
     */
    public static int getTierValue(String faction, int rep) {
        return getTier(faction, rep).value;
    }

    private static JsonObject createTier(String name, int value) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", name);
        obj.addProperty("value", value);
        return obj;
    }

    /**
     * Gets a list of the ReputationTiers for the given faction.
     *
     * @param faction The faction to query.
     * @return A List of ReputationTiers.
     */
    public static synchronized List<ReputationTier> getTiers(String faction) {
        return tiers.get(faction);
    }

    /**
     * Adds all tiers to the given faction.
     *
     * @param faction The faction to set tiers for.
     * @param tiers   A list of ReputationTiers to add.
     */
    public static synchronized void setAllTiers(String faction, List<ReputationTier> tiers) {
        if (!FactionData.tiers.containsKey(faction)) {
            FactionData.tiers.put(faction, new ArrayList<>());
        }
        clearTiers(faction);
        FactionData.tiers.get(faction)
            .addAll(tiers);
        FactionData.tiers.get(faction)
            .sort(Comparator.comparingInt(t -> t.value));
    }

    /**
     * Clears tiers for every faction.
     */
    public static void clearAllTiers() {
        for (String faction : tiers.keySet()) {
            clearTiers(faction);
        }
    }

    /**
     * Clears set tiers for the given faction.
     *
     * @param faction The faction's tiers to clear
     */
    public static void clearTiers(String faction) {
        if (faction != null) {
            tiers.get(faction)
                .clear();
        }
    }

    /**
     * Gets the display name for the given faction (automatically translates translation keys!)
     */
    public static String getDisplayName(String factionId) {
        return StatCollector.translateToLocalFormatted(names.getOrDefault(factionId, factionId));
    }

    public static Set<String> getAllFactions() {
        return tiers.keySet();
    }

}
