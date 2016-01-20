package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBConnector {
    private static Connection c = null;

    private static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:server.db");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }

    public static void createTablesIfNotExists() {
        Statement stmt = null;
        try {
            connect();
            String sql;
            try {
                stmt = c.createStatement();

                sql = "CREATE TABLE CLIENT " +
                        "(ID INTEGER PRIMARY KEY     AUTOINCREMENT," +
                        " NAME           TEXT    NOT NULL);";
                stmt.executeUpdate(sql);
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
            stmt.close();
            try {
                stmt = c.createStatement();
                sql = "CREATE TABLE GAME_MAP " +
                        "(ID INTEGER PRIMARY KEY     AUTOINCREMENT," +
                        " GAME_ID        INTEGER     NOT NULL," +
                        " CLIENT_ID      INTEGER     NOT NULL)";
                stmt.executeUpdate(sql);
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
            stmt.close();
            try {
                stmt = c.createStatement();
                sql = "CREATE TABLE GAME " +
                        "(ID INTEGER PRIMARY KEY     AUTOINCREMENT," +
                        " START_DATE     DATETIME DEFAULT CURRENT_TIMESTAMP)";
                stmt.executeUpdate(sql);
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
            stmt.close();
            try {
                stmt = c.createStatement();
                sql = "CREATE TABLE TURN " +
                        "(ID INTEGER PRIMARY KEY     AUTOINCREMENT," +
                        " CASH           INTEGER     NOT NULL," +
                        " ESM            INTEGER     NOT NULL," +
                        " EGP            INTEGER     NOT NULL," +
                        " LOAN           INTEGER             ," +
                        " FABRIC_COUNT   INTEGER     NOT NULL," +
                        " A_FABRIC_COUNT INTEGER     NOT NULL," +
                        " U_FABRIC_COUNT INTEGER     NOT NULL," +
                        " TURN           INTEGER     NOT NULL," +
                        " CLIENT_ID      INTEGER     NOT NULL," +
                        " GAME_ID        INTEGER     NOT NULL," +
                        " SAVE_ID        INTEGER             )";
                stmt.executeUpdate(sql);
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
            stmt.close();
            disconnect();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table created successfully");
    }

    public static Map<Integer, Map<String, Integer>> getCurrentClientsState(int gameId, int saveId) {
        Map<Integer, Map<String, Integer>> bigMap = new HashMap<>();
        connect();
        Statement stmt;
        int turn = 0;
        try {
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM TURN where GAME_ID = " + gameId +
                    " AND TURN = " + getCurrentTurn(gameId, saveId) +
                    (saveId > 0 ? " AND SAVE_ID =" + saveId : "") + ";");
            while (rs.next()) {
                Map<String, Integer> map = new HashMap<>();
                map.put("CASH", rs.getInt(1));
                map.put("ESM", rs.getInt(2));
                map.put("EGP", rs.getInt(3));
                map.put("LOAN", rs.getInt(4));
                map.put("FABRIC_COUNT", rs.getInt(5));
                map.put("A_FABRIC_COUNT", rs.getInt(6));
                map.put("U_FABRIC_COUNT", rs.getInt(7));
                bigMap.put(rs.getInt(0), map);
            }
            rs.close();
            stmt.close();

            disconnect();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");
        return bigMap;
    }

    private static int getCurrentTurn(int gameId, int saveId) {
        connect();
        Statement stmt;
        int turn = 0;
        try {
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(TURN) FROM TURN where GAME_ID = " + gameId +
                    (saveId > 0 ? " AND SAVE_ID =" + saveId : "") + ";");
            if (rs.first())
                turn = rs.getInt(0);
            else return 0;
            rs.close();
            stmt.close();

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");
        return turn;
    }


    private static void disconnect() throws SQLException {
        c.close();
    }

    public static void insertTurn(int cash, int esm, int egp, int clientId, int gameId, int saveId) {
        Statement stmt = null;
        try {
            connect();
            c.setAutoCommit(false);
            stmt = c.createStatement();
            int turn = getNextTurn(gameId, saveId);
            String sql = "INSERT INTO TURN (CASH,ESM,EGP,TURN,CLIENT_ID,GAME_ID,SAVE_ID) " +
                    "VALUES (" + cash + "," + esm + "," + egp + "," + turn + "," + clientId + "," + gameId + "," + saveId + " );";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            disconnect();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Records created successfully");
    }

    private static int getNextTurn(int gameId, int saveId) {
        connect();
        Statement stmt;
        int turn = 0;
        try {
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT MAX(TURN) FROM TURN where GAME_ID = "
                    + gameId + " AND SAVE_ID =" + saveId + ";");
            int max = 0;
            if (rs.first())
                max = rs.getInt(0);
            else return 0;
            rs.close();
            stmt.close();

            stmt = c.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM TURN where GAME_ID = " + gameId
                    + " AND TURN =" + max
                    + " AND SAVE_ID =" + saveId + ";");
            int count = 0;
            if (rs.first())
                count = rs.getInt(0);
            else return 0;
            rs.close();
            stmt.close();

            stmt = c.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM GAME_MAP where GAME_ID = " + gameId + ";");
            if (rs.first()) {
                if (rs.getInt(0) > count)
                    turn = max;
                else turn = max + 1;
            } else return 0;
            rs.close();
            stmt.close();
            disconnect();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");
        return turn;
    }


    public static int insertClient(String name) {
        Statement stmt = null;
        try {
            connect();
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "INSERT INTO CLIENT (NAME) " +
                    "VALUES (" + "'" + name + "'" + " );";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            disconnect();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Records created successfully");
        return getClientId(name);
    }

    public static void insertSave(String name, int gameId) {
        Statement stmt = null;
        try {
            connect();
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "INSERT INTO SAVE (GAME_ID,NAME) " +
                    "VALUES (" + gameId + "," + "'" + name + "'" + " );";
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            disconnect();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Records created successfully");
    }

    public static void insertGameMap(int clientId, int gameId) throws Exception {
        Statement stmt = null;
        if (getNumberOfClientInGame(gameId) < 4)
            try {
                connect();
                c.setAutoCommit(false);
                stmt = c.createStatement();
                String sql = "INSERT INTO GAME_MAP (GAME_ID,CLIENT_ID) " +
                        "VALUES (" + gameId + "," + clientId + " );";
                stmt.executeUpdate(sql);
                stmt.close();
                c.commit();
                disconnect();
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
        else throw new Exception("game.Game has 4 players already");
        System.out.println("Records created successfully");
    }

    public static void insertGame() {
        Statement stmt = null;
        try {
            connect();
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "INSERT INTO GAME (START_DATE) " +
                    "VALUES (datetime());";
            System.out.print(sql);
            stmt.executeUpdate(sql);
            stmt.close();
            c.commit();
            disconnect();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Records created successfully");
    }

    public static int getLastGame() {
        connect();
        Statement stmt;
        int i = -1;
        try {
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT max(ID) FROM GAME;");

            rs.next();

            i = rs.getInt("max(ID)");

            rs.close();
            stmt.close();
            disconnect();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");
        return i;
    }

    public static int getNumberOfClientInGame(int id) {
        connect();
        Statement stmt;
        int i = 0;
        try {
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ID FROM COMPANY where GAME_ID = " + id + ";");

            while (rs.next()) {
                i++;
            }
            rs.close();
            stmt.close();
            disconnect();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");
        return i;
    }

    public static int getClientId(String name) {
        connect();
        Statement stmt;
        int i = -1;
        try {
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ID FROM CLIENT where NAME = '" + name + "';");

            while (rs.next()) {
                i = rs.getInt("ID");
            }
            rs.close();
            stmt.close();
            disconnect();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");
        return i;
    }

    public static List<Integer> getClientsOfGame(int gameId) {
        List<Integer> clients = new ArrayList<>();
        connect();
        Statement stmt;
        int i = -1;
        try {
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT CLIENT_ID FROM CLIENT where GAME_ID = '" + gameId + "';");

            while (rs.next()) {
                clients.add(rs.getInt("CLIENT_ID"));
            }
            rs.close();
            stmt.close();
            disconnect();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return clients;
    }
}