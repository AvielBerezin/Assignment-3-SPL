#ifndef SENDER_H_
#define SENDER_H_

#include <iostream>
#include <boost/thread.hpp>
#include "ConnectionHandler.h"

class Sender {
private:
    ConnectionHandler * _connectionHandler;
    bool * _shouldTerminate;
    boost::mutex * _mutex;

public:
    Sender (ConnectionHandler * connectionHandler, bool * shouldTerminate, boost::mutex* mutex);

    void run();

    std::string getLineFromKeyboard();

    bool trySend(std::string lineMessage);
};

#endif
