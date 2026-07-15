package koolkrafter5.questrep.reputation;

public record ReputationTier(int value, String name) {

    public static final ReputationTier UNKNOWN = new ReputationTier(0, "Unknown");
    public static final ReputationTier MAX_INT = new ReputationTier(Integer.MAX_VALUE, "None");
    public static final ReputationTier MIN_INT = new ReputationTier(Integer.MIN_VALUE, "None");
}
