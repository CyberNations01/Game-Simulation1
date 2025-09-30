import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class Bag {
    final EnumMap<FeedbackToken, Integer> limitPerType = new EnumMap<>(FeedbackToken.class);
    final EnumMap<FeedbackToken, Integer> counts = new EnumMap<>(FeedbackToken.class);
    final Random rng;

    Bag(Random rng, int defaultLimitEach) {
        this.rng = rng;
        for (FeedbackToken t : FeedbackToken.values()) {
            limitPerType.put(t, defaultLimitEach);
            counts.put(t, 0);
        }
    }

    void setLimit(FeedbackToken t, int limit) {
        limitPerType.put(t, limit);
    }

    /**
     * Add a token to the pool (subject to the upper limit).
     */
    void add(FeedbackToken t) {
        int c = counts.get(t);
        int lim = limitPerType.get(t);
        if (c < lim) counts.put(t, c + 1);
    }

    /**
     * Draw one; if the pool is empty, return null.
     */
    Optional<FeedbackToken> drawOne() {
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) return Optional.empty();
        int r = rng.nextInt(total);
        for (FeedbackToken t : FeedbackToken.values()) {
            int c = counts.get(t);
            if (r < c) {
                counts.put(t, c - 1);
                return Optional.of(t);
            }
            r -= c;
        }
        return Optional.empty();
    }

    /**
     * Return one (without upper limit, for the "return to pool" recycling logic)
     */
    void putBack(FeedbackToken t) {
        counts.put(t, counts.get(t) + 1);
    }

    Map<FeedbackToken, Integer> snapshot() {
        return new EnumMap<>(counts);
    }
    /**
     *
     * Get the total number of tokens in the current bag.
     */
    public int totalCount() {
    int total = 0;
    for (int count : counts.values()) {
        total += count;
    }
    return total;
}

    /** Retrieve the quantity of a specific token type.*/
    public int count(FeedbackToken tokenType) {
        return counts.getOrDefault(tokenType, 0);
    }
}


