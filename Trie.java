import java.util.Queue;
import java.util.ArrayDeque;
import java.text.StringCharacterIterator;

/** A prefix Trie */
public class Trie {
    /** Enum to allow checking both the membership of a key k
     * and the existance of any keys prefixed by k */
    public static enum SearchResult {
        FOUND,
        NOTFOUND,
        NOPREFIX
    }
    /** Iterator for Searching.
     * TODO:  Make copying cheap! */
    public static class SearchIterator {
        /** The current node being visited */
        private Node curNode;
        /** The current string (INEFFICIENT) */
        private StringBuilder str;
        /** Check membership of the searched key */
        public boolean inSet() {
            if (curNode == null) return false;
            return curNode.value_here;
        }
        /** Check reachability of any keys from the current node */
        public boolean reachable() {
            return curNode != null;
        }
        public void next(char c) {
            str.append(c);
            if (curNode == null) return;
            curNode = curNode.children[Node.index(c)];
        }
        public SearchIterator(Node n) {
            curNode = n;
            str = new StringBuilder();
        }
        public SearchIterator(SearchIterator si) {
            curNode = si.curNode;
            str = new StringBuilder(si.str);
        }
        public String toString() {
            return str.toString();
        }
    }
    private static class Node {
        public boolean value_here;
        public Node[] children;
        public Node() {
            value_here = false;
            children = new Node[26];
        }
        public static int index(char c) {
            return (int)(c - 'A');
        }
    }
    private Node root;
    public Trie() {
        root = new Node();
        //we do a little more work than would be elegant to accomodate
        //java's iterator idioms here...
        for (int i = 0; i < 26; ++i) {
            root.children[i] = new Node();
        }
    }
    public SearchIterator beginSearch() {
        return new SearchIterator(root);
    }
    private static void insert(Node n, StringCharacterIterator sci) {
        char c = sci.next();
        if (c == sci.DONE) {
            n.value_here = true;
            return;
        }
        int i = Node.index(c);
        if (n.children[i] == null) {
            n.children[i] = new Node();
        }
        insert(n.children[i], sci);
    }
    public void insert(String s) {
        StringCharacterIterator sci = new StringCharacterIterator(s);
        insert(root.children[Node.index(sci.first())], sci);
    }
    private static SearchResult find(Node n, StringCharacterIterator sci) {
        if (n == null) {
            return SearchResult.NOPREFIX;
        }
        char c = sci.next();
        if (c == sci.DONE) {
            return n.value_here ? SearchResult.FOUND : SearchResult.NOTFOUND;
        }
        return find(n.children[Node.index(c)], sci);
    }
    public SearchResult find(String s) {
        StringCharacterIterator sci = new StringCharacterIterator(s);
        return find(root.children[Node.index(sci.first())], sci);
    }
    private static void enqueue(Node n, StringBuilder prefix, Queue<String> q) {
        if (n == null) return;
        if (n.value_here) {
            q.offer(prefix.toString());
        }
        for (int i = 0; i < 26; ++i) {
            StringBuilder newpfx = new StringBuilder(prefix);
            enqueue(n.children[i], newpfx.append((char)('A' + i)), q);
        }
    }
    public Queue<String> enqueue() {
        //give us a decent initial size - don't worry about memory
        Queue<String> q = new ArrayDeque<String>(512);
        enqueue(q);
        return q;
    }
    public void enqueue(Queue<String> q) {
        for (int i = 0; i < 26; ++i) {
            StringBuilder pfx = new StringBuilder();
            enqueue(root.children[i], pfx.append((char)('A' + i)), q);
        }
    }
}
        
