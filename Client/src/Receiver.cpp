#include "Receiver.h"

Receiver::Receiver (ConnectionHandler * connectionHandler,
  bool * shouldTerminate,
  boost::mutex * mutex) :
  _connectionHandler(connectionHandler),
  _shouldTerminate(shouldTerminate),
  _mutex(mutex) {}

void Receiver::run() {
  while(!*_shouldTerminate) {
    *_shouldTerminate = !receiveMessage();
  }
}

bool Receiver::receiveMessage() {
  std::string response;
  if (!_connectionHandler->getLine(response)) {
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
