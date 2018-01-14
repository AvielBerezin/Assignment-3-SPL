#ifndef RECEIVER_H_
#define RECEIVER_H_

#include <iostream>
#include <boost/thread.hpp>
#include "ConnectionHandler.h"

class Receiver{
private:
    ConnectionHandler * _connectionHandler;
    bool * _shouldTerminate;
    boost::mutex * _mutex;
public:
    Receiver (ConnectionHandler * connectionHandler,
      bool * shouldTerminate,
      boost::mutex * mutex);

    void run();

    bool tryReceive();

};

#endif
