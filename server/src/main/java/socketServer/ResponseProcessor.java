package socketServer;

import game.Client;
import game.Game;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;

public class ResponseProcessor {

    public void processResponse(Socket client, Message message) throws IOException {
        Message answer = process(message);

        sendAnswer(client, answer);
    }

    private synchronized Message process(Message message) {
        Message answer = new Message();
        switch (message.getMessageType()) {
            case CONNECT: {
                answer = connect(message);
                answer.addMessage("OK");
            }
            break;
            case CREATE_GAME: {
                answer = createGame(message);
                answer.addMessage("OK");
            }
            break;
            case TURN: {
                answer = makeTurn(message);
                answer.addMessage("OK");
            }
            break;
            default: {
                answer.addErrorDetails("Can not read message");
            }
            break;
        }
        return answer;
    }

    private synchronized Message makeTurn(Message message) {
        int sessionId = message.getInt("sessionId");
        int sell = message.getInt("sell");
        int buy = message.getInt("buy");
        int buildFabricCount = message.getInt("buildFabricCount");
        int automateFabricCount = message.getInt("automateFabricCount");
        int buildAFabricCount = message.getInt("buildAFabricCount");
        int loanCount = message.getInt("loanCount");
        Game.getInstance().makeTurn(sessionId, sell, buy, buildFabricCount, automateFabricCount, buildAFabricCount, loanCount);
        return message;
    }

    private synchronized Message createGame(Message message) {
        Map<String, String> details = message.getGameDetails();
        if (details != null) {
            Game.GameHolder.setGameId(Integer.parseInt(details.get("gameId")));
            try {
                Game.GameHolder.setSaveId(Integer.parseInt(details.get("saveId")));
            } catch (NumberFormatException e) {// ignore this, govnokod, i know
            }
        }
        Game game = Game.getInstance();

        Message answer = new Message();
        return answer;
    }

    private synchronized Message connect(Message message) {
        String name = message.getString("name");
        Game.getInstance().connectClient(new Client(name));
        return message;
    }

    public static class ResponseProcessorHolder {
        private static ResponseProcessor HOLDER_INSTANCE = new ResponseProcessor();
    }

    private void sendAnswer(Socket client, Message message) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        writer.write(message.toString());
        writer.flush();
        writer.close();
    }

    public static ResponseProcessor getInstance() {
        return ResponseProcessorHolder.HOLDER_INSTANCE;
    }
}