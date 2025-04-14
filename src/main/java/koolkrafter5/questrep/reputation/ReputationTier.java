package koolkrafter5.questrep.reputation;

public class ReputationTier {

    public static final ReputationTier UNKNOWN = new ReputationTier(0, "Unknown");
    public static final ReputationTier MAX_INT = new ReputationTier(Integer.MAX_VALUE, "None");
    public static final ReputationTier MIN_INT = new ReputationTier(Integer.MIN_VALUE, "None");
    public String name = "Neutral";
    public int value = 0;

    public ReputationTier(int value, String tierName) {
        this.name = tierName;
        this.value = value;
    }

    public ReputationTier() {

    }
}
