package socketServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SocketClientHandler implements Runnable {

    private Socket client;

    public SocketClientHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            System.out.println("Thread started with name:" + Thread.currentThread().getName());
            readResponse();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void readResponse() throws IOException, InterruptedException {
        StringBuilder userInput = new StringBuilder();
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
        String tmp;
        if ((tmp = stdIn.readLine()) != null) {
            userInput.append(tmp);
        }
        ResponseProcessor.getInstance().processResponse(client, new Message(userInput.toString()));
    }
}
