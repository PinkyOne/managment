package socketServer;

import java.io.*;
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
        while ((tmp = stdIn.readLine()) != null) {
            userInput.append(tmp);
        }
    }

    public void sendOK() throws IOException, InterruptedException {
        Message okMessage = new Message();
        okMessage.addMessage("OK");
        sendAnswer(okMessage);
    }

    public void sendError() throws IOException, InterruptedException {
        Message errorMessage = new Message();
        errorMessage.addMessage("ERROR");
        errorMessage.addErrorDetails(null);
        sendAnswer(errorMessage);
    }

    private void sendAnswer(Message message) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        writer.write(message.toString());
        writer.flush();
        writer.close();
    }

}
