#include "Sender.h"

Sender::Sender (ConnectionHandler * connectionHandler, bool * shouldTerminate, boost::mutex* mutex) : _connectionHandler(connectionHandler), _shouldTerminate(shouldTerminate), _mutex(mutex) {}

void Sender::run(){
    while(!_shouldTerminate) {
        *_shouldTerminate = !trySend(getLineFromKeyboard());
    }
}

std::string Sender::getLineFromKeyboard() {
    const short bufsize = 1024;
    char buf[bufsize];
    std::cin.getline(buf, bufsize);
    return std::string(buf);
}

bool Sender::trySend(std::string lineMessage) {
    int len = lineMessage.length();

    if (!_connectionHandler->sendLine(lineMessage)) {
        boost::mutex::scoped_lock lock(*_mutex);
        std::cout << "Disconnected. Exiting...\n" << std::endl;
        return false;
    }

    {
        boost::mutex::scoped_lock lock(*_mutex);
        std::cout << "Sent " << len+1 << " bytes to server" << std::endl;
    }

    return true;
}
