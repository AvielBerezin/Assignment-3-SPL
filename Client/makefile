CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lboost_thread -lboost_locale

all: BBclient

BBclient: bin/BBclient.o bin/ConnectionHandler.o bin/Sender.o bin/Receiver.o
	g++ -o bin/BBclient bin/BBclient.o bin/ConnectionHandler.o bin/Sender.o bin/Receiver.o $(LDFLAGS)

bin/BBclient.o: src/BBclient.cpp
	g++ $(CFLAGS) -o bin/BBclient.o src/BBclient.cpp

bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp

bin/Sender.o: src/Sender.cpp
	g++ $(CFLAGS) -o bin/Sender.o src/Sender.cpp

bin/Receiver.o: src/Receiver.cpp
	g++ $(CFLAGS) -o bin/Receiver.o src/Receiver.cpp

.PHONY: clean
clean:
	rm -f bin/*
