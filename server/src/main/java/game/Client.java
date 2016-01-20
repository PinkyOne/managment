package game;

import database.DBConnector;

public class Client {
    private int id;
    private String name;

    public Client(String name) {
        this.id = DBConnector.getClientId(name);
        if (id < 0) id = DBConnector.insertClient(name);
    }

    public Client(int id) {
        this.name = DBConnector.getClientName(id);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
