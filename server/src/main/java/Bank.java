import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bank {

    private List<Pair<Integer, Integer>> ESMLevels;
    private List<Pair<Integer, Integer>> EGPLevels;
    private final double[][] levelMatrix = {
            {1.0 / 3.0, 1.0 / 3.0, 1.0 / 6.0, 1.0 / 12.0, 1.0 / 12.0},
            {1.0 / 4.0, 1.0 / 3.0, 1.0 / 4.0, 1.0 / 12.0, 1.0 / 12.0},
            {1.0 / 12.0, 1.0 / 4.0, 1.0 / 3.0, 1.0 / 4.0, 1.0 / 12.0},
            {1.0 / 12.0, 1.0 / 12.0, 1.0 / 4.0, 1.0 / 3.0, 1.0 / 4.0},
            {1.0 / 12.0, 1.0 / 12.0, 1.0 / 6.0, 1.0 / 4.0, 1.0 / 3.0}};
    private int count;
    private int level;
    private final int loan = 5000;
    private final int loanAdvanced = 10000;
    private final int fabricCost = 5000;
    private final int automatedFabricCost = 10000;
    private final int automationCost = 7000;

    public Bank(int count) {
        this.count = count;
        level = 0;
        ESMLevels = new ArrayList<Pair<Integer, Integer>>(5);
        EGPLevels = new ArrayList<Pair<Integer, Integer>>(5);
        for (int i = 0; i < 5; i++) {
            ESMLevels.set(i,
                    new ImmutablePair<Integer, Integer>((int) (
                            Math.floor(1.0 + i * 0.5) * (count)),
                            i < 2
                                    ? 800 - 150 * i
                                    : 800 - 100 * (i + 1)));
            EGPLevels.set(i,
                    new ImmutablePair<Integer, Integer>(
                            (int) (Math.floor(3.0 - i * 0.5) * (count)),
                            6500 - 500 * i));
        }
    }

    public void makeTurn() {

        Random random = new Random();
        int i = 0;
        while (true) {
            double p = random.nextDouble() / Double.MAX_VALUE;
            if (p <= levelMatrix[level][i]) {
                level = i % 5;
                break;
            }
            i++;
        }
    }

    private int getCash(int id) {
        return 0;
    }

    private void sellESM(List<Pair<Integer, Integer>> bids) {
    }

    private void buyEGP(List<Pair<Integer, Integer>> bids) {
    }

}
