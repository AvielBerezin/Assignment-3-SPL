#include <iostream>
#include <boost/thread.hpp>
#include "ConnectionHandler.h"
 
class Sender{
private:
    ConnectionHandler * _connectionHandler;
    bool * _shouldTerminate;
    boost::mutex * _mutex;
public:
    Sender (ConnectionHandler * connectionHandler, bool * shouldTerminate, boost::mutex* mutex) : _connectionHandler(connectionHandler), _shouldTerminate(shouldTerminate), _mutex(mutex) {}
    
    void operator()(){
        while(!_shouldTerminate) {
            *_shouldTerminate = !trySend(getLineFromKeyboard()); 
        }
    }

    std::string getLineFromKeyboard() {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        return std::string(buf);
    }

    bool trySend(std::string lineMessage) {
        int len = lineMessage.length();
        
        if (!_connectionHandler->sendLine(lineMessage)) {
            boost::mutex::scoped_lock lock(*_mutex);
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            return false;
        }

        {
            boost::mutex::scoped_lock lock(*_mutex);
            // _connectionHandler->sendLine appends '\n' to the message. Therefor we send len+1 bytes.
            std::cout << "Sent " << len+1 << " bytes to server" << std::endl;
        }

        return true;
    }
};