package socketServer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Message {
    private JsonObject jsonObject;

    public Message() {
        jsonObject = new JsonObject();
    }

    public void addMessage(String message) {
        jsonObject.addProperty("message", message);
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }

    public void addErrorDetails(JsonElement details) {
        jsonObject.add("details",details);
    }
}
