package koolkrafter5.questrep.network;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

import koolkrafter5.questrep.QRConfig;

public class ClientReputationCache {

    private static final Map<String, Integer> reputationMap = new HashMap<>();

    public static void updateFromNBT(NBTTagCompound tag) {
        reputationMap.clear();
        if (tag == null) return;
        NBTTagCompound rep = tag.getCompoundTag("PlayerReputation");
        for (String key : rep.func_150296_c()) {
            reputationMap.put(key, rep.getInteger(key));
        }
    }

    public static int getReputation(String faction) {
        return reputationMap.getOrDefault(faction, QRConfig.defaultReputation);
    }
}
