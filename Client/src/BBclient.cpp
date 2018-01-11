#include <stdlib.h>
#include "ConnectionHandler.h"
#include "Receiver.cpp"
#include "Sender.cpp"
 
/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
* 
* 
* An echo client is provided, but its a single threaded client.
* While it is blocking on stdin(read from keyboard) it does not read messages from the socket.
* You should improve the client so that it will run 2 threads:
* - One should read from keyboard
* - The other should read from socket
* Both threads may write to the socket.
* 
* The client should receive the server’s IP and PORT as arguments.
* You may assume a network disconnection does not happen (like disconnecting the network cable).
* 
* The client should recive commands using the standard input.
* Commands are defined in previous sections.
* 
* The client should print to screen any message coming from the server (ACK’s, ERROR’s and BROADCAST’s).
* Notice that the client should not close until it recives an ACK packet for the SIGNOUT call.
* 
* The Client directory should contain a src, include and bin subdirectories and a Makefile as shown in class.
* The output executable for the client is named BBclient and should reside in the bin folder after calling make.
* Testing run commands: bin/BBclient <ip> <port>
* 
*/
int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    
    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    
    boost::mutex mutex;
    bool terminator = false;
    Receiver task1(&connectionHandler, &terminator, &mutex);
    Sender task2(&connectionHandler, &terminator, &mutex);
    
    boost::thread th1(task1); 
    boost::thread th2(task2); 
    th1.join();
    th2.join();
    
    return 0;
    
}






