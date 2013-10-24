#ifndef BOARD_H_INC
#define BOARD_H_INC

#include <string>
#include <vector>
#include "Trie.h"

class Board {
private:
    char board[5][5];
    Trie englishWords;

public:
  /**
   * Constructs the board, using the current time as the Random Number
   * Generator's seed.
   */
    Board();

  /**
   * Construct the board, creating one of your data structure, and filling
   * it with the words in the English dictionary.
   *
   * Prints a stack trace if the file "american-english" is not in the
   * directory.
   * @param seed the seed for the random object; running this with identical
   * seeds should get you identical dice rolls and placement.
   */
    Board(long seed);


  /**
   * String representation of the Board.
   */
    std::string toString();

  /**
   * Given a Queue full of Strings, returns the number of points that list
   * of Strings receives in a game of Boggle.
   */
    static int countPoints(std::vector<std::string>& q);
    
  /**
   * A method that returns a Queue filled with every word that appears in 
   * the Boggle board. 
   */
    std::vector<std::string> allWords();

private:

  /**
   * Used to roll the dice, and fill the Boggle board with the results.
   * Based on actual Boggle dice.
   */
    void newGame(long seed);

  /** The recursive version of the above.
   * @param si          A search iterator representing our current position
   *                    in the dictionary
   * @param r           The row of the current node
   * @param c           The column of the current node
   * @param used        An array representing which nodes are already
   *                    visited in this string
   * @param foundwords  A set to insert all of the found words into
   *
   * Implementation note:  Because we use Trie.SearchIterator, we're locked
   * in to using Tries to hold the dictionary, we can't just use a Set. 
   * However, foundwords can easily be a hashtable or an AVL tree -
   * this may even be faster.
   * 
   * The biggest win from Tries comes from knowing early in the string
   * whether we are on a 'dead' path, since a Trie can tell us if the 
   * current string is the prefix of at least one string in englishWords().
   * This makes us orders of magnitude faster than a hash table, even 
   * though search is O(1) in both structures, ESPECIALLY when we don't 
   * stop searching at strings of length 8...
   */
    void allWords(Trie::SearchIterator si, int r, int c,
                  bool (&used)[5][5], Trie& foundwords);
};

#endif
