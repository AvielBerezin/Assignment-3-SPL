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

  std::string getLineFromKeyboard();
  bool sendMessage(std::string);

public:
  Sender (ConnectionHandler * connectionHandler,
    bool * shouldTerminate,
    boost::mutex * mutex);

  void run();
};

#endif
