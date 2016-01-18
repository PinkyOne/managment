package game;

import database.DBConnector;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import socketServer.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Game {
    private final int gameId;
    private int saveId;
    private Map<Integer, Client> clients;
    private List<Integer> dbClientIds;

    public Client getClient(int sessionId) throws NullPointerException {
        return clients.get(sessionId);
    }

    private Game(int gameId, int saveId) {
        this.gameId = gameId;
        this.saveId = saveId;
        clients = new HashMap<>();
        dbClientIds = DBConnector.getClientsOfGame(gameId);
    }

    private Game(int gameId) {
        this.gameId = gameId;
        saveId = -1;
        clients = new HashMap<>();
        dbClientIds = DBConnector.getClientsOfGame(gameId);
    }

    private Game() {
        DBConnector.insertGame();
        saveId = -1;
        gameId = DBConnector.getLastGame();
        clients = new HashMap<>();
    }

    public void save(String name) {
        DBConnector.insertSave(name, gameId);
    }

    private void load(int saveId) {
    }

    public synchronized Message connectClient(Client client) {
        Message message = new Message();
        if (dbClientIds != null) {
            boolean isClientFound = false;
            for (int id : dbClientIds) {
                if (client.getId() == id) {
                    isClientFound = true;
                    break;
                }
            }
            if (!isClientFound) {
                message.addErrorDetails("You can not connect to this game");
                return message;
            }
        }
        if (clients.get(client) == null) {
            if (clients.size() < 4) {
                Integer sessionId = new Random().nextInt();
                clients.put(sessionId, client);
                message.addProperty("sessionId", String.valueOf(sessionId));
            } else {
                message.addErrorDetails("This game doesn't have more places");
            }
        } else {
            message.addErrorDetails("game.Client with this name already connected");
        }
        return message;
    }

    public synchronized void makeTurn(
            int sessionId,
            Pair<Integer, Integer> sell,
            Pair<Integer, Integer> buy,
            Pair<Integer, Integer> produceEGP,
            int buildFabricCount,
            int automateFabricCount,
            int buildAFabricCount,
            int loanCount) {
        for (int i = 0; ; i++)
            try {
                Bank.getInstance().collectTurnRequest(sessionId, sell, buy, produceEGP, buildFabricCount, automateFabricCount, buildAFabricCount, loanCount);
            } catch (Exception e) {
                if (i > 10) break;
            }
    }

    public synchronized void makeTurn(
            int sessionId,
            Pair<Integer, Integer> sell,
            Pair<Integer, Integer> buy,
            Pair<Integer, Integer> produceEGP,
            int buildFabricCount,
            int automateFabricCount,
            int buildAFabricCount) {
        makeTurn(sessionId, sell, buy, produceEGP, buildFabricCount, automateFabricCount, buildAFabricCount, 0);
    }

    public synchronized void makeTurn(
            int sessionId,
            Pair<Integer, Integer> sell,
            Pair<Integer, Integer> buy,
            Pair<Integer, Integer> produceEGP) {
        makeTurn(sessionId, sell, buy, produceEGP, 0, 0, 0);
    }

    public synchronized void makeTurn(int sessionId) {
        makeTurn(sessionId, new ImmutablePair<>(0, 0), new ImmutablePair<>(0, 0), new ImmutablePair<>(0, 0));
    }

    public int getSaveId() {
        return saveId;
    }

    public int getGameId() {
        return gameId;
    }

    public int getClientSessionId(int key) {
        int id = -1;
        for (Map.Entry<Integer, Client> entry : clients.entrySet()) {
            if (entry.getValue().getId() == key) {
                id = entry.getKey();
                break;
            }
        }
        return id;
    }

    public static class GameHolder {
        public static void setSaveId(int saveId) {
            GameHolder.saveId = saveId;
        }

        public static void setGameId(int gameId) {
            GameHolder.gameId = gameId;
        }

        private static int saveId = -1;
        private static int gameId = -1;
        private static Game HOLDER_INSTANCE =
                saveId > -1 && gameId > -1
                        ? new Game(gameId, saveId)
                        : gameId > -1
                        ? new Game(gameId)
                        : new Game();
    }

    public synchronized static Game getInstance() {
        return GameHolder.HOLDER_INSTANCE;
    }
}
