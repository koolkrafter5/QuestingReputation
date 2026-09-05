package koolkrafter5.questrep.importers.hqm;

import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.utils.BQThreadedIO;
import bq_standard.importers.hqm.HQMQuestImporter;
import bq_standard.importers.hqm.converters.HQMRep;
import koolkrafter5.questrep.QuestingReputation;
import koolkrafter5.questrep.reputation.FactionData;
import koolkrafter5.questrep.reputation.ReputationTier;
import koolkrafter5.questrep.rewards.RewardReputation;
import koolkrafter5.questrep.tasks.TaskDeaths;
import koolkrafter5.questrep.tasks.TaskReputation;

public class QuestRepHQMQuestImporter extends HQMQuestImporter {

    public static final HQMQuestImporter INSTANCE = new QuestRepHQMQuestImporter();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
        .create();

    @Override
    public String getUnlocalisedName() {
        return "questrep.importer.hqm_quest.name";
    }

    @Override
    public String getUnlocalisedDescription() {
        return "questrep.importer.hqm_quest.desc";
    }

    @Override
    public void loadFiles(IQuestDatabase questDB, IQuestLineDatabase lineDB, File[] files) {
        super.loadFiles(questDB, lineDB, files);
        for (File f : files) {
            if (f == null || !f.exists()
                || !f.getName()
                    .equalsIgnoreCase("reputations.json"))
                continue;

            JsonArray json = readArrayFromFile(f);
            convertFactions(json);
        }
    }

    public static ITask[] convertReputationTask(JsonObject json) {
        List<ITask> tasks = new ArrayList<>();

        for (JsonElement je : JsonHelper.GetArray(json, "reputation")) {
            if (!(je instanceof JsonObject)) continue;
            JsonObject jRep = je.getAsJsonObject();

            String repId;
            JsonElement jid = jRep.get("reputation");
            if (jid == null || !jid.isJsonPrimitive()) continue;
            if (jid.getAsJsonPrimitive()
                .isString()) {
                repId = jid.getAsString();
            } else {
                repId = jid.getAsNumber()
                    .toString();
            }

            HQMRep repObj = INSTANCE.reputations.get(repId);
            if (repObj == null) continue;

            TaskReputation task = new TaskReputation();
            task.faction = repObj.rName;
            if (jRep.has("lower")) task.lowerBound = repObj.getMarker(
                jRep.get("lower")
                    .getAsInt());
            if (jRep.has("upper")) task.upperBound = repObj.getMarker(
                jRep.get("upper")
                    .getAsInt());
            task.invert = JsonHelper.GetBoolean(jRep, "inverted", false);

            tasks.add(task);
        }

        return tasks.toArray(new ITask[0]);
    }

    public static ITask[] convertDeathTask(JsonObject json) {
        List<ITask> tasks = new ArrayList<>();

        if (json.has("deaths")) {
            TaskDeaths task = new TaskDeaths();
            task.target = json.get("deaths")
                .getAsInt();
            tasks.add(task);
        }

        return tasks.toArray(new ITask[0]);
    }

