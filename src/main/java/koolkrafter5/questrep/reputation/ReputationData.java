package koolkrafter5.questrep.reputation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

import betterquesting.api.questing.party.IParty;
import betterquesting.api2.storage.DBEntry;
import betterquesting.questing.party.PartyManager;

public class ReputationData extends WorldSavedData {

    public static final String DATA_NAME = "QuestingReputation";

    public Map<UUID, Map<String, Integer>> playerReputation = new HashMap<>();
    public Map<Integer, Map<String, Integer>> partyReputation = new HashMap<>();

    /**
     * Needed for saving/loading to work properly
     */
    public ReputationData() {
        super("");
    }

    public ReputationData(String dataName) {
        super(dataName);
    }

    /**
     * Get the player's (party's) reputation level for every faction. No defaults are generated if the player (or
     * party) has no reputation with a given faction.
     */
    public synchronized Map<String, Integer> getAllReputations(UUID id) {
        return getProperReputationMap(id);
    }

    /**
     * Gets the player's (party's) reputation level for the given faction. Defaults to 0 if the player (or party)
     * has no reputation with that faction.
     *
     * @param id      The player's UUID
     * @param faction The reputation faction to query.
     */
    public synchronized int getReputation(UUID id, String faction) {
        return getAllReputations(id).getOrDefault(faction, 0);
    }

    /**
     * Gets the proper reputation map for the given player's UUID.
     *
     * @param id The player's UUID.
     * @return the player's party's reputation map if in a party, or the player's individual reputation map if not.
     */
    private Map<String, Integer> getProperReputationMap(UUID id) {
        DBEntry<IParty> party = PartyManager.INSTANCE.getParty(id);
        if (party != null) {
            int pID = party.getID();
            if (!partyReputation.containsKey(pID)) {
                partyReputation.put(pID, new HashMap<>());
            }
            return partyReputation.get(party.getID());
        }
        if (!playerReputation.containsKey(id)) {
            playerReputation.put(id, new HashMap<>());
        }
        return playerReputation.get(id);
    }

    /**
     * Adds to the player's (party's) reputation level for the given faction.
     *
     * @param id      The player's UUID
     * @param faction The faction's reputation to add to.
     * @param amount  The amount to add to the player's reputation. Can be made negative to subtract.
     */
    public synchronized void addReputation(UUID id, String faction, int amount) {
        if (amount == 0) {
            return;
        }
        Map<String, Integer> map = getAllReputations(id);
        map.put(faction, getReputation(id, faction) + amount);
        markDirty();
    }

    @Override
    public synchronized void readFromNBT(NBTTagCompound nbt) {
        deserializePlayers(nbt);
        deserializeParties(nbt);
    }

    private void deserializePlayers(NBTTagCompound nbt) {
        NBTTagList playerList = nbt.getTagList("PlayerReputation", 10);
        for (int i = 0; i < playerList.tagCount(); i++) {
            NBTTagCompound tag = playerList.getCompoundTagAt(i);
            UUID uuid = UUID.fromString(tag.getString("UUID"));
            NBTTagCompound repTag = tag.getCompoundTag("Reputations");
            Map<String, Integer> reps = new HashMap<>();
            for (String key : repTag.func_150296_c()) {
                reps.put(key, repTag.getInteger(key));
            }
            playerReputation.put(uuid, reps);
        }
    }

    private void deserializeParties(NBTTagCompound nbt) {
        NBTTagList playerList = nbt.getTagList("PartyReputation", 10);
        for (int i = 0; i < playerList.tagCount(); i++) {
            NBTTagCompound tag = playerList.getCompoundTagAt(i);
            int id = tag.getInteger("ID");
            NBTTagCompound repTag = tag.getCompoundTag("Reputations");
            Map<String, Integer> reps = new HashMap<>();
            for (String key : repTag.func_150296_c()) {
                reps.put(key, repTag.getInteger(key));
            }
            partyReputation.put(id, reps);
        }
    }

    @Override
    public synchronized void writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("PlayerReputation", serializePlayers());
        nbt.setTag("PartyReputation", serializeParties());
    }

    private NBTTagList serializePlayers() {
        NBTTagList playerList1 = new NBTTagList();
        for (Map.Entry<UUID, Map<String, Integer>> entry1 : playerReputation.entrySet()) {
            NBTTagCompound tag1 = new NBTTagCompound();
            tag1.setString(
                "UUID",
                entry1.getKey()
                    .toString());

            NBTTagCompound repTag1 = new NBTTagCompound();
            for (Map.Entry<String, Integer> rep1 : entry1.getValue()
                .entrySet()) {
                repTag1.setInteger(rep1.getKey(), rep1.getValue());
            }

            tag1.setTag("Reputations", repTag1);
            playerList1.appendTag(tag1);
        }
        return playerList1;
    }

    private NBTTagList serializeParties() {
        NBTTagList playerList = new NBTTagList();
        for (Map.Entry<Integer, Map<String, Integer>> entry : partyReputation.entrySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("ID", entry.getKey());

            NBTTagCompound repTag = new NBTTagCompound();
            for (Map.Entry<String, Integer> rep : entry.getValue()
                .entrySet()) {
                repTag.setInteger(rep.getKey(), rep.getValue());
            }

            tag.setTag("Reputations", repTag);
            playerList.appendTag(tag);
        }
        return playerList;
    }

    public static ReputationData get() {
        MapStorage storage = MinecraftServer.getServer()
            .worldServerForDimension(0).mapStorage;
        ReputationData data = (ReputationData) storage.loadData(ReputationData.class, ReputationData.DATA_NAME);
        if (data == null) {
            data = new ReputationData(DATA_NAME);
            data.setDirty(true);
            storage.setData(DATA_NAME, data);
        }
        return data;
    }
}
