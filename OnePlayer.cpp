#include "Board.h"
#include <chrono>
#include <iostream>

int main(int argc, char ** argv) {
    Board * b;

    auto start = std::chrono::high_resolution_clock::now();

    // 0th argument gets used as a seed for the random number generator.
    // If the seed is identical, the dice rolls will be identical.  To
    // have a surprise roll happen, use no arguments, and the current
    // time in milliseconds will be used as the seed.
    
    if (argc > 1) {
        b = new Board(atoi(argv[1]));
    }
    else {
        b = new Board();
    }
    std::cout << b->toString() << '\n';
    auto q = b->allWords();
    for (const auto& s : q) {
        std::cout << s << '\n';
    }
    std::cout << Board::countPoints(q) << " points\n"; 
    auto end = std::chrono::high_resolution_clock::now();
    std::cout << std::chrono::duration_cast<std::chrono::milliseconds>(
            end - start).count() / 1000.0 << " seconds\n";
    delete b;
    return 0;
}
