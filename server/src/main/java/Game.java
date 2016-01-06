public class Game {
    private Game(int gameId, int saveId) {
    }

    private Game(int gameId) {
    }

    private Game() {
    }

    public void save() {
    }

    private void load(int saveId) {
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

    public static Game getInstance() {
        return GameHolder.HOLDER_INSTANCE;
    }
}
