package bgu.spl181.net.impl.echo;

import java.io.*;
import java.net.Socket;

public class EchoClient {

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            args = new String[]{"localhost", "hello"};
        }

        if (args.length < 2) {
            System.out.println("you must supply two arguments: host, message");
            System.exit(1);
        }

        client(args[0], 7777, args[1]);
    }

    private static void client(String serverHost, int serverPort, String msg) throws IOException {
        //BufferedReader and BufferedWriter automatically using UTF-8 encoding
        try (
                Socket sock = new Socket(serverHost, serverPort);
                BufferedReader in = getBufferedReader(sock);
                BufferedWriter out = getBufferedWriter(sock)
        ) {

            log("sending message to server");
            sendMsg(out, msg);

            log("awaiting response");
            String response = receiveResponse(in);
            log("message from server: " + response);
        }
    }

    private static void sendMsg(BufferedWriter out, String msg) throws IOException {
        out.write(msg);
        out.newLine();
        out.flush();
    }

    private static String receiveResponse(BufferedReader in) throws IOException {
        return in.readLine();
    }

    private static void log(String msg) {
        System.out.println(msg);
    }

    private static BufferedReader getBufferedReader(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    private static BufferedWriter getBufferedWriter(Socket socket) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

}
