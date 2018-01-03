package bgu.spl181.net.impl.GettingUsedTo.Sockets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Function;

/**
 * Created by avielber on 1/3/18.
 */
public class IDK {
    public static void main(String[] args) {
        Catcher catcher = e->log(e.getMessage());
        Function<Thrower, Runnable> logExceptions = (Thrower thrower)->exceptionsHandler(thrower, catcher);
        Runnable server = logExceptions.apply(IDK::runServer);

        server.run();
    }

    public static class Client {
        public static void main(String[] args) {
            Catcher catcher = e->log(e.getMessage());
            Function<Thrower, Runnable> logExceptions = (Thrower thrower)->exceptionsHandler(thrower, catcher);
            Runnable client = logExceptions.apply(IDK::runClient);

            client.run();
        }
    }

    private static Runnable exceptionsHandler(Thrower thrower, Catcher catcher) {
        return () -> {
            try {
                thrower.run();
            }
            catch (Exception e) {
                catcher.run(e);
            }
        };
    }

    private static void log(String message) {
        System.out.println(message);
    }

    private static void runServer() throws Exception {
        ServerSocket server = new ServerSocket(1234);

        Socket connection = server.accept();

        BufferedReader reader = getReader(connection);
        receive(reader, "server");

        BufferedWriter writer = getWriter(connection);
        send(writer, "heard you", "responding", "server");
    }

    private static Runnable concurrent(Runnable proccess) {
        return () -> new Thread(proccess) .run();
    }

    private static String receive(BufferedReader reader, String receiver) throws Exception {
        String received = reader.readLine();
        log("["+receiver+"] received:" + received);
        return received;
    }

    private static BufferedReader getReader(Socket connection) throws Exception {
        return new BufferedReader(new InputStreamReader(connection.getInputStream()));
    }

    private static BufferedWriter getWriter(Socket connection) throws Exception {
        return new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
    }

    private static void runClient() throws Exception {
        String targetHost = "localhost";
        int targetPort = 1234;
        Socket connection = new Socket(targetHost, targetPort);

        send(getWriter(connection), "Fuck you!", "sending", "client");

        receive(getReader(connection), "client");
    }

    private static void send(BufferedWriter writer, String message, String action, String sender) throws Exception {
        log("["+sender+"] "+action+": " + message);

        writer.write(message);
        writer.newLine();
        writer.flush();
    }


    interface Thrower {
        void run() throws Exception;
    }

    interface Catcher {
        void run(Exception e);
    }
}
