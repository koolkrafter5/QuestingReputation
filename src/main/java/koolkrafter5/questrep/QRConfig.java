package koolkrafter5.questrep;

import com.gtnewhorizon.gtnhlib.config.Config;

@Config(modid = "questrep")
public class QRConfig {

    @Config.Comment("The default death loss for factions that do not have the \"deathChange\" property set in config/betterquesting/factions.json.\n-10 means a loss of 10 rep each death, while 10 means you gain 10 rep each death.\"")
    @Config.DefaultInt(0)
    public static int defaultDeathChange;

    @Config.Comment("Set the default reputation for players with factions that do not have the \"defaultReputation\" property set in config/betterquesting/factions.json.")
    @Config.DefaultInt(0)
    public static int defaultReputation;
}
