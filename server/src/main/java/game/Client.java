package game;

import database.DBConnector;

public class Client {
    private int id;
    private String name;

    public Client(String name) {
        this.id = DBConnector.getClientId(name);
        if (id < 0) id = DBConnector.insertClient(name);
    }

    public Client(int sessionId) {

    }

    public int getId() {
        return id;
    }
}
