package koolkrafter5.questrep.reputation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

public class ClientReputationCache {

    private static final Map<String, Integer> reputations = new HashMap<>();

    public static void syncAllFactions(NBTTagCompound tag) {
        reputations.clear();
        if (tag == null) return;
        for (String key : tag.func_150296_c()) {
            reputations.put(key, tag.getInteger(key));
        }
    }

    public static void updateFaction(String faction, int reputation) {
        reputations.put(faction, reputation);
    }

    public static int getReputation(String faction) {
        return reputations.getOrDefault(faction, FactionData.getDefaultReputation(faction));
    }

    public static Map<String, Integer> getAllReputations() {
        return Collections.unmodifiableMap(reputations);
    }

    public static void setReputation(String faction, int value) {
        reputations.put(faction, value);
    }

    public static void removeReputation(String faction) {
        reputations.remove(faction);
    }

    public static void clear() {
        reputations.clear();
    }

    public static void addReputation(String faction, int amount) {
        reputations.put(faction, reputations.getOrDefault(faction, 0) + amount);
    }
}
