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
     * 向池中加入一个 token（受上限限制）
     */
    void add(FeedbackToken t) {
        int c = counts.get(t);
        int lim = limitPerType.get(t);
        if (c < lim) counts.put(t, c + 1);
    }

    /**
     * 抽一个；若池为空则返回空（Optional）
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
        return Optional.empty(); // 不应到达
    }

    /**
     * 放回一个（不计上限，用于“回 Pool”的回收逻辑）
     */
    void putBack(FeedbackToken t) {
        counts.put(t, counts.get(t) + 1);
    }

    Map<FeedbackToken, Integer> snapshot() {
        return new EnumMap<>(counts);
    }
//    int getLimit(FeedbackToken t) { return limitPerType.get(t); }
//    int getCount(FeedbackToken t) { return counts.get(t); }
//    int totalCount() { return counts.values().stream().mapToInt(Integer::intValue).sum(); }
}

