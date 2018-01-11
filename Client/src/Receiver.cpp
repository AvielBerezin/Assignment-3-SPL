#include <iostream>
#include <boost/thread.hpp>
#include "ConnectionHandler.h"

using namespace std;
 
class Receiver{
private:
    ConnectionHandler * _connectionHandler;
    bool * _shouldTerminate;
    boost::mutex * _mutex;
public:
    Receiver (ConnectionHandler * connectionHandler, bool * shouldTerminate, boost::mutex * mutex) : _connectionHandler(connectionHandler), _shouldTerminate(shouldTerminate), _mutex(mutex) {}
    
    void operator()(){
        while(!_shouldTerminate) {
            *_shouldTerminate = !tryReceive();
        }
    }

    bool tryReceive() {
        /*
        // We can use one of three options to read data from the server:
        // 1. Read a fixed number of characters
        // 2. Read a line (up to the newline character using the getline() buffered reader
        // 3. Read up to the null character
        */
        std::string responseFromServer;
        /*
        // Get back an answer: by using the expected number of bytes (len bytes + newline delimiter)
        // We could also use: _connectionHandler.getline(answer) and
        // then get the answer without the newline char at the end
        */
        if (!_connectionHandler->getLine(responseFromServer)) {
            boost::mutex::scoped_lock lock(*_mutex);
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            return false;
        }
        
        int len = responseFromServer.length();
        /*
        // A C string must end with a 0 char delimiter.
        // When we filled the answer buffer from the socket we filled up to the \n char -
        // we must make sure now that a 0 char is also present.
        // So we truncate last character.
        */
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

};