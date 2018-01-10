package bgu.spl181.net.srv;

import bgu.spl181.net.api.users.User;
import bgu.spl181.net.srv.bidi.ConnectionHandler;
import bgu.spl181.net.api.MessageEncoderDecoder;
import bgu.spl181.net.api.bidi.BidiMessagingProtocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;


public class BlockingConnectionHandler<T> implements Runnable, bgu.spl181.net.srv.bidi.ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket socket;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;


    private Queue<T> messages;

    public BlockingConnectionHandler(Socket socket, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol) {
        this.socket = socket;
        this.encdec = reader;
        this.protocol = protocol;

        messages = new LinkedList<>();
    }

    @Override
    public void run() {
        try (Socket socket = this.socket) { //just for automatic closing
            int read;

            in = new BufferedInputStream(socket.getInputStream());
            out = new BufferedOutputStream(socket.getOutputStream());
            while (!protocol.shouldTerminate() && connected) {
                if (!messages.isEmpty()) {
                    out.write(encdec.encode(messages.poll()));
                    out.flush();
                }
                else {
                    // I assume things because I don't actually know how they work. Here is an assumption of mine:
                    // "in.read()" returns -1 when end of buffer is reached or *when input wasn't received yet.*
                    if ((read = in.read()) >= 0) {
                        T messageReceivedByClient = encdec.decodeNextByte((byte)read);
                        protocol.process(messageReceivedByClient);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        socket.close();
    }

    @Override
    public void send(T message) {
        messages.add(message);
    }
}
