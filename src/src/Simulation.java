import java.util.*;
import java.util.stream.Collectors;

public class Simulation {
        final List<MyStack> myStacks = new ArrayList<>(11);
        final Bag bag;
        final Random rng;
        final int turns;

        // 位置顺序（与图一致）：1(inner), 2-7(middle), 8-11(outer)
        static final List<Integer> ORDER = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);

        // 假设：位置 1、8–11 的令牌“留在板上”（不回 Pool），其余 2–7 回 Pool
        static final Set<Integer> PERSIST_POSITIONS = Set.of(1, 8, 9, 10, 11);

        Simulation(int turns, long seed,
                   Map<Integer, State> initialStates,
                   Map<FeedbackToken, Integer> poolLimitOverride) {
            if (turns < 1 || turns > 100) throw new IllegalArgumentException("turns must be 1..100");
            this.turns = turns;
            this.rng = new Random(seed);
            this.bag = new Bag(rng, 20); // 默认每种上限 20

            // 上限覆盖
            if (poolLimitOverride != null) {
                for (var e : poolLimitOverride.entrySet()) bag.setLimit(e.getKey(), e.getValue());
            }

            // 构建 11 个 Stacks
            // 1: inner, 2-7: middle, 8-11: outer
            for (int id = 1; id <= 11; id++) {
                StackRing ring = (id == 1) ? StackRing.INNER :
                        (id >= 2 && id <= 7) ? StackRing.MIDDLE : StackRing.OUTER;
                State init = initialStates.getOrDefault(id, State.WILDS); // 默认给 WILDS，按需改
                myStacks.add(new MyStack(id, ring, init));
            }
            System.out.println("Init states => " +
                    myStacks.stream().map(s -> s.id + ":" + s.state.name())
                            .collect(Collectors.joining("  ")));
        }

//    private void debugBag(String when) {
//        System.out.printf(
//                "Bag@%s  Wi=%d/%d  Wa=%d/%d  A=%d/%d  B=%d/%d  total=%d%n",
//                when,
//                bag.getCount(FeedbackToken.WILDS),  bag.getLimit(FeedbackToken.WILDS),
//                bag.getCount(FeedbackToken.WASTES), bag.getLimit(FeedbackToken.WASTES),
//                bag.getCount(FeedbackToken.DEVA),   bag.getLimit(FeedbackToken.DEVA),
//                bag.getCount(FeedbackToken.DEVB),   bag.getLimit(FeedbackToken.DEVB),
//                bag.totalCount()
//        );
//    }

        /**
         * 每回合：1) 生成 11 token 入池；2) 抽 11 个按顺序结算；3) 按规则回收/保留
         */
        void run() {
            for (int t = 1; t <= turns; t++) {
                // 1) 各 MyStack 生成 token 入池（受上限）
                for (MyStack s : myStacks) {
                    bag.add(toTokenFromState(s.state));
                }
//                debugBag("afterAdd");

                // 2) 抽 11 个并按 ORDER 结算
                List<FeedbackToken> drawn = new ArrayList<>();
                for (int pos : ORDER) {
                    Optional<FeedbackToken> opt = bag.drawOne();
                    FeedbackToken tok = opt.orElseGet(() -> randomAnyToken()); // 池空时兜底随机（极端情况）
                    drawn.add(tok);
                    // 即时结算到对应 MyStack
                    MyStack target = myStacks.get(pos - 1); // id 从 1 开始，list 从 0 开始
                    tok.resolveOn(target);
                }
//                debugBag("afterDraw");


                // 3) 回收：2-7 回 Pool；1、8–11 留在板上（不回池）
                for (int i = 0; i < ORDER.size(); i++) {
                    int pos = ORDER.get(i);
                    FeedbackToken tok = drawn.get(i);
                    if (!PERSIST_POSITIONS.contains(pos)) {
                        bag.putBack(tok);
                    }
                }
//                debugBag("afterPutBack");

                // 输出当回合摘要
                System.out.println("Turn " + t + " done.");
                printStacks();
            }

        }

        private void printStacks() {
            String s = myStacks.stream()
                    .sorted(Comparator.comparingInt(st -> st.id))
                    .map(st -> st.id + ":" + st.state.name())
                    .collect(Collectors.joining("  "));
            System.out.println("Stacks => " + s);
        }

        private FeedbackToken toTokenFromState(State st) {
            return switch (st) {
                case WILDS -> FeedbackToken.WILDS;
                case WASTES -> FeedbackToken.WASTES;
                case DEVA -> FeedbackToken.DEVA;
                case DEVB -> FeedbackToken.DEVB;
            };
        }

        private FeedbackToken randomAnyToken() {
            FeedbackToken[] all = FeedbackToken.values();
            return all[rng.nextInt(all.length)];
        }
    }
