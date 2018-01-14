// #include <stdlib.h>
// #include "ConnectionHandler.h"
// #include "Receiver.cpp"
// #include "Sender.cpp"
//
// /**
// * This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
// *
// *
// * An echo client is provided, but its a single threaded client.
// * While it is blocking on stdin(read from keyboard) it does not read messages from the socket.
// * You should improve the client so that it will run 2 threads:
// * - One should read from keyboard
// * - The other should read from socket
// * Both threads may write to the socket.
// *
// * The client should receive the server’s IP and PORT as arguments.
// * You may assume a network disconnection does not happen (like disconnecting the network cable).
// *
// * The client should recive commands using the standard input.
// * Commands are defined in previous sections.
// *
// * The client should print to screen any message coming from the server (ACK’s, ERROR’s and BROADCAST’s).
// * Notice that the client should not close until it recives an ACK packet for the SIGNOUT call.
// *
// * The Client directory should contain a src, include and bin subdirectories and a Makefile as shown in class.
// * The output executable for the client is named BBclient and should reside in the bin folder after calling make.
// * Testing run commands: bin/BBclient <ip> <port>
// *
// */
//
// int main (int argc, char *argv[]) {
//     if (argc < 3) {
//         std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
//         return -1;
//     }
//     std::string host = argv[1];
//     short port = atoi(argv[2]);
//
//     ConnectionHandler connectionHandler(host, port);
//     if (!connectionHandler.connect()) {
//         std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
//         return 1;
//     }
//
//     boost::mutex mutex;
//     bool terminator = false;
//     Receiver receiver(&connectionHandler, &terminator, &mutex);
//     Sender sender(&connectionHandler, &terminator, &mutex);
//
//     boost::thread th1(&Receiver::run, &receiver);
//     boost::thread th2(&Sender::run, &sender);
//     th1.join();
//     th2.join();
//
//     return 0;
//
// }


// #include <iostream>
// #include <boost/thread.hpp>
//
// class Task{
// private:
//     int _id;
//     boost::mutex * _mutex;
// public:
//     Task (int id, boost::mutex * mutex) : _id(id), _mutex(mutex) {}
//
//     void run(){
//         for (int i= 0; i < 10; i++){
//             boost::mutex::scoped_lock lock(*_mutex);
//             std::cout << i << ") Task " << _id << " is working" << std::endl;
//         }
//     }
// };
//
// int main(){
//     boost::mutex mutex;
//     Task task1(1, &mutex);
//     Task task2(2, &mutex);
//
//     boost::thread th1(&Task::run, &task1);
//     boost::thread th2(&Task::run, &task2);
//     th1.join();
//     th2.join();
//     return 0;
// }


#include <stdlib.h>
#include "ConnectionHandler.h"

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
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

    //From here we will see the rest of the ehco client implementation:
    while (1) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        int len=line.length();
        if (!connectionHandler.sendLine(line)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        // connectionHandler.sendLine(line) appends '\n' to the message. Therefor we send len+1 bytes.
        std::cout << "Sent " << len+1 << " bytes to server" << std::endl;


        // We can use one of three options to read data from the server:
        // 1. Read a fixed number of characters
        // 2. Read a line (up to the newline character using the getline() buffered reader
        // 3. Read up to the null character
        std::string answer;
        // Get back an answer: by using the expected number of bytes (len bytes + newline delimiter)
        // We could also use: connectionHandler.getline(answer) and then get the answer without the newline char at the end
        if (!connectionHandler.getLine(answer)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }

        len=answer.length();
        // A C string must end with a 0 char delimiter.  When we filled the answer buffer from the socket
        // we filled up to the \n char - we must make sure now that a 0 char is also present. So we truncate last character.
        answer.resize(len-1);
        std::cout << "Reply: " << answer << " " << len << " bytes " << std::endl << std::endl;
        if (answer == "bye") {
            std::cout << "Exiting...\n" << std::endl;
            break;
        }
    }
    return 0;
}
