package koolkrafter5.questrep.reputation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.questing.party.PartyManager;
import koolkrafter5.questrep.network.PacketHandler;
import koolkrafter5.questrep.network.PacketReputationSync;
import koolkrafter5.questrep.network.PacketReputationUpdate;
import koolkrafter5.questrep.tasks.TaskReputation;

public class ReputationData extends WorldSavedData {

    private static final String DATA_NAME = "QuestingReputation";

    public final Map<UUID, Map<String, Integer>> playerReputation = new HashMap<>();
    public final Map<Integer, Map<String, Integer>> partyReputation = new HashMap<>();

    @SuppressWarnings("unused") // Needed for saving/loading to work properly due to WorldSavedData reflection
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
    public Map<String, Integer> getAllReputations(EntityPlayer player) {
        return getAllReputations(player.getUniqueID());
    }

    /**
     * Get the player's (party's) reputation level for every faction. No defaults are generated if the player (or
     * party) has no reputation with a given faction.
     */
    public Map<String, Integer> getAllReputations(UUID uuid) {
        return getProperReputationMap(uuid);
    }

    /**
     * Gets the player's (party's) reputation level for the given faction. Defaults to 0 if the player (or party)
     * has no reputation with that faction.
     *
     * @param player  The player to check.
     * @param faction The reputation faction to query.
     */
    public int getReputation(EntityPlayer player, String faction) {
        return getReputation(player.getUniqueID(), faction);
    }

    /**
     * Gets the player's (party's) reputation level for the given faction. Defaults to 0 if the player (or party)
     * has no reputation with that faction.
     *
     * @param uuid    The UUID of the player to check.
     * @param faction The reputation faction to query.
     */
    public int getReputation(UUID uuid, String faction) {
        return getProperReputationMap(uuid).getOrDefault(faction, FactionData.getDefaultReputation(faction));
    }

    /**
     * Gets the proper reputation map for the given player's UUID.
     * If a party has just been formed, it copies the owner's reputation to the party's.
     * TODO: This CAN be cheesed by having high rep, forming a new party, tanking the reputation, leaving the party,
     * TODO: and forming a new one. I need to figure out some way to prevent that.
     *
     * @param player The player to check.
     * @return the player's party's reputation map if in a party, or the player's individual reputation map if not.
     */
    private Map<String, Integer> getProperReputationMap(EntityPlayer player) {
        UUID id = player.getUniqueID();
        return getProperReputationMap(id);
    }

    private synchronized Map<String, Integer> getProperReputationMap(UUID id) {
        DBEntry<IParty> party = PartyManager.INSTANCE.getParty(id);
        if (party != null) {
            int pID = party.getID();
            if (!partyReputation.containsKey(pID)) {
                for (UUID member : party.getValue()
                    .getMembers()) {
                    if (party.getValue()
                        .getStatus(member) == EnumPartyStatus.OWNER) {
                        if (!playerReputation.containsKey(id)) {
                            playerReputation.put(id, new HashMap<>());
                            markDirty();
                        }
                        partyReputation.put(pID, new HashMap<>(playerReputation.get(member)));
                        break;
                    }
                }
            }
            return partyReputation.get(party.getID());
        }
        if (!playerReputation.containsKey(id)) {
            playerReputation.put(id, new HashMap<>());
            markDirty();
        }
        return playerReputation.get(id);
    }

    /**
     * Adds to the player's (party's) reputation level for the given faction.
     *
     * @param player  The player to add reputation for.
     * @param faction The faction's reputation to add to.
     * @param amount  The amount to add to the player's reputation. Can be made negative to subtract.
     */
    public synchronized void addReputation(EntityPlayer player, String faction, int amount) {
        if (amount == 0) {
            return;
        }
        Map<String, Integer> map = getProperReputationMap(player);
        map.put(faction, map.getOrDefault(faction, 0) + amount);
        updateReputationTasks(player);
        markDirty();
        syncFactionToParty(player, faction);
    }

    public void syncAllFactions(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP playerMP)) {
            return;
        }

        Map<String, Integer> rep = getProperReputationMap(player);

        NBTTagCompound tag = new NBTTagCompound();

        for (Map.Entry<String, Integer> entry : rep.entrySet()) {
            tag.setInteger(entry.getKey(), entry.getValue());
        }

        PacketHandler.INSTANCE.sendTo(new PacketReputationSync(tag), playerMP);
    }

    private void syncFactionToParty(EntityPlayer player, String faction) {
        DBEntry<IParty> party = PartyManager.INSTANCE.getParty(player.getUniqueID());

        if (party == null) {
            syncFaction(player, faction);
            return;
        }

        for (UUID member : party.getValue()
            .getMembers()) {
            EntityPlayer memberPlayer = MinecraftServer.getServer()
                .getConfigurationManager()
                .func_152612_a(member.toString());

            if (memberPlayer != null) {
                syncFaction(memberPlayer, faction);
            }
        }
    }

    public void syncFaction(EntityPlayer player, String faction) {
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }

        PacketHandler
            .sendToPlayer(new PacketReputationUpdate(faction, getReputation(player, faction)), (EntityPlayerMP) player);
    }

    /**
     * Sets the player's (party's) reputation level for the given faction.
     *
     * @param player  The player to set reputation for.
     * @param faction The faction's reputation to add to.
     * @param amount  The amount to set the player's reputation to.
     */
    public synchronized void setReputation(EntityPlayer player, String faction, int amount) {
        Map<String, Integer> map = getProperReputationMap(player);
        map.put(faction, amount);
        updateReputationTasks(player);
        syncFactionToParty(player, faction);
        markDirty();
    }

    public void updateReputationTasks(EntityPlayer player) {
        ParticipantInfo pInfo = new ParticipantInfo(player);
        for (Map.Entry<UUID, IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB)
            .filterKeys(pInfo.getSharedQuests())
            .entrySet()) {
            for (DBEntry<ITask> task : (entry.getValue()).getTasks()
                .getEntries()) {
                if (task.getValue() instanceof TaskReputation) {
                    task.getValue()
                        .detect(pInfo, entry);
                }
            }
        }
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
        NBTTagList playerList = new NBTTagList();
        for (Map.Entry<UUID, Map<String, Integer>> entry : playerReputation.entrySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString(
                "UUID",
                entry.getKey()
                    .toString());

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
