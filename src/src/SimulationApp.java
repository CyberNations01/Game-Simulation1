import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Collectors;

public class SimulationApp {
    // ---------- CLI Entrance ----------
    public static void main(String[] args) {
        int turns  = -1;
        long seed = 5L;
        int version = 1; // Default to Stage 1

        // Initialize the state of 11 stacks.
        Map<Integer, State> init = new HashMap<>();
        
        Scanner in = new Scanner(System.in);
        
        // Version selection
        while (true) {
            System.out.println("Please select version:");
            System.out.println("1 - Stage 1 (Basic version)");
            System.out.println("2 - Stage 2 (With disruption cards)");
            System.out.print("Enter version number (1 or 2): ");

            String versionInput = in.nextLine().trim();
            try {
                int selected = Integer.parseInt(versionInput);
                if (selected == 1 || selected == 2) {
                    version = selected;
                    System.out.println(selected == 2
                            ? "Stage 2 selected - With disruption cards enabled"
                            : "Stage 1 selected - Basic version");
                    break; // valid, exit loop
                } else {
                    System.out.printf("Invalid number: %s. Please enter 1 or 2.%n", versionInput);
                }
            } catch (NumberFormatException e) {
                System.out.printf("Invalid input: %s. Please enter 1 or 2.%n", versionInput);
            }
        }
//        System.out.println("Please select version:");
//        System.out.println("1 - Stage 1 (Basic version)");
//        System.out.println("2 - Stage 2 (With disruption cards)");
//        System.out.print("Enter version number (1 or 2): ");
//        String versionInput = in.nextLine().trim();
//
//        try {
//            int selectedVersion = Integer.parseInt(versionInput);
//            if (selectedVersion == 2) {
//                version = 2;
//                System.out.println("Stage 2 selected - With disruption cards enabled");
//            } else {
//                version = 1;
//                System.out.println("Stage 1 selected - Basic version");
//            }
//        } catch (NumberFormatException e) {
//            System.out.println("Invalid input, using default Stage 1");
//            version = 1;
//        }
        
        System.out.println("Would you like to manually set initial states or just put in a ratio? (y/n/r)");
        String choice = in.nextLine().trim().toLowerCase();

        if (choice.equals("n")) {
            // Automatic initialization.
            Random rng = new Random(50); // set the seed equal to 50.
            turns = rng.nextInt(1,100);
            System.out.println("The turns number randomly set to "+turns+".");
            for (int i = 1; i <= 11; i++) {
                int rand = 1 + rng.nextInt(4); // 1~4
                State st = switch (rand) {
                    case 1 -> State.WILDS;
                    case 2 -> State.WASTES;
                    case 3 -> State.DEVA;
                    case 4 -> State.DEVB;
                    default -> State.WILDS; // fallback, theoretically unreachable
                };
                init.put(i, st);
                System.out.println("Stack " + i + " randomly set to " + st);
            }
            Map<FeedbackToken, Integer> limitOverride = new EnumMap<>(FeedbackToken.class);
            for (String a : args) {
                if (a.startsWith("--turns=")) {
                    turns = Integer.parseInt(a.substring(8));
                } else if (a.startsWith("--seed=")) {
                    seed = Long.parseLong(a.substring(7));
                } else if (a.startsWith("--s")) {
                    // s3 means that the initial state is DevA.
                    String[] kv = a.substring(3).split("=");
                    int id = Integer.parseInt(kv[0]);
                    State st = State.valueOf(kv[1].toUpperCase());
                    init.put(id, st);
                } else if (a.startsWith("--limit=")) {
                    String spec = a.substring(8); // WILDS:30,DEVA:10
                    for (String part : spec.split(",")) {
                        String[] kv = part.split(":");
                        limitOverride.put(FeedbackToken.valueOf(kv[0].toUpperCase()), Integer.parseInt(kv[1]));
                    }
                }
            }
            Simulation sim = new Simulation(turns, seed, init, limitOverride, version);
            sim.run();
        } else if(choice.equals("y")){
            System.out.println("Please type your turn number in (1 - 100).");
            //System.out.println("Stack " + i + ": Please type your initial states, choose one: 1 - WILDS, 2 - WASTES, 3 - DEVA, 4 - DEVB.");
            boolean invalid = true;
            while (invalid) {
                Scanner turn = new Scanner(System.in);
                turns = Integer.parseInt(turn.next());
                if (turns >= 1 && turns <= 100) {
                    invalid = false;
                } else {
                    System.out.println("Invalid input! Please input again!");
                }
            }

            System.out.println("Please type in your maximum number of feedback tokens for each type (WILDS, WASTES, DEVA, DEVB), separated by spaces:");
            boolean validInput = false;
            int[] limits = new int[4];// Default values indicating no limits set
            limits[0] = -1;
            while (!validInput) {
                Scanner tokenInput = new Scanner(System.in);
                String line = tokenInput.nextLine();
                String[] parts = line.trim().split("\\s+");
                if (parts.length != 4) {
                    System.out.println("Invalid input! Please enter exactly four numbers separated by spaces.");
                    continue;
                }
                try {
                    int wildsLimit = Integer.parseInt(parts[0]);
                    int wastesLimit = Integer.parseInt(parts[1]);
                    int devaLimit = Integer.parseInt(parts[2]);
                    int devbLimit = Integer.parseInt(parts[3]);
                    if (wildsLimit < 0 || wastesLimit < 0 || devaLimit < 0 || devbLimit < 0) {
                        System.out.println("Invalid input! Please enter non-negative integers.");
                        continue;
                    }
                    validInput = true;
                    limits[0] = wildsLimit;
                    limits[1] = wastesLimit;
                    limits[2] = devaLimit;
                    limits[3] = devbLimit;
                    System.out.println("Token limits set to - WILDS: " + wildsLimit + ", WASTES: " + wastesLimit + ", DEVA: " + devaLimit + ", DEVB: " + devbLimit);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input! Please enter valid integers.");
                }
            }
            for (int i = 1; i <= 11; i++) {
                System.out.println("Stack " + i + ": Please type your initial states, choose one: 1 - WILDS, 2 - WASTES, 3 - DEVA, 4 - DEVB.");
                Scanner ins = new Scanner(System.in);
                int input = Integer.parseInt(ins.next());
                if (input != 1 && input != 2 && input != 3 && input != 4) {
                    System.out.println("Invalid input! Please enter in 1-4!");
                    i--;
                } else {
                    switch (input) {
                        case 1: {
                            init.put(i, State.WILDS);
                            break;
                        }
                        case 2: {
                            init.put(i, State.WASTES);
                            break;
                        }
                        case 3: {
                            init.put(i, State.DEVA);
                            break;
                        }
                        case 4: {
                            init.put(i, State.DEVB);
                            break;
                        }
                    }
                }
            }
            Map<FeedbackToken, Integer> limitOverride = new EnumMap<>(FeedbackToken.class);
            for (String a : args) {
                if (a.startsWith("--turns=")) {
                    turns = Integer.parseInt(a.substring(8));
                } else if (a.startsWith("--seed=")) {
                    seed = Long.parseLong(a.substring(7));
                } else if (a.startsWith("--s")) {
                    // s3 means that the initial state is DevA.
                    String[] kv = a.substring(3).split("=");
                    int id = Integer.parseInt(kv[0]);
                    State st = State.valueOf(kv[1].toUpperCase());
                    init.put(id, st);
                } else if (a.startsWith("--limit=")) {
                    String spec = a.substring(8); // WILDS:30,DEVA:10
                    for (String part : spec.split(",")) {
                        String[] kv = part.split(":");
                        limitOverride.put(FeedbackToken.valueOf(kv[0].toUpperCase()), Integer.parseInt(kv[1]));
                    }
                }
            }
            Simulation sim = new Simulation(turns, seed, init, limitOverride, limits, version);
            sim.run();
        }else {
//            System.out.println("Please type your turn number in (1 - 100).");
//            //System.out.println("Stack " + i + ": Please type your initial states, choose one: 1 - WILDS, 2 - WASTES, 3 - DEVA, 4 - DEVB.");
//            boolean invalid = true;
//            while (invalid) {
//                Scanner turn = new Scanner(System.in);
//                turns = Integer.parseInt(turn.next());
//                if (turns >= 1 && turns <= 100) {
//                    invalid = false;
//                } else {
//                    System.out.println("Invalid input! Please input again!");
//                }
//            }
            turns = 10;
            Random rng = new Random();
            //System.out.println("We only test on the ratio of WILD and DEVA now, please type in the number of WILD you want in this 11 stacks.");
            //Scanner ratio = new Scanner(System.in);

//            if(num<0||num>11) {
//                System.out.println("Invalid input! Please type in a number between 0-11.");
//            }else {
//                Random rng = new Random(); // 或者传入 Simulation 的 seed
//                init = generateWildDevaState(num, rng);
//            }
            for (int i=0; i<=11;i++){
              for(int k=0; k<30; k++){
                    init = generateWildDevaState(i, rng);
                    // Some parameters: --turns=N --seed=S --s3=DEVA --limit=WILDS:30,DEVA:10
                    Map<FeedbackToken, Integer> limitOverride = new EnumMap<>(FeedbackToken.class);
                    for (String a : args) {
                        if (a.startsWith("--turns=")) {
                            turns = Integer.parseInt(a.substring(8));
                        } else if (a.startsWith("--seed=")) {
                            seed = Long.parseLong(a.substring(7));
                        } else if (a.startsWith("--s")) {
                            // s3 means that the initial state is DevA.
                            String[] kv = a.substring(3).split("=");
                            int id = Integer.parseInt(kv[0]);
                            State st = State.valueOf(kv[1].toUpperCase());
                            init.put(id, st);
                        } else if (a.startsWith("--limit=")) {
                            String spec = a.substring(8); // WILDS:30,DEVA:10
                            for (String part : spec.split(",")) {
                                String[] kv = part.split(":");
                                limitOverride.put(FeedbackToken.valueOf(kv[0].toUpperCase()), Integer.parseInt(kv[1]));
                            }
                        }
                    }
                    Simulation sim = new Simulation(turns, seed, init, limitOverride, version);
                    sim.run();
               }
            }
        }
    }

    public static Map<Integer, State> generateWildDevaState(int wildCount, Random rng) {
        if (wildCount < 0 || wildCount > 11) {
            throw new IllegalArgumentException("WILD count must be between 0 and 11.");
        }

        Map<Integer, State> initialStates = new HashMap<>();

        // 生成 1~11 中的不重复 wildCount 个随机位置
        List<Integer> positions = new ArrayList<>();
        for (int i = 1; i <= 11; i++) positions.add(i);
        Collections.shuffle(positions, rng);
        List<Integer> wildPositions = positions.subList(0, wildCount);

        // 设置状态
        for (int i = 1; i <= 11; i++) {
            if (wildPositions.contains(i)) {
                initialStates.put(i, State.WILDS);
            } else {
                initialStates.put(i, State.DEVA);
            }
        }
        return initialStates;
    }
}
