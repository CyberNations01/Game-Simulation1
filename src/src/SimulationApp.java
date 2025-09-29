import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Collectors;

public class SimulationApp {
    // ---------- CLI Entrance ----------
    public static void main(String[] args) {
        int turns  = -1;
        long seed = 1L;

        // Initialize the state of 11 stacks.
        Map<Integer, State> init = new HashMap<>();
        System.out.println("Would you like to manually set initial states? (y/n)");
        Scanner in = new Scanner(System.in);
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
        } else {
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
        }

        // 上限覆盖（默认每种 20）
        Map<FeedbackToken, Integer> limitOverride = new EnumMap<>(FeedbackToken.class);

        // 解析简单参数：--turns N --eed S --s3=DEVA --limit=WILDS:30,DEVA:10
        for (String a : args) {
            if (a.startsWith("--turns=")) {
                turns = Integer.parseInt(a.substring(8));
            } else if (a.startsWith("--seed=")) {
                seed = Long.parseLong(a.substring(7));
            } else if (a.startsWith("--s")) {
                // 例如 --s3=DEVA 表示 stack#3 初始状态为 DEVA
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
        Simulation sim = new Simulation(turns, seed, init, limitOverride);
        sim.run();
    }

}
