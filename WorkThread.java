import java.util.concurrent.CountDownLatch;

public class WorkThread extends Thread {
    private char[][] board;
    private Trie englishWords;
    private Trie foundWords;
    private int rmin;
    private int rmax;
    private int cmin;
    private int cmax;
    private CountDownLatch latch;
    private boolean[][] used;

    public WorkThread(char[][] b, Trie e, Trie f, int rn, int rx, int cn, int cx, CountDownLatch l) {
        board = b;
        englishWords = e;
        foundWords = f;
        rmin = rn;
        rmax = rx;
        cmin = cn;
        cmax = cx;
        latch = l;
        used = new boolean[5][5];
    }
    public void run() {
        used = new boolean[5][5];
        for (int r = rmin; r < rmax; ++r) {
            for (int c = cmin; c < cmax; ++c) {
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
        }
        latch.countDown();
    }
    //Data Structure
    private void search(Trie.SearchIterator si, int r, int c) {
        if (!si.reachable()) {
            //no reachable strings from this prefix
            return;
        }
        if (si.inSet()) {
            String s = si.toString();
            synchronized (foundWords) {
                foundWords.insert(s);
            }
        }
        //try to append all adjacent nodes to current prefix
        for (int y : new int[]{r-1, r, r+1}) {
            for (int x : new int[]{c-1, c, c+1}) {
                //check if we're out of bounds
                if (x < 0 || y < 0 || y >= used.length || x >= used[0].length) {
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
