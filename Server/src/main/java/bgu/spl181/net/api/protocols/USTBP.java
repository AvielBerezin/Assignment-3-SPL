package bgu.spl181.net.api.protocols;

import bgu.spl181.net.api.bidi.BidiMessagingProtocol;
import bgu.spl181.net.api.bidi.Connections;
import bgu.spl181.net.data.DataBase;
import bgu.spl181.net.impl.ConnectionsImpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by avielber on 1/8/18.
 *
 * 2.1 Establishing a client/server connection
 * Upon connecting, a client must identify themselves to the system. In order to identify, a
 * user must be registered in the system. The LOGIN command is used to identiy. Any
 * command (except for REGISTER) used before the login is complete will be rejected by the
 * system.
 *
 * 2.2 Message encoding
 * A message is defined by a list of characters in UTF-8 encoding following the special
 * character ‘\n’. This is a very simple message encoding pattern that was seen in class.
 *
 * 2.3 Supported Commands
 * In the following section we will entail a list of commands supported by the User Service
 * Text-based protocol. Each of these commands will be sent independently within the
 * encoding defined in the previous section. (User examples appear at the end of the
 * assignment)
 *
 * Annotations:
 * • <x> – defines mandatory data to be sent with the command
 * • [x] – defines optional data to be sent with the command
 * • “x” – strings that allow a space or comma in complex commands will be wrapped
 * with quotation mark (more than a single argument)
 * • x,… - defines a variable list of arguments
 *
 * ------------------------------------- Server commands -------------------------------------
 * All ACK and ERROR message may be extended over the specifications, but the message
 * prefix must match the instructions (reminder: testing and grading are automatic).
 *
 * 1) ACK [message]
 * The acknowledge command is sent by the server to reply to a successful request by a
 * client. Specific cases are noted in the Client commands section.
 *
 * 2) ERROR <error message>
 * The error command is sent by the server to reply to a failed request. Specific cases are
 * noted in the Client commands section.
 *
 * 3) BROADCAST <message>
 * The broadcast command is sent by the server to all logged in clients. Specific cases are
 * noted in the Client commands section.
 * -------------------------------------------------------------------------------------------
 *
 *
 * ------------------------------------- Client commands -------------------------------------
 * 1) REGISTER <username> <password> [Data block,…]
 * Used to register a new user to the system.
 * • Username – The user name.
 * • Password – the password.
 * • Data Block – An optional block of additional information that may be used by the
 * service.
 * In case of failure, an ERROR command will be sent by the server: ERROR registration failed
 * Reasons for failure:
 * 1. The client performing the register call is already logged in.
 * 2. The username requested is already registered in the system.
 * 3. Missing info (username/password).
 * 4. Data block does not fit service requirements (defined in rental service section).
 * In case of successful registration an ACK command will be sent: ACK registration
 * succeeded
 *
 * 2) LOGIN <username> <password>
 * Used to login into the system.
 * • Username – The username.
 * • Password – The password.
 * In case of failure, an ERROR command will be sent by the server: ERROR login failed
 * Reasons for failure:
 * 1. Client performing LOGIN command already performed successful LOGIN
 * command.
 * 2. Username already logged in.
 * 3. Username and Password combination does not fit any user in the system.
 * In case of a successful login an ACK command will be sent: ACK login succeeded
 *
 * 3) SIGNOUT
 * Sign out from the server.
 * In case of failure, an ERROR command will be sent by the server: ERROR signout failed
 * Reasons for failure:
 * 1. Client not logged in.
 * In case of successful sign out an ACK command will be sent: ACK signout succeeded
 * After a successful ACK for sign out the client should terminate!
 *
 * 4) REQUEST <name> [parameters,…]
 * A general call to be used by clients. For example, our movie rental service will use it for
 * its applications. The next section will list all the supported requests.
 * • Name – The name of the service request.
 * • Parameters,.. – specific parameters for the request.
 * In case of a failure, an ERROR command will be sent by the server:
 * ERROR request <name> failed
 * Reasons for failure:
 * 1. Client not logged in.
 * 2. Error forced by service requirements (defined in rental service section).
 * In case of successful request an ACK command will be sent. Specific ACK messages are
 * listed on the service specifications.
 * -------------------------------------------------------------------------------------------
 *
 * User service text based protocol
 */
public abstract class USTBP implements BidiMessagingProtocol<String> {
    protected ConnectionsImpl<String> connections;
    protected int connectionId;
    protected boolean shouldTerminate = false;

    protected DataBase dataBase;

    public USTBP(DataBase dataBase) {
        this.dataBase = dataBase;
    }

    // -------------------- client commands identifiers -------------------
    public static final String REGISTER_COMMAND_IDENTIFIER = "REGISTER";
    public static final String LOGIN_COMMAND_IDENTIFIER = "LOGIN";
    public static final String SIGNOUT_COMMAND_IDENTIFIER = "SIGNOUT";
    public static final String REQUEST_COMMAND_IDENTIFIER = "REQUEST";

    // -------------------- server commands identifiers -------------------
    public static final String ACK_COMMAND_IDENTIFIER = "ACK";
    public static final String ERROR_COMMAND_IDENTIFIER = "ERROR";
    public static final String BROADCAST_COMMAND_IDENTIFIER = "BROADCAST";

    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.connections = (ConnectionsImpl<String>) connections;
        this.connectionId = connectionId;
    }

    @Override
    public void process(String message) {
        message = message.trim();
        String commandType = getIdentifier(message);

        Map<String, Consumer<String>> actions = new HashMap<>();

        actions.put(REGISTER_COMMAND_IDENTIFIER, this::register);
        actions.put(LOGIN_COMMAND_IDENTIFIER, this::login);
        actions.put(SIGNOUT_COMMAND_IDENTIFIER, this::signout);
        actions.put(REQUEST_COMMAND_IDENTIFIER, this::request);

        if (actions.keySet().contains(commandType)) {
            actions.get(commandType).accept(message);
        }
        else {
            reportError("invalid command");
        }
    }

    private String getIdentifier(String message) {
        String[] dividedBySpaces = message.split(" ");

        if (dividedBySpaces.length == 0) {
            return null;
        }

        return dividedBySpaces[0];
    }

    protected abstract void login(String message);
    protected abstract void register(String message);
    protected abstract void signout(String message);
    protected abstract void request(String message);

    protected void reportError(String errorMessage) {
        connections.send(connectionId, ERROR_COMMAND_IDENTIFIER + " " + errorMessage);
    }
    protected void acknowledge(String acknowledgeMessage) {
        connections.send(connectionId, ACK_COMMAND_IDENTIFIER + " " + acknowledgeMessage);
    }
    protected void broadcast(String broadcastMessage) {
        connections.broadcast(BROADCAST_COMMAND_IDENTIFIER + " " + broadcastMessage);
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
