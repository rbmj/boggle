CFLAGS=-O3

clang++ $CFLAGS -c -std=c++11 Trie.cpp -o Trie.o
clang++ $CFLAGS -c -std=c++11 Board.cpp -o Board.o
g++ $CFLAGS -c -std=c++0x OnePlayer.cpp -o OnePlayer.o
g++ $CFLAGS Trie.o Board.o OnePlayer.o -o OnePlayer

