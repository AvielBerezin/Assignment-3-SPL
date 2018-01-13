package bgu.spl181.net.impl.echo;

import java.io.*;
import java.net.Socket;

public class EchoClient {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            args = new String[]{"localhost", "7777"};
        }

        if (args.length != 2) {
            System.out.println("you must supply two arguments: host, port");
            System.exit(1);
        }

        final String hostAddress = args[0];
        final Integer port = Integer.parseInt(args[1]);
        final Socket socket = new Socket(hostAddress, port);

        final Thread outputToServer = new Thread(() -> {
            try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                while (!Thread.currentThread().isInterrupted()) {
                    String keyboardInput = readLine(System.in);
                    System.out.println(">" + keyboardInput + ">");
                    out.write(keyboardInput);
                    out.newLine();
                    out.flush();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
                Thread.currentThread().interrupt();
            }
        });


        final Thread inputFromServer = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                while (!Thread.currentThread().isInterrupted()) {
                    String line = in.readLine();
                    System.out.println("<" + line + "<");

                    if (line.equals("ACK signout success")) {
                        outputToServer.interrupt();
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
                Thread.currentThread().interrupt();
            }
        });

        outputToServer.start();
        inputFromServer.start();
    }

    private static String readLine(InputStream in) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            return bufferedReader.readLine();
        } catch (IOException ioException) {
            ioException.printStackTrace();
            Thread.currentThread().interrupt();
        }

        return "";
    }
}
