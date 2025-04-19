package koolkrafter5.questrep.deaths;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

public class DeathData extends WorldSavedData {

    private static final String DATA_NAME = "DeathData";

    private final Map<UUID, Integer> deaths = new HashMap<>();

    /**
     * Needed for saving/loading to work properly
     */
    public DeathData() {
        super("");
    }

    public DeathData(String dataName) {
        super(dataName);
    }

    /**
     * Gets the number of deaths for the player with the given UUID.
     */
    public synchronized int getDeaths(UUID id) {
        return deaths.getOrDefault(id, 0);
    }

    /**
     * Gets the total number of deaths for all given UUIDs.
     */
    public synchronized int getDeaths(List<UUID> list) {
        int out = 0;
        for (UUID id : list) {
            out += getDeaths(id);
        }
        return out;
    }

    /**
     * Adds one death for the given UUID.
     * Other mods probably shouldn't call this, or it will desync with the stats page.
     */
    public synchronized void addDeath(UUID id) {
        setDeaths(id, getDeaths(id) + 1);
    }

    /**
     * Sets the deaths for the given UUID to the given count.
     * Other mods probably shouldn't call this, or it will desync with the stats page.
     */
    public synchronized void setDeaths(UUID id, int count) {
        deaths.put(id, count);
        markDirty();
    }

    /**
     * Returns true if DeathData has an entry for that UUID.
     */
    public boolean containsPlayer(UUID id) {
        return deaths.containsKey(id);
    }

    @Override
    public synchronized void readFromNBT(NBTTagCompound nbt) {
        NBTTagList playerList = nbt.getTagList("Deaths", 10);
        for (int i = 0; i < playerList.tagCount(); i++) {
            NBTTagCompound tag = playerList.getCompoundTagAt(i);
            UUID uuid = UUID.fromString(tag.getString("UUID"));
            int deathCount = tag.getInteger("deaths");
            deaths.put(uuid, deathCount);
        }
    }

    @Override
    public synchronized void writeToNBT(NBTTagCompound nbt) {
        NBTTagList playerList = new NBTTagList();
        for (Map.Entry<UUID, Integer> entry : deaths.entrySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString(
                "UUID",
                entry.getKey()
                    .toString());
            tag.setInteger("deaths", entry.getValue());
            playerList.appendTag(tag);
        }
        nbt.setTag("Deaths", playerList);
    }

    public static DeathData get() {
        MapStorage storage = MinecraftServer.getServer()
            .worldServerForDimension(0).mapStorage;
        DeathData data = (DeathData) storage.loadData(DeathData.class, DeathData.DATA_NAME);
        if (data == null) {
            data = new DeathData(DATA_NAME);
            data.setDirty(true);
            storage.setData(DATA_NAME, data);
        }
        return data;
    }
}
