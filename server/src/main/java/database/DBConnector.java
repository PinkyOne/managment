package database;

import java.sql.*;

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
            stmt = c.createStatement();
            String sql = "CREATE TABLE IF NOT EXIST CLIENT " +
                    "(ID INT PRIMARY KEY     AUTOINCREMENT," +
                    " NAME           TEXT    NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TABLE IF NOT EXIST GAME_MAP " +
                    "(ID INT PRIMARY KEY     AUTOINCREMENT," +
                    " GAME_ID        INT     NOT NULL," +
                    " CLIENT_ID      INT     NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TABLE IF NOT EXIST GAME " +
                    "(ID INT PRIMARY KEY     AUTOINCREMENT," +
                    " START_DATE     DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = c.createStatement();
            sql = "CREATE TABLE IF NOT EXIST TURN " +
                    "(ID INT PRIMARY KEY     AUTOINCREMENT," +
                    " CASH           INT     NOT NULL," +
                    " ESM            INT     NOT NULL," +
                    " EGP            INT     NOT NULL," +
                    " TURN           INT     NOT NULL," +
                    " CLIENT_ID      INT     NOT NULL," +
                    " GAME_ID        INT     NOT NULL," +
                    " SAVE_ID        INT     NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
            disconnect();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table created successfully");
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
                    + gameId + "AND SAVE_ID =" + saveId + ";");
            int max = 0;
            if (rs.first())
                max = rs.getInt(0);
            else return 0;
            rs.close();
            stmt.close();

            stmt = c.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM TURN where GAME_ID = " + gameId
                    + "AND TURN =" + max
                    + "AND SAVE_ID =" + saveId + ";");
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
        else throw new Exception("Game has 4 players already");
        System.out.println("Records created successfully");
    }

    public static void insertGame() {
        Statement stmt = null;
        try {
            connect();
            c.setAutoCommit(false);
            stmt = c.createStatement();
            String sql = "INSERT INTO GAME (START_DATE) " +
                    "VALUES (" + null + " );";
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
}