    public static IReward[] convertReputationReward(JsonElement json) {
        if (!(json instanceof JsonArray)) return null;
        List<IReward> rList = new ArrayList<>();

        for (JsonElement je : json.getAsJsonArray()) {
            if (!(je instanceof JsonObject)) continue;
            JsonObject jRep = je.getAsJsonObject();

            JsonElement jid = jRep.get("reputation");
            if (jid == null || !jid.isJsonPrimitive()) continue;

            String repId;
            if (jid.getAsJsonPrimitive()
                .isString()) {
                repId = JsonHelper.GetString(jRep, "reputation", "");
            } else {
                repId = JsonHelper.GetNumber(jRep, "reputation", 0)
                    .toString();
            }

            HQMRep repObj = INSTANCE.reputations.get(repId);
            if (repObj == null) continue;

            RewardReputation reward = new RewardReputation();
            reward.faction = repObj.rName;
            reward.amount = JsonHelper.GetNumber(jRep, "value", 1)
                .intValue();
            rList.add(reward);
        }

        return rList.toArray(new IReward[0]);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(String name) throws ReflectiveOperationException {
        Field field = HQMQuestImporter.class.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(null);
    }

    static {
        try {
            HashMap<String, Function<JsonObject, ITask[]>> taskConverters = getField("taskConverters");
            taskConverters.put("REPUTATION", QuestRepHQMQuestImporter::convertReputationTask);
            taskConverters.put("DEATH", QuestRepHQMQuestImporter::convertDeathTask);
            HashMap<String, Function<JsonElement, IReward[]>> rewardConverters = getField("rewardConverters");
            rewardConverters.put("reputationrewards", QuestRepHQMQuestImporter::convertReputationReward);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    // ugh ugly copypasta from super
    private static JsonArray readArrayFromFile(File file) {
        Future<JsonArray> task = BQThreadedIO.INSTANCE.enqueue(() -> {
            if (file == null || !file.exists()) {
                return new JsonArray();
            }

            try (InputStreamReader fr = new InputStreamReader(
                Files.newInputStream(file.toPath()),
                StandardCharsets.UTF_8)) {
                return GSON.fromJson(fr, JsonArray.class);
            } catch (Exception e) {
                QuestingReputation.LOG.log(Level.ERROR, "An error occured while loading JSON from file:", e);

                int i = 0;
                File bkup = new File(file.getParent(), "malformed_" + file.getName() + i + ".json");

                while (bkup.exists()) {
                    i++;
                    bkup = new File(file.getParent(), "malformed_" + file.getName() + i + ".json");
                }

                QuestingReputation.LOG.log(Level.ERROR, "Creating backup at: " + bkup.getAbsolutePath());
                JsonHelper.CopyPaste(file, bkup);

                return new JsonArray(); // Just a safety measure against NPEs
            }
        });

        try {
            return task.get(); // Wait for other scheduled file ops to finish
        } catch (Exception e) {
            QuestingReputation.LOG.error("Unable to read from file {}", file, e);
            return new JsonArray();
        }
    }

    private void convertFactions(JsonArray jsonRoot) {
        if (jsonRoot == null || jsonRoot.size() <= 0) return;

        int i = 0;

        FactionData.clearAllFactions();
        for (JsonElement e : jsonRoot) {
            if (!(e instanceof JsonObject jRep)) continue;

            String faction = "Reputation(" + i + ")";
            i++;
            if (jRep.has("Name")) faction = JsonHelper.GetString(jRep, "Name", faction);
            if (jRep.has("name")) faction = JsonHelper.GetString(jRep, "name", faction);

            List<ReputationTier> tiers = new ArrayList<>();
            JsonElement neutral;
            if (jRep.has("Neutral")) {
                neutral = jRep.get("Neutral");
            } else {
                neutral = jRep.get("neutral");
            }
            if (neutral instanceof JsonObject mark) {
                tiers.add(convertMarkToTier(mark));
            }
            JsonArray markers = null;
            if (jRep.has("Markers")) markers = JsonHelper.GetArray(jRep, "Markers");
            if (markers == null) markers = JsonHelper.GetArray(jRep, "markers");

            for (JsonElement e2 : markers) {
                if (!(e2 instanceof JsonObject)) continue;

                JsonObject mark = e2.getAsJsonObject();

                tiers.add(convertMarkToTier(mark));
            }

            FactionData.setAllTiers(faction, tiers);
        }
        FactionData.writeToConfig();
    }

    @Nonnull
    private static ReputationTier convertMarkToTier(JsonObject mark) {
        ReputationTier tier;
        int value = 0;
        String name = "Unknown Tier";
        if (mark.has("Value")) value = JsonHelper.GetNumber(mark, "Value", value)
            .intValue();
        if (mark.has("value")) value = JsonHelper.GetNumber(mark, "value", value)
            .intValue();

        if (mark.has("Name")) name = JsonHelper.GetString(mark, "Name", name);
        if (mark.has("name")) name = JsonHelper.GetString(mark, "name", name);
        tier = new ReputationTier(value, name);
        return tier;
    }

}
