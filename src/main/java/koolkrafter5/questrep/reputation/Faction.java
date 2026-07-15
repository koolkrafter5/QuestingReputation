package koolkrafter5.questrep.reputation;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Items;

import betterquesting.api.utils.BigItemStack;
import koolkrafter5.questrep.QRConfig;

public class Faction {

    public String name;
    public int deathChange = QRConfig.defaultDeathChange;
    public int defaultReputation = QRConfig.defaultReputation;
    public BigItemStack item = new BigItemStack(Items.nether_star);
    public final List<ReputationTier> tiers = new ArrayList<>();

    public static final Faction UNKNOWN = new Faction();
    static {
        UNKNOWN.name = "Unknown";
    }

    public Faction() {}

    public Faction(String displayName) {
        this.name = displayName;
    }

}
