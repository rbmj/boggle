import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentLinkedQueue;

/** A thread to do the boggle using a subset of the board as starting
 * indices.  The search may move outside of these starting indices to
 * continue the search.
 */
public class WorkThread extends Thread {
    /** The board to search */
    private char[][] board;
    /** The dictionary */
    private Trie englishWords;
    /** The shared set of words found on the board */
    private Trie foundWords;
    /** The work-stealing queue */
    private ConcurrentLinkedQueue<Board.Position> work_queue;
    /** The latch to signal upon completion */
    private CountDownLatch latch;
    /** The set of used indices for a search */
    private boolean[][] used;

    /** Construct a WorkThread to search a subset of the boggle board
     * @param b The board to search (readonly)
     * @param e The dictionary (readonly)
     * @param f The shared set (locks used) of words found on the board (rw)
     * @param q The work queue
     * @param l The latch to signal upon completion of the search
     */
    public WorkThread(char[][] b, Trie e, Trie f, 
                      ConcurrentLinkedQueue<Board.Position> q,
                      CountDownLatch l)
    {
        board = b;
        englishWords = e;
        foundWords = f;
        work_queue = q;
        latch = l;
    }
    /** Start the thread. */
    public void run() {
        used = new boolean[5][5];
        while (true) {
            Board.Position pos = work_queue.poll();
            if (pos == null) {
                //done
                break;
            }
            int r = pos.row;
            int c = pos.column;

            used[r][c] = true;
            char ch = board[r][c];

            Trie.SearchIterator it = englishWords.beginSearch();
            it.next(ch);
            //handle Qu
            if (ch == 'Q') {
                it.next('U');
            }
            //perform search
            search(it, r, c);
            used[r][c] = false;
        }
        latch.countDown();
    }
    /** A recursive search function.
     * @param si The current position in the dictionary Trie.
     * @param r The current row
     * @param c The current column
     */
    private void search(Trie.SearchIterator si, int r, int c) {
        if (!si.reachable()) {
            //no reachable strings from this prefix
            return;
        }
        if (si.inSet()) {
            synchronized(foundWords) {
                foundWords.insertForeignIt(si);
            }
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
                    //append
                    it.next(ch);
                    if (ch == 'Q') {
                        it.next('U');
                    }
                    //search
                    search(it, y, x);
                    //unset now that we've handled that node
                    used[y][x] = false;
                }
            }
        }
    }
}
