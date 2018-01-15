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
#include "Sender.h"
#include "Receiver.h"

std::string getLineFromStandartInput();
bool sendMessage(std::string, ConnectionHandler *);
bool receiveMessage(ConnectionHandler *);

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

  bool shouldTerminate = false;
  boost::mutex mutex;

  Sender sender(&connectionHandler, &shouldTerminate, &mutex);
  Receiver receiver(&connectionHandler, &shouldTerminate, &mutex);

  boost::thread senderThread(&Sender::run, &sender);
  boost::thread receiverThread(&Receiver::run, &receiver);

  senderThread.join();
  receiverThread.join();

  return 0;
}

std::string getLineFromStandartInput() {
  const short bufsize = 1024;
  char buf[bufsize];
  std::cin.getline(buf, bufsize);
  return std::string(buf);
}

bool sendMessage(std::string message, ConnectionHandler * connectionHandler) {
  if (!connectionHandler->sendLine(message)) {
      std::cout << "Disconnected. Exiting...\n" << std::endl;
      return false;
  }

  return true;
}

bool receiveMessage(ConnectionHandler * connectionHandler) {
  std::string response;
  if (!connectionHandler->getLine(response)) {
    std::cout << "Disconnected. Exiting...\n" << std::endl;
    return false;
  }

  response.resize(response.length() - 1);
  std::cout << response << std::endl << std::endl;
  if (response == "ACK signout succeeded") {
    std::cout << "Exiting...\n" << std::endl;
    return false;
  }

  return true;
}
