import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Simulation {
    final List<MyStack> myStacks = new ArrayList<>(11);
    final Bag bag;
    final Random rng;
    final int turns;
    private int currentRound = 0;

    public int getCurrentRound() {
        return currentRound;
    }

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

        // Create 11 Stacks.
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


    /**
     * 每回合：1) 生成 11 token 入池；2) 抽 11 个按顺序结算；3) 按规则回收/保留
     */
    void run() {
        // This is to store the result of each turn.
        List<int[]> timelineCodes = new ArrayList<>();

        int[] round0 = new int[myStacks.size()];
        for (int i = 0; i < myStacks.size(); i++) {
            round0[i] = getCodeFromState(myStacks.get(i).state);
        }
        timelineCodes.add(round0);

        for (int t = 1; t <= turns; t++) {
            // 1) 各 MyStack 生成 token 入池（受上限）
            this.currentRound = t;

            for (MyStack s : myStacks) {
                bag.add(toTokenFromState(s.state));
            }


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



            // 3) 回收：2-7 回 Pool；1、8–11 留在板上（不回池）
            for (int i = 0; i < ORDER.size(); i++) {
                int pos = ORDER.get(i);
                FeedbackToken tok = drawn.get(i);
                if (!PERSIST_POSITIONS.contains(pos)) {
                    bag.putBack(tok);
                }
            }


            // Record the result of this turn.
            int[] snapshot = new int[myStacks.size()];
            for (int i = 0; i < myStacks.size(); i++) {
                snapshot[i] = getCodeFromState(myStacks.get(i).state);
            }
            timelineCodes.add(snapshot);

            // 输出当回合摘要
            System.out.println("Turn " + t + " done.");
            printStacks();
        }
        exportResultToJson(myStacks, bag, currentRound, turns, timelineCodes);
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


    // Transform the state into numbers.
    private static int getCodeFromState(State state) {
        return switch (state) {
            case WILDS  -> 1;
            case WASTES -> 2;
            case DEVA   -> 3;
            case DEVB   -> 4;
        };
    }

    public static void exportResultToJson(
            List<MyStack> myStacks,
            Bag bag,
            int currentRound,
            int maxRounds,
            List<int[]> timelineCodes
    ) {
        ObjectMapper mapper = new ObjectMapper();

        // 构建根节点
        ObjectNode root = mapper.createObjectNode();
        root.put("version", 1);

        // legend：Status Code List.
        ObjectNode legend = mapper.createObjectNode();
        legend.put("WILDS", 1);
        legend.put("WASTES", 2);
        legend.put("DEVA", 3);
        legend.put("DEVB", 4);
        root.set("legend", legend);

        // 构建 board.hexes
        ObjectNode board = mapper.createObjectNode();
        var hexesArray = mapper.createArrayNode();
        for (MyStack s : myStacks) {
            var hex = mapper.createObjectNode();
            hex.put("id", s.id);
            hex.put("type", s.state.name());
            hex.put("color", getColorFromState(s.state));
            hexesArray.add(hex);
        }
        board.set("hexes", hexesArray);
        root.set("board", board);

        // game_state
        ObjectNode gameState = mapper.createObjectNode();
        gameState.put("current_round", currentRound);
        gameState.put("max_rounds", maxRounds);
        gameState.put("bag_total", bag.totalCount());
        root.set("game_state", gameState);

        // tokens
        ObjectNode tokens = mapper.createObjectNode();
        for (FeedbackToken token : FeedbackToken.values()) {
            tokens.put(token.name(), bag.count(token));
        }
        root.set("tokens", tokens);

        // timeline：每一轮的状态代码数组（按 id=1..11）
        var timeline = mapper.createArrayNode();
        for (int r = 0; r < timelineCodes.size(); r++) {
            ObjectNode round = mapper.createObjectNode();
            round.put("round", r);
            var arr = mapper.createArrayNode();
            for (int code : timelineCodes.get(r)) arr.add(code);
            round.set("states", arr);
            timeline.add(round);
        }
        root.set("timeline", timeline);

        // 创建 assets 目录（如果不存在）
        File dir = new File("assets");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成带时间戳的文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File output = new File(dir, "simulation_result_" + timestamp + ".json");

        // 写入文件
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(output, root);
            System.out.println("Exported to: " + output.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 状态到颜色的映射
    private static String getColorFromState(State state) {
        return switch (state) {
            case WILDS -> "green";
            case WASTES -> "brown";
            case DEVA -> "pink";
            case DEVB -> "blue";
        };
    }
}
