#include "Receiver.h"

Receiver::Receiver (ConnectionHandler * connectionHandler,
  bool * shouldTerminate,
  boost::mutex * mutex) :
  _connectionHandler(connectionHandler),
  _shouldTerminate(shouldTerminate),
  _mutex(mutex) {}

void Receiver::run(){
    while(!_shouldTerminate) {
        *_shouldTerminate = !tryReceive();
    }
}

bool Receiver::tryReceive() {

    std::string responseFromServer;
    if (!_connectionHandler->getLine(responseFromServer)) {
        boost::mutex::scoped_lock lock(*_mutex);
        std::cout << "Disconnected. Exiting...\n" << std::endl;
        return false;
    }

    int len = responseFromServer.length();
    responseFromServer.resize(len-1);
    {
        boost::mutex::scoped_lock lock(*_mutex);
        std::cout << "Reply: " << responseFromServer << " " << len << " bytes " << std::endl << std::endl;
    }

    if (responseFromServer == "ACK signout") {
        boost::mutex::scoped_lock lock(*_mutex);
        std::cout << "Exiting...\n" << std::endl;
        return false;
    }

    return true;
}
