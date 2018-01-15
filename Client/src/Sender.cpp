#include "Sender.h"

Sender::Sender (ConnectionHandler * connectionHandler,
  bool * shouldTerminate,
  boost::mutex* mutex) :
  _connectionHandler(connectionHandler),
  _shouldTerminate(shouldTerminate),
  _mutex(mutex) {}

void Sender::run() {
  while(!*_shouldTerminate) {
    std::string message = getLineFromKeyboard();
    if (!*_shouldTerminate) {
      *_shouldTerminate = !sendMessage(message);
    }
  }
}

bool Sender::sendMessage(std::string message) {
  if (!_connectionHandler->sendLine(message)) {
      std::cout << "Disconnected. Exiting...\n" << std::endl;
      return false;
  }

  return true;
}

std::string Sender::getLineFromKeyboard() {
    const short bufsize = 1024;
    char buf[bufsize];
    std::cin.getline(buf, bufsize);
    return std::string(buf);
}
