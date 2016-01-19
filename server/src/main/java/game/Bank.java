package game;

import database.DBConnector;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import socketServer.Decision;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

class Bank {

    private List<Pair<Integer, Integer>> ESMLevels;
    private List<Pair<Integer, Integer>> EGPLevels;
    private final double[][] levelMatrix = {
            {1.0 / 3.0, 1.0 / 3.0, 1.0 / 6.0, 1.0 / 12.0, 1.0 / 12.0},
            {1.0 / 4.0, 1.0 / 3.0, 1.0 / 4.0, 1.0 / 12.0, 1.0 / 12.0},
            {1.0 / 12.0, 1.0 / 4.0, 1.0 / 3.0, 1.0 / 4.0, 1.0 / 12.0},
            {1.0 / 12.0, 1.0 / 12.0, 1.0 / 4.0, 1.0 / 3.0, 1.0 / 4.0},
            {1.0 / 12.0, 1.0 / 12.0, 1.0 / 6.0, 1.0 / 4.0, 1.0 / 3.0}};
    private int usersCount;
    private int level;
    private final int loan = 5000;
    private final int loanAdvanced = 10000;
    private final int fabricCost = 5000;
    private final int automatedFabricCost = 10000;
    private final int automationCost = 7000;

    private Bank() {
        usersCount = 0;
        level = 0;
        ESMLevels = new ArrayList<>(5);
        EGPLevels = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            ESMLevels.set(i,
                    new ImmutablePair<>((int) (
                            Math.floor(1.0 + i * 0.5) * (usersCount)),
                            i < 2
                                    ? 800 - 150 * i
                                    : 800 - 100 * (i + 1)));
            EGPLevels.set(i,
                    new ImmutablePair<>(
                            (int) (Math.floor(3.0 - i * 0.5) * (usersCount)),
                            6500 - 500 * i));
        }
    }

    private Map<Integer, Pair<Integer, Integer>> sellList;
    private Map<Integer, Pair<Integer, Integer>> buyList;
    private Map<Integer, Pair<Integer, Integer>> produceEGPList;
    private Map<Integer, Integer> buildFabricCountList;
    private Map<Integer, Integer> automateFabricCountList;
    private Map<Integer, Integer> buildAFabricCountList;
    private Map<Integer, Integer> loanCountList;

    public synchronized void collectTurnRequest(int sessionId,
                                                Pair<Integer, Integer> sell,
                                                Pair<Integer, Integer> buy,
                                                Pair<Integer, Integer> produceEGP,
                                                int buildFabricCount,
                                                int automateFabricCount,
                                                int buildAFabricCount,
                                                int loanCount) throws Exception {
        sellList.put(sessionId, sell);
        buyList.put(sessionId, buy);
        produceEGPList.put(sessionId, produceEGP);
        buildFabricCountList.put(sessionId, buildFabricCount);
        automateFabricCountList.put(sessionId, automateFabricCount);
        buildAFabricCountList.put(sessionId, buildAFabricCount);
        loanCountList.put(sessionId, loanCount);
        if (loanCountList.size() == usersCount) {
            makeDecision();
        }
    }

    Map<Integer, Decision> decisions;

    Map<Integer, Map<String, Integer>> currentStates;

    private void makeDecision() throws Exception {
        makeMapsOrdered();
        refreshStates();
        decisions = new HashMap<>();
        for (int id : sellList.keySet()) {
            Decision decision = new Decision(id);

            decisions.put(id, decision);
        }

        makeESMDecision(decisions);

        makeEGPDecision(decisions);

        makeLoanDecision(decisions);

        makeFabricsDecision(decisions);

        makeCashDecision(decisions);

        setNewStates(decisions);
    }

    private void makeCashDecision(Map<Integer, Decision> decisions) {
        for (int id : sellList.keySet()) {
            Decision decision = decisions.get(id);
            Map<String, Integer> currentState = currentStates.get(Game.getInstance().getClient(id).getId());
            int newCashValue = currentState.get("CASH");
            newCashValue -= (decision.getFCount() * fabricCost
                    + decision.getUFCount() * automationCost
                    + decision.getAFCount() * automatedFabricCost);
            newCashValue += decision.getLoan();
            newCashValue += (decision.getEGP() * sellList.get(id).getRight());
            newCashValue -= (decision.getESM() * buyList.get(id).getRight());
            decision.setCash(newCashValue);
        }
    }

    private void makeFabricsDecision(Map<Integer, Decision> decisions) {
        for (int id : sellList.keySet()) {
            Decision decision = decisions.get(id);
            decision.setAFCount(buildAFabricCountList.get(id));
            decision.setFCount(buildFabricCountList.get(id));
            decision.setUFCount(automateFabricCountList.get(id));
        }
    }

    private void refreshStates() {
        currentStates =
                DBConnector.getCurrentClientsState(
                        Game.getInstance().getGameId(),
                        Game.getInstance().getSaveId());
    }

    private void makeLoanDecision(Map<Integer, Decision> decisions) {
        for (Map.Entry<Integer, Integer> entry : loanCountList.entrySet()) {
            Map<String, Integer> currentState = currentStates.get(entry.getKey());
            int currentLoan = currentState.get("LOAN");
            int currentCapital = getCurCap(currentState);
            if (currentLoan + entry.getValue() <= currentCapital / 2) {
                decisions.get(entry.getKey()).setLoan(entry.getValue());
            }
        }
    }

    private int getCurCap(Map<String, Integer> currentState) {
        int capital = 0;
        int cash = currentState.get("CASH");
        int esm = currentState.get("ESM");
        int egp = currentState.get("EGP");
        int fCount = currentState.get("FABRIC_COUNT");
        int aFCount = currentState.get("A_FABRIC_COUNT");
        fCount += currentState.get("U_FABRIC_COUNT");
        capital = cash +
                fCount * fabricCost +
                aFCount * automatedFabricCost +
                esm * ESMLevels.get(level).getRight() +
                egp * EGPLevels.get(level).getRight();
        return capital;
    }

    private Map<Integer, Map<String, Integer>> setNewStates(Map<Integer, Decision> decisions) throws Exception {
        Map<Integer, Map<String, Integer>> newStates = new HashMap<>();
        for (Map.Entry<Integer, Map<String, Integer>> entry : currentStates.entrySet()) {
            int sessionId = Game.getInstance().getClientSessionId(entry.getKey());
            Decision decision;
            if (sessionId < 0) throw new Exception("Client not found");
            else decision = decisions.get(sessionId);

            Map<String, Integer> map = new HashMap<>();
            Map<String, Integer> oldMap = entry.getValue();

            map.put("CASH", oldMap.get("CASH") + decision.getCash());
            map.put("ESM", oldMap.get("ESM") + decision.getESM());
            map.put("EGP", oldMap.get("EGP") + decision.getEGP());
            map.put("LOAN", oldMap.get("LOAN") + decision.getLoan());
            map.put("FABRIC_COUNT", oldMap.get("FABRIC_COUNT") + decision.getFCount() - decision.getUFCount());
            map.put("A_FABRIC_COUNT", oldMap.get("A_FABRIC_COUNT") +
                    decision.getAFCount() +
                    (decision.getUFCount() < 0
                            ? -decision.getUFCount()
                            : 0));
            map.put("U_FABRIC_COUNT", oldMap.get("U_FABRIC_COUNT") + decision.getUFCount());

            newStates.put(entry.getKey(), map);
        }
        return newStates;
    }

    private void makeEGPDecision(Map<Integer, Decision> decisions) {
        Pair<Integer, Integer> EGPLevel = EGPLevels.get(level);
        int availableEGPCount = EGPLevel.getKey();
        int maxEGPPrice = EGPLevel.getValue();
        for (Map.Entry<Integer, Pair<Integer, Integer>> entry : sellList.entrySet()) {
            Pair<Integer, Integer> request = entry.getValue();
            int producedEGP = produceEGPList.get(entry.getKey()).getLeft();
            if (request.getValue() <= maxEGPPrice) {
                Decision decision = decisions.get(entry.getKey());
                int requestedEGP = request.getLeft();
                if (availableEGPCount >= requestedEGP) {
                    decision.setEGP(-requestedEGP + producedEGP);
                    availableEGPCount -= requestedEGP;
                } else if (availableEGPCount > 0) {
                    decision.setEGP(-availableEGPCount + producedEGP);
                    availableEGPCount = 0;
                } else break;
            } else break;
        }
    }

    private void makeESMDecision(Map<Integer, Decision> decisions) {
        Pair<Integer, Integer> ESMLevel = ESMLevels.get(level);
        int availableESMCount = ESMLevel.getKey();
        int minESMPrice = ESMLevel.getValue();
        for (Map.Entry<Integer, Pair<Integer, Integer>> entry : sellList.entrySet()) {
            Pair<Integer, Integer> request = entry.getValue();
            int producedEGP = produceEGPList.get(entry.getKey()).getLeft();
            if (request.getValue() >= minESMPrice) {
                Decision decision = decisions.get(entry.getKey());
                int requestedESM = request.getLeft();
                if (availableESMCount >= requestedESM) {
                    decision.setESM(requestedESM - producedEGP);
                    availableESMCount -= requestedESM;
                } else if (availableESMCount > 0) {
                    decision.setESM(availableESMCount - producedEGP);
                    availableESMCount = 0;
                } else break;
            } else break;
        }
    }

    private void makeMapsOrdered() {
        Map<Integer, Pair<Integer, Integer>> tmpMap;

        tmpMap = new TreeMap<>(new ResourceComparator(sellList).reversed());
        tmpMap.putAll(sellList);
        sellList = tmpMap;

        tmpMap = new TreeMap<>(new ResourceComparator(buyList));
        tmpMap.putAll(buyList);
        buyList = tmpMap;
    }

    private void setLevel() {
        sellList = new HashMap<>();
        buyList = new HashMap<>();
        buildFabricCountList = new HashMap<>();
        automateFabricCountList = new HashMap<>();
        buildAFabricCountList = new HashMap<>();
        loanCountList = new HashMap<>();
        produceEGPList = new HashMap<>();
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

    public Decision getDecision(int sessionId) {
        Decision decision = decisions.get(sessionId);
        decisions.remove(sessionId);
        if (decisions.size() <= 0) {
            setLevel();
        }
        return decision;
    }

    class ResourceComparator implements Comparator {
        Map<Integer, Pair<Integer, Integer>> base;

        public ResourceComparator(Map base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with
        // equals.
        public int compare(Object a, Object b) {
            if (base.get(a).getValue() >= base.get(b).getValue()) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }

        private class ESMComparator extends ResourceComparator {

            public ESMComparator(Map base) {
                super(base);
            }

            // Note: this comparator imposes orderings that are inconsistent with
            // equals.
            public int compare(Object a, Object b) {
                if (base.get(a).getValue() <= base.get(b).getValue()) {
                    return -1;
                } else {
                    return 1;
                } // returning 0 would merge keys
            }
        }

        @Override
        public Comparator reversed() {
            return new ESMComparator(base);
        }

        @Override
        public Comparator thenComparing(Comparator other) {
            return null;
        }

        @Override
        public Comparator thenComparingInt(ToIntFunction keyExtractor) {
            return null;
        }

        @Override
        public Comparator thenComparingLong(ToLongFunction keyExtractor) {
            return null;
        }

        @Override
        public Comparator thenComparingDouble(ToDoubleFunction keyExtractor) {
            return null;
        }

        @Override
        public Comparator thenComparing(Function keyExtractor) {
            return null;
        }

        @Override
        public Comparator thenComparing(Function keyExtractor, Comparator keyComparator) {
            return null;
        }
    }

    public static class BankHolder {
        private static Bank HOLDER_INSTANCE = new Bank();
    }

    public synchronized static Bank getInstance() {
        return BankHolder.HOLDER_INSTANCE;
    }
}
