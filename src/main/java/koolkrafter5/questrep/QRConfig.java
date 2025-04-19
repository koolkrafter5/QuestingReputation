package koolkrafter5.questrep;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class QRConfig {

    public static int defaultDeathChange = 0;
    public static Integer defaultReputation = 0;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);
        defaultDeathChange = configuration.getInt(
            "defaultDeathChange",
            Configuration.CATEGORY_GENERAL,
            defaultDeathChange,
            Integer.MIN_VALUE,
            Integer.MAX_VALUE,
            "Set the default death loss for factions that do not have the \"deathChange\" property set in config/questingreputation/factions.json. -10 means a loss of 10 rep each death, while 10 means you gain 10 rep each death.");

        defaultReputation = configuration.getInt(
            "defaultReputation",
            Configuration.CATEGORY_GENERAL,
            defaultReputation,
            Integer.MIN_VALUE,
            Integer.MAX_VALUE,
            "Set the default reputation for players with factions that do not have the \"defaultReputation\" property set in config/questingreputation/factions.json.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
