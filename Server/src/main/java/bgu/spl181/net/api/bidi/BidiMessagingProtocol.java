/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.spl181.net.api.bidi;

/**
 *
 * @author bennyl
 */
public interface BidiMessagingProtocol<T>  {

    /**
     * Assign relevant connections to protocol.
     * Assign connectionId as the connection id of the connection using this protocol.
     * @param connectionId an id of the connection using this protocol.
     * @param connections the connections which are relevant to the connection using this protocol.
     */
    void start(int connectionId, Connections<T> connections);

    /**
     * The method do things according to the protocol.
     *
     * ------------------------ For example ------------------------
     * If client request's to make a change to the database,
     * the method may verify the request and make the change.
     * Then the method can sent a broadcast telling all connections
     * about that change.
     * -------------------------------------------------------------
     *
     * @param message message received by client.
     */
    void process(T message);
	
	/**
     * @return true if the connection should be terminated
     */
    boolean shouldTerminate();
}
