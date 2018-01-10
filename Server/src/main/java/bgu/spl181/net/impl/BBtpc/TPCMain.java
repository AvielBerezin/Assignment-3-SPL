package bgu.spl181.net.impl.BBtpc;

import bgu.spl181.net.api.protocols.MovieRentalServeiceProtocol;
import bgu.spl181.net.data.DataBase;
import bgu.spl181.net.impl.echo.LineMessageEncoderDecoder;
import bgu.spl181.net.srv.Server;

import java.util.function.Supplier;

/**
 * Created by yossi on 1/10/2018.
 */
public class TPCMain {
    public static void main(String[] args) {
        args = new String[] {"7777"};

        DataBase dataBase = new DataBase();

        Server<String> server = Server.threadPerClient(Integer.parseInt(args[0]),
                ()->new MovieRentalServeiceProtocol(dataBase),
                LineMessageEncoderDecoder::new);

        server.serve();
    }
}
