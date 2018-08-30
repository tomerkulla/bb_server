package bgu.spl181.net.impl.BBtpc;

import bgu.spl181.net.api.BlockBuster.BBProtocol;
import bgu.spl181.net.api.BlockBuster.BBServerUsers;
import bgu.spl181.net.api.BlockBuster.MovieDataBase;
import bgu.spl181.net.api.USTP.ServerUsers;
import bgu.spl181.net.api.bidi.BidiMessagingProtocol;
import bgu.spl181.net.api.bidi.LineMessageEncoderDecoder;
import bgu.spl181.net.api.bidi.Server;

public class TPCMain {

    public static void main(String[] args) {
        ServerUsers users = new BBServerUsers("Database/Users.json");
        MovieDataBase movies = new MovieDataBase( "Database/Movies.json");

        Server.threadPerClient(Integer.parseInt(args[0]), ()-> (BidiMessagingProtocol)(new BBProtocol(users,movies)),
                  LineMessageEncoderDecoder::new ).serve();
    }
}
