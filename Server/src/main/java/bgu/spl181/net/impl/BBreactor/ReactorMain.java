package bgu.spl181.net.impl.BBreactor;

import bgu.spl181.net.api.protocols.MRSP;
import bgu.spl181.net.data.DataBase;
import bgu.spl181.net.impl.LineMessageEncoderDecoder;
import bgu.spl181.net.srv.Server;

public class ReactorMain {
    public static void main(String[] args) {
        args = args.length == 0 ? new String[] {"7777"} : args;

        DataBase dataBase = new DataBase();

        Server<String> server = Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                Integer.parseInt(args[0]),
                ()->new MRSP(dataBase),
                LineMessageEncoderDecoder::new);

        server.serve();
    }
}
