package bgu.spl181.net.impl;

import bgu.spl181.net.api.bidi.Connections;
import bgu.spl181.net.data.DataBase;
import bgu.spl181.net.srv.bidi.ConnectionHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by avielber on 1/8/18.
 */
public class ConnectionsImpl<T> implements Connections<T> {

    private Map<Integer, ConnectionHandler<T>> connections;

    public ConnectionsImpl() {
        connections = new HashMap<Integer, ConnectionHandler<T>>();
    }

    public boolean send(int connectionId, T msg) {
        connections.get(connectionId).send(msg);
        return true;
    }

    public void broadcast(T msg) {
        connections.values().forEach(conn->conn.send(msg));
    }

    public void disconnect(int connectionId) {
        try {
            connections.remove(connectionId).close();
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addConnection(int connectionId, ConnectionHandler<T> connectionHandler) {
        connections.put(connectionId, connectionHandler);
    }



}
