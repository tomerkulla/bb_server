#include <stdlib.h>
//#include <connectionHandler.h>
#include <iostream>
#include "../include/connectionHandler.h"
#include <boost/thread.hpp>

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/

bool disconnect(false);
bool disconnectAns(false);

class ReadFromServer {
private:
    ConnectionHandler& connectionHandler;
public:
    ReadFromServer(ConnectionHandler& connectionHandler) : connectionHandler(connectionHandler) {}

    void operator()(){
        int len;
        while(!disconnect) {
            std::string answer;
            if (!connectionHandler.getLine(answer)) {
                std::cout << "Disconnected. Exiting...\n" << std::endl;
                disconnect =true;
                break;
            }
            len=answer.length();
            answer.resize(len-1);
            std::cout << answer << " " << std::endl;
            if (answer == "ACK signout succeeded") {
                disconnect = true;
                disconnectAns = true;
                break;
            }
            if (answer == "ERROR signout failed")
                disconnectAns = true;
        }
        }
    };


int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1]; //!!!!!!!
    short port = atoi(argv[2]); //!!!!!
    
    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    ReadFromServer read(connectionHandler);
    boost::thread th2(read);
    const short bufsize = 1024;
    while (!disconnect) {
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        if (!connectionHandler.sendLine(line)) {
            std::cout << "Disconnected. Exiting..." << std::endl;
            break;
        }
        if(line == "SIGNOUT") {
            disconnectAns = false;
            while(!disconnectAns);
        }
    }
    th2.join();
    return 0;
}
