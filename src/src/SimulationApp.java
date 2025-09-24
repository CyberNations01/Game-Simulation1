import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.stream.Collectors;

public class SimulationApp {
    // ---------- CLI 入口 ----------
    public static void main(String[] args) {
        System.out.println("Please type your turn number in (1 - 100).");
        int turns  = -1;
        boolean invalid = true;
        while(invalid) {
            Scanner turn = new Scanner(System.in);
            turns = Integer.parseInt(turn.next());
            if(turns>=1&&turns<=100){
                invalid = false;
            }else{
                System.out.println("Invalid input! Please input again!");
            }
        }
        long seed = 1234L;

        // 初始 11 个栈的状态（可通过命令行覆盖；这里先都给 WILDS 作为示例）
        Map<Integer, State> init = new HashMap<>();
        for (int i = 1; i <= 11; i++) {
            System.out.println("Stack "+i+": Please type your initial states, choose one: 1 - WILDS, 2 - WASTES, 3 - DEVA, 4 - DEVB.");
            Scanner in = new Scanner(System.in);
            int input = Integer.parseInt(in.next());
            if(input != 1 && input != 2 && input != 3 && input !=4){
                System.out.println("Invalid input! Please enter in 1-4!");
                i--;
            }else {
                switch (input) {
                    case 1: {
                        init.put(i,State.WILDS);
                    }
                    case 2: {
                        init.put(i,State.WASTES);
                    }
                    case 3: {
                        init.put(i,State.DEVA);
                    }
                    case 4: {
                        init.put(i,State.DEVB);
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
