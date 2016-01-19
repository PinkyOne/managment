package socketServer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Message {
    private JsonObject jsonObject;

    public MessageType getMessageType() {
        return messageType;
    }

    private void setMessageType(String messageType) {
        this.messageType = MessageType.getMessageType(messageType);
    }

    private MessageType messageType;

    public Message() {
        jsonObject = new JsonObject();
        setMessageType(null);
    }

    public Message(String input) {
        System.out.print(input);
        try {
            jsonObject = (JsonObject) new JsonParser().parse(input);
        } catch (Exception e) {
            jsonObject = new JsonObject();
            setMessageType("");
            return;
        }
        setMessageType(jsonObject.get("type").getAsString());
    }

    public void addMessage(String message) {
        jsonObject.addProperty("message", message);
    }

    public void addProperty(String property, String value) {
        jsonObject.addProperty(property, value);
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }

    public void addErrorDetails(String details) {
        jsonObject.addProperty("errorDetails", details);
    }

    public Map<String, String> getGameDetails() {
        Set<Map.Entry<String, JsonElement>> details = jsonObject.get("gameDetails").getAsJsonObject().entrySet();
        Map<String, String> detailsAsMap = new HashMap<>();
        for (Map.Entry<String, JsonElement> detail : details) {
            detailsAsMap.put(detail.getKey(), detail.getValue().getAsJsonPrimitive().toString());
        }
        return detailsAsMap;
    }

    public String getString(String name) {
        return jsonObject.get(name).getAsString();
    }

    public int getInt(String name) {
        return jsonObject.get(name).getAsInt();
    }

    public JsonObject getObject(String prop) {
        return jsonObject.getAsJsonObject(prop);
    }

    public void addDecision(Decision decision) {

    }

    public enum MessageType {
        CONNECT("connect"), TURN("turn"), CREATE_GAME("create"), UNKNOWN("unknown"), GET_DECISION("shutdown");
        private final String type;

        MessageType(String type) {
            this.type = type;
        }

        public static MessageType getMessageType(String type) {
            switch (type) {
                case "connect": {
                    return CONNECT;
                }
                case "turn": {
                    return TURN;
                }
                case "create": {
                    return CREATE_GAME;
                }
                case "shutdown": {
                    return GET_DECISION;
                }
                default: {
                    return UNKNOWN;
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this.toString().equals(obj.toString());
    }
}
