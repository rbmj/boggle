import java.util.*;
import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Board {
    private char[][] board;
    private Trie englishWords;

  /**
   * Constructs the board, using the current time as the Random Number
   * Generator's seed.
   */
    public Board() {
        this(System.currentTimeMillis());
    }

  /**
   * Construct the board, creating one of your data structure, and filling
   * it with the words in the English dictionary.
   *
   * Prints a stack trace if the file "american-english" is not in the
   * directory.
   * @param seed the seed for the random object; running this with identical
   * seeds should get you identical dice rolls and placement.
   */
    public Board(long seed) {
        newGame(seed);
        englishWords = new Trie();
        try {
            Scanner s = new Scanner(new File("american-english"));
            while (s.hasNext()) {
                englishWords.insert(s.next().toUpperCase());
            }
            s.close();
        }
        catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
    }

  /**
   * Used to roll the dice, and fill the Boggle board with the results.
   * Based on actual Boggle dice.
   */
    private void newGame(long seed) {
        board = new char[5][5];
        Random r = new Random(seed);
        char[] dieRolls = new char[25];

        dieRolls[0] = new String("AAAFRS").charAt(r.nextInt(6));
        dieRolls[1] = new String("AAEEEE").charAt(r.nextInt(6));
        dieRolls[2] = new String("AAFIRS").charAt(r.nextInt(6));
        dieRolls[3] = new String("ADENNN").charAt(r.nextInt(6));
        dieRolls[4] = new String("AEEEEM").charAt(r.nextInt(6));
        dieRolls[5] = new String("AEEGMU").charAt(r.nextInt(6));
        dieRolls[6] = new String("AEGMNN").charAt(r.nextInt(6));
        dieRolls[7] = new String("AFIRSY").charAt(r.nextInt(6));
        dieRolls[8] = new String("BJKQXZ").charAt(r.nextInt(6));
        dieRolls[9] = new String("CCENST").charAt(r.nextInt(6));
        dieRolls[10] = new String("CEIILT").charAt(r.nextInt(6));
        dieRolls[11] = new String("CEILPT").charAt(r.nextInt(6));
        dieRolls[12] = new String("CEIPST").charAt(r.nextInt(6));
        dieRolls[13] = new String("DDHNOT").charAt(r.nextInt(6));
        dieRolls[14] = new String("DHHLOR").charAt(r.nextInt(6));
        dieRolls[15] = new String("DHLNOR").charAt(r.nextInt(6));
        dieRolls[16] = new String("DHLNOR").charAt(r.nextInt(6));
        dieRolls[17] = new String("EIIITT").charAt(r.nextInt(6));
        dieRolls[18] = new String("EMOTTT").charAt(r.nextInt(6));
        dieRolls[19] = new String("ENSSSU").charAt(r.nextInt(6));
        dieRolls[20] = new String("FIPRSY").charAt(r.nextInt(6));
        dieRolls[21] = new String("GORRVW").charAt(r.nextInt(6));
        dieRolls[22] = new String("IPRRRY").charAt(r.nextInt(6));
        dieRolls[23] = new String("NOOTUW").charAt(r.nextInt(6));
        dieRolls[24] = new String("OOOTTU").charAt(r.nextInt(6));
        List<Integer> permute = new LinkedList<Integer>();
        for (int i = 0; i < 25; i++) {
            permute.add(new Integer(i));
        }
        Collections.shuffle(permute, r);
        for (int row = 0; row < 5; row++) {
            for (int c = 0; c < 5; c++) {
                int die = permute.remove(0).intValue();

                board[row][c] = dieRolls[die];
            }
        }
    }

  /**
   * String representation of the Board.
   */
    @Override 
    public String toString() {
        String s = "";

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                s += board[r][c];
                if (board[r][c] == 'Q') {
                    s += "u";
                }
                else {
                    s += " ";
                }
            }
            if (r < 4) {
                s += "\n";
            }
        }
        return s;
    }

  /**
   * Given a Queue full of Strings, returns the number of points that list
   * of Strings receives in a game of Boggle.
   */
    public static int countPoints(Queue<String> q) {
        int pts = 0;
        for (String s : q) {
            switch (s.length()) {
            case 1:
            case 2:
                break;
            case 3:
            case 4:
                pts += 1;
                break;
            case 5:
                pts += 2;
                break;
            case 6:
                pts += 3;
                break;
            case 7:
                pts += 5;
                break;
            default:
                pts += 11;
                break;
            }
        }
        return pts;
    }
    
    /** A class to represent a position for the work queue */
    public static class Position {
        /** The row */
        public int row;
        /** The column */
        public int column;
        /** Construct a position
         * @param r Row
         * @param c Column
         */
        public Position(int r, int c) {
            row = r;
            column = c;
        }
    }
    
    /** A multithreaded version of allWords().
     * The difference between this and allWords() is that this partitions
     * the set of initial starting indices into several groups, and gives
     * each thread one of these groups.
     */
    public Queue<String> getWords() {
        Trie foundwords = new Trie();
        int numthreads = 4; //should be a good balance and saturate a
                            //quad-core machine.
        CountDownLatch latch = new CountDownLatch(numthreads);
        ConcurrentLinkedQueue<Board.Position> q = 
                new ConcurrentLinkedQueue<Board.Position>();

        for (int r = 0; r < 5; ++r) {
            for (int c = 0; c < 5; ++c) {
                q.offer(new Position(r, c));
            }
        }
        
        for (int i = 0; i < numthreads; ++i) {
            WorkThread t = new WorkThread(board, englishWords, foundwords,
                                          q, latch);
            t.start();
        }

        //wait for all threads to finish
        for (boolean done = false; !done;) {
            try {
                latch.await();
                done = true;
            }
            catch(InterruptedException e) {
                //do nothing... just try again.
            }
        }

        return foundwords.enqueue();
    }

  /**
   * A method that returns a Queue filled with every word that appears in 
   * the Boggle board. 
   */
    public Queue<String> allWords() {
        boolean[][]used = new boolean[5][5];    //Start used as all false
        Trie foundwords = new Trie();

        //basically, we just do the recursive search starting at each node
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                used[r][c] = true;
                char ch = board[r][c];

                Trie.SearchIterator it = englishWords.beginSearch();
                it.next(ch);
                //handle Qu
                if (ch == 'Q') {
                    it.next('U');
                }
                //perform search
                allWords(it, r, c, used, foundwords);
                used[r][c] = false;
            }
        }
        return foundwords.enqueue();
    }

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
    private void allWords(Trie.SearchIterator si, int r, int c,
                          boolean[][]used, Trie foundwords)
    {
        if (!si.reachable()) {
            //no reachable strings from this prefix
            return;
        }
        if (si.inSet()) {
            foundwords.insertForeignIt(si);
        }

        //try to append all adjacent nodes to current prefix
        for (int y : new int[] {r - 1, r, r + 1}) {
            for (int x : new int[] {c - 1, c, c + 1}) {
                //check if we're out of bounds
                if (x < 0 || y < 0 || y >= used.length 
                          || x >= used[0].length) 
                {
                    continue;
                }
                //only append if we haven't already used this node
                if (!used[y][x]) {
                    used[y][x] = true;
                    char ch = board[y][x];

                    //copy so we can keep our current place
                    Trie.SearchIterator it = new Trie.SearchIterator(si);
                    it.next(ch);
                    if (ch == 'Q') {
                        it.next('U');
                    }
                    //append and check
                    allWords(it, y, x, used, foundwords);
                    //unset now that we've handled that node
                    used[y][x] = false;
                }
            }
        }
    }
}
