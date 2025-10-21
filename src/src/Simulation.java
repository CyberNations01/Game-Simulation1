import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Simulation {
    final List<MyStack> myStacks = new ArrayList<>(11);
    final Bag bag;
    final Random rng;
    final int turns;
    final long seed;
    private int currentRound = 0;
    private DisruptionCardManager disruptionManager;
    private Parameters parameters;
    private int version = 1; // 1 = Stage 1, 2 = Stage 2

    public int getCurrentRound() {
        return currentRound;
    }

    // Thw circles: 1(inner), 2-7(middle), 8-11(outer)
    static final List<Integer> ORDER = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);

    // Tokens in positions 1, 8–11 do not return to the Pool, while the remaining tokens in positions 2–7 return to the Pool.
    static final Set<Integer> PERSIST_POSITIONS = Set.of(1, 8, 9, 10, 11);

    Simulation(int turns, long seed,
               Map<Integer, State> initialStates,
               Map<FeedbackToken, Integer> poolLimitOverride) {
        this(turns, seed, initialStates, poolLimitOverride, 1); // Default to Stage 1
    }
    
    Simulation(int turns, long seed,
               Map<Integer, State> initialStates,
               Map<FeedbackToken, Integer> poolLimitOverride,
               int version) {
        if (turns < 1 || turns > 100) throw new IllegalArgumentException("turns must be 1..100");
        this.turns = turns;
        this.seed  = seed;
        this.rng = new Random(seed);
        this.version = version;
        this.bag = new Bag(rng, 20); // Default is 20.
        
        // Initialize Stage 2 components
        if (version == 2) {
            this.disruptionManager = new DisruptionCardManager();
            this.parameters = new Parameters();
            
            // Load disruption cards
            if (!disruptionManager.loadCardsFromFile("game-data/disruption.json")) {
                System.out.println("Warning: Failed to load disruption cards, using Stage 1 mode");
                this.version = 1;
            } else {
                System.out.println("Stage 2 mode enabled with disruption cards");
            }
        }

        // Set the bag limit to 20.
        if (poolLimitOverride != null) {
            for (var e : poolLimitOverride.entrySet()) bag.setLimit(e.getKey(), e.getValue());
        }

        // Create 11 Stacks.
        // 1: inner, 2-7: middle, 8-11: outer
        for (int id = 1; id <= 11; id++) {
            StackRing ring = (id == 1) ? StackRing.INNER :
                    (id >= 2 && id <= 7) ? StackRing.MIDDLE : StackRing.OUTER;
            State init = initialStates.getOrDefault(id, State.WILDS); // Default to WILDS, modify as needed
            myStacks.add(new MyStack(id, ring, init));
        }
        System.out.println("Init states => " +
                myStacks.stream().map(s -> s.id + ":" + s.state.name())
                        .collect(Collectors.joining("  ")));
    }

    Simulation(int turns, long seed,
               Map<Integer, State> initialStates,
               Map<FeedbackToken, Integer> poolLimitOverride,
               int[] poolLimitsArray) {
        this(turns, seed, initialStates, poolLimitOverride, poolLimitsArray, 1); // Default to Stage 1
    }
    
    Simulation(int turns, long seed,
               Map<Integer, State> initialStates,
               Map<FeedbackToken, Integer> poolLimitOverride,
               int[] poolLimitsArray,
               int version) {
        if (turns < 1 || turns > 100) throw new IllegalArgumentException("turns must be 1..100");
        this.turns = turns;
        this.seed  = seed;
        this.rng = new Random(seed);
        this.version = version;
        this.bag = new Bag(rng, poolLimitsArray, 20); // Default is 20.
        
        // Initialize Stage 2 components
        if (version == 2) {
            this.disruptionManager = new DisruptionCardManager();
            this.parameters = new Parameters();
            
            // Load disruption cards
            if (!disruptionManager.loadCardsFromFile("game-data/disruption.json")) {
                System.out.println("Warning: Failed to load disruption cards, using Stage 1 mode");
                this.version = 1;
            } else {
                System.out.println("Stage 2 mode enabled with disruption cards");
            }
        }

        // Set the bag limit to 20.
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
     * Each turn: 1) generate 11 tokens and put them into the bag; 2) Draw 11 cards and resolve them in sequence. 3) Recycle or retain according to the rules.
     */
    void run() {
        // This is to store the result of each turn.
        List<int[]> timelineCodes = new ArrayList<>();
        List<ObjectNode> roundSnapshots = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        int[] round0 = new int[myStacks.size()];
        for (int i = 0; i < myStacks.size(); i++) {
            round0[i] = getCodeFromState(myStacks.get(i).state);
        }
        timelineCodes.add(round0);

        for (int t = 1; t <= turns; t++) {
            // 1) Each MyStack generates tokens into the pool (subject to the cap).
            roundSnapshots.add(createRoundSnapshot(mapper, myStacks, bag, currentRound, seed, turns));
            this.currentRound = t;

            for (MyStack s : myStacks) {
                bag.add(toTokenFromState(s.state));
            }


            // 2) Draw 11 cards and settle according to ORDER.
            List<FeedbackToken> drawn = new ArrayList<>();
            for (int pos : ORDER) {
                Optional<FeedbackToken> opt = bag.drawOne();
                FeedbackToken tok = opt.orElseGet(() -> randomAnyToken()); // When the pool is empty, default to random selection (in extreme cases).
                drawn.add(tok);
                // Immediately settle to the corresponding MyStack.
                MyStack target = myStacks.get(pos - 1); // ids start at 1, while lists start at 0.
                if (version == 2 && disruptionManager != null) {
                    tok.resolveOn(target, disruptionManager);
                } else {
                    tok.resolveOn(target);
                }
            }


            // 3) recycle：put 2-7 into the pool and 1, 8–11 remain on the board (do not return to pool)
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

            // print the summary of this turn.
            System.out.println("Turn " + t + " done.");
            printStacks();
        }
        roundSnapshots.add(createRoundSnapshot(mapper, myStacks, bag, currentRound, seed, turns));
        exportResultToJson(myStacks, bag, currentRound, turns, timelineCodes, roundSnapshots, seed);
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
            List<int[]> timelineCodes,
            List<ObjectNode> roundSnapshots,
            long seed
            ) {
        ObjectMapper mapper = new ObjectMapper();

        // Create the root.
        ObjectNode root = mapper.createObjectNode();
        root.put("version", 1);

        // legend：Status Code List.
        ObjectNode legend = mapper.createObjectNode();
        legend.put("WILDS", 1);
        legend.put("WASTES", 2);
        legend.put("DEVA", 3);
        legend.put("DEVB", 4);
        root.set("legend", legend);

        ObjectNode maximum_tokens = mapper.createObjectNode();
        maximum_tokens.put("WILDS", bag.limitPerType.get(FeedbackToken.WILDS));
        maximum_tokens.put("WASTES", bag.limitPerType.get(FeedbackToken.WASTES));
        maximum_tokens.put("DEVA", bag.limitPerType.get(FeedbackToken.DEVA));
        maximum_tokens.put("DEVB", bag.limitPerType.get(FeedbackToken.DEVB));
        root.set("maximum_tokens", maximum_tokens);

        // create board.hexes
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
        gameState.put("seed", seed);
        root.set("game_state", gameState);

        // tokens
        ObjectNode tokens = mapper.createObjectNode();
        for (FeedbackToken token : FeedbackToken.values()) {
            tokens.put(token.name(), bag.count(token));
        }
        root.set("tokens", tokens);

        // timeline：arrays for states in each turn.
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

        ArrayNode roundOutputs = mapper.createArrayNode();
        for (ObjectNode node : roundSnapshots) {
            roundOutputs.add(node);
        }
        root.set("round_outputs", roundOutputs);

        // create assets files.
        File dir = new File("assets");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // generate files with timestamps.
        String file_name= String.format("simulation_result_%s_%d.json",
                new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date()),
                System.nanoTime()
        );
        File output = new File(dir, file_name);

        // write in files.
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(output, root);
            //System.out.println("Exported to: " + output.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("✅");
    }

    // reflect the states on colors.
    private static String getColorFromState(State state) {
        return switch (state) {
            case WILDS -> "green";
            case WASTES -> "brown";
            case DEVA -> "pink";
            case DEVB -> "blue";
        };
    }
    private ObjectNode createRoundSnapshot(
            ObjectMapper mapper,
            List<MyStack> myStacks,
            Bag bag,
            int currentRound,
            long seed,
            int max_round
    ) {
        ObjectNode snapshot = mapper.createObjectNode();

        snapshot.put("round: ", currentRound);

        // 添加 hex 状态信息
        ObjectNode board = mapper.createObjectNode();
        ArrayNode hexesArray = mapper.createArrayNode();
        for (MyStack s : myStacks) {
            ObjectNode hex = mapper.createObjectNode();
            hex.put("id", s.id);
            hex.put("type", s.state.name());
            hex.put("color", getColorFromState(s.state));
            hexesArray.add(hex);
        }
        board.set("hexes", hexesArray);
        snapshot.set("board", board);

        ObjectNode game = mapper.createObjectNode();
        game.put("current_round", currentRound);
        game.put("max_round", max_round);
        game.put("bag_total", bag.totalCount());
        game.put("seed", seed);

        snapshot.set("game_state", game);

        // 添加当前 bag 状态
        ObjectNode tokens = mapper.createObjectNode();
        for (FeedbackToken token : FeedbackToken.values()) {
            tokens.put(token.name(), bag.count(token));
        }
        snapshot.set("tokens", tokens);

        return snapshot;
    }
}
