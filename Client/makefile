CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lboost_locale -lboost_thread

all: EchoClient
	g++ -o bin/BBclient bin/connectionHandler.o bin/Boost_Echo_Client.o $(LDFLAGS)

EchoClient: bin/connectionHandler.o bin/echoClient.o
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/echoClient.o: src/Boost_Echo_Client.cpp
	g++ $(CFLAGS) -o bin/Boost_Echo_Client.o src/Boost_Echo_Client.cpp
	
.PHONY: clean
clean:
	rm -f bin/*
