import java.util.Queue;
import java.util.ArrayDeque;
import java.text.StringCharacterIterator;

/** A prefix Trie.
 * Note:  This Trie will only work on strings "[A-Z]*".  Any
 * other characters will cause failure conditions.
 *
 * This Trie is built to be fast, so it has minimal error
 * checking.  If you send it bad input you'll probably segfault.
 */
public class Trie {
    /** Enum to allow checking both the membership of a key k
     * and the existance of any keys prefixed by k */
    public static enum SearchResult {
        FOUND,
        NOTFOUND,
        NOPREFIX
    }
    /** Iterator for searching a Trie */
    public static class SearchIterator {
        /** The current node being visited */
        private Node curNode;
        /** Check membership of the current location
         * @return True if this iterator represents a string in the set
         */
        public boolean inSet() {
            if (curNode == null) return false;
            return curNode.value_here;
        }
        /** Check reachability of any keys from the current node.
         * @return True if some sequence of keys moves this iterator
         *         to a valid string in the Trie
         */
        public boolean reachable() {
            if (curNode == null) return false;
            return true;
            //this is semantically more correct than return true,
            //but I don't know if it's worth the extra work.
            //Benchmark.
            /*
            for (Node child : curNode.children) {
                if (child != null) return true;
            }
            return curNode.value_here;
            */
        }
        /** Descend to the next level in the Trie.
         * @param c The character to append/the path to descend
         * 
         * Note:  It is possible to fall off the bottom of the tree
         * with this method.  If that is the case reachable() will
         * return false.
         */
        public void next(char c) {
            if (curNode == null) return;
            curNode = curNode.children[Node.index(c)];
        }
        /** Construct a SearchIterator.
         * @param The root of the Trie to search
         */
        public SearchIterator(Node n) {
            curNode = n;
        }
        /** Copy a SearchIterator.
         * @param si The SearchIterator to copy
         */
        public SearchIterator(SearchIterator si) {
            curNode = si.curNode;
        }
        /** Get the current node as a String
         * @return The string representing the string at this node
         */
        public String toString() {
            /* Building the string on demand instead of continually updating
             * it on the iterator yields an overall 4.9% overall improvement
             */
            StringBuilder sb = new StringBuilder();
            buildString(sb, curNode);
            return sb.toString();
        }
        /** Recursive helper for toString().
         * @param sb The string so far (i.e. the prefix)
         * @param n The current node being visited
         */
        private static void buildString(StringBuilder sb, Node n) {
            if (n == null) {
                return;
            }
            char c = n.char_here;
            if (c == 0) {
                return; //also done - root node may not have a valid char
            }
            buildString(sb, n.parent);
            sb.append(c);
        }
        /** The current character at this node in the Trie.
         * @return The character
         */
        public char charHere() {
            return curNode.char_here;
        }
        /** Ascend this iterator to the parent node.
         *
         * It is possible to fall off the top of the trie using this
         * method (i.e. if the iterator points to the root).  In that
         * case, reachable() will return false.
         */
        public void up() {
            if (curNode != null) curNode = curNode.parent;
        }
    }
    /** A low level struct with some helpers for use by Trie. */
    private static class Node {
        /** This node's parent (null if root) */
        public Node parent;
        /** The character at this node */
        public char char_here;
        /** Whether this node represents a string in the Trie */
        public boolean value_here;
        /** The child node array */
        //TODO:  Might be faster to actually have 26 children in line so
        //we don't have the extra indirection, at the cost of some code.
        //need to benchmark for that though.
        public Node[] children;
        /** Create a node with a given character character and parent
         * @param c The character at this node
         * @param p The parent of this node
         */
        public Node(char c, Node p) {
            parent = p;
            char_here = c;
            value_here = false;
            children = new Node[26];
        }
        /** Get the index corresponding to a character into the child
         *  array
         *  @param c The character key
         *  @return i The index into the array
         */
        public static int index(char c) {
            return (int)(c - 'A');
        }
        /** Get the proper child node corresponding to a character,
         *  creating the node if necessary.
         *  @param c The character key.
         */
        public Node get(char c) {
            int i = index(c);
            Node n = children[i];
            if (n == null) {
                n = new Node(c, this);
                children[i] = n;
            }
            return n;
        }
    }
    /** The root of the Trie */
    private Node root;
    /** The number of elements in this Trie */
    private int size;
    /** Create a Trie */
    public Trie() {
        root = new Node((char)0, null);
        size = 0;
    }
    /** Gets a SearchIterator for this Trie.
     * @return The SearchIterator.
     */
    public SearchIterator beginSearch() {
        return new SearchIterator(root);
    }
    /** Recursive helper for insert(s).
     * @param n The current node visited
     * @param sci An iterator to the next char in the string
     * This is basic tree traversal - nothing new here.
     */
    private static void insert(Node n, StringCharacterIterator sci) {
        char c = sci.next();
        if (c == sci.DONE) {
            n.value_here = true;
            return;
        }
        insert(n.get(c), sci);
    }
    /** Insert a string into the trie.
     * @param s The string to insert
     */
    public void insert(String s) {
        StringCharacterIterator sci = new StringCharacterIterator(s);
        insert(root.get(sci.first()), sci);
        ++size;
    }
    /** Recursive helper for insertForeignIt(it).
     * @param it The iterator into the foreign Trie.
     * @param r The root node of the local Trie.
     * @param c The character indicating the proper child to traverse to
     *          from the node in the local tree corresponding to it.
     * @return The child of the node in the local tree corresponding to
     *         it.next(c)
     *
     * The recursion here is not the simplest to grok.  As an illustration:
     *
     * Foreign Trie:  root(0) - node0(b) - node1(a) - node2(r)
     * Local Trie:  same nodes, but names are prefixed with L (e.g. Lroot)
     * 
     * insertForeignIt([iterator corresponding to "bar"]):
     *      calls insertForeignIt(node2, Lroot, 0)
     *          calls insertForeignIt(node1, Lroot, r)
     *              calls insertForeignIt(node0, Lroot, a)
     *                  calls insertForeignIt(root, Lroot, b)
     *                      calls insertForeignIt(root, Lroot, 0)
     *                          returns Lroot
     *                      returns Lroot['b'] (Lnode0)
     *                  returns Lroot['b']['a'] (Lnode1)
     *              returns Lroot['b']['a']['r'] (Lnode2)
     *          returns Lroot['b']['a']['r'] (Lnode2)
     *      Lroot['b']['a']['r'].value_here = true;
     * done
     *
     * Basically, we walk up the first trie, pushing characters onto the
     * call stack as we go, and then once we reach the top, we walk down
     * the local trie, popping characters off the call stack as we go.
     * 
     * It's a fancy version of a recursive implementation of a stack based
     * string reversal algorithm, applied to a linked list and a trie.
     *
     * And everyone thought that reversing strings was a silly problem...
     */
    private static Node insertForeignIt(SearchIterator it, Node r, char c) {
        if (!it.reachable()) { //fallen off the top of the tree
            return r;
        }
        char tmp = it.charHere();
        it.up();
        Node n = insertForeignIt(it, r, tmp);
        return (c == 0) ? n : n.get(c);
    }
    /** Insert the string indicated by a SearchIterator into another Trie
     * into this Trie.
     * @param it An iterator into another Trie.
     */
    public void insertForeignIt(SearchIterator it) {
        insertForeignIt(it, root, (char)0).value_here = true;
        ++size;
    }
    /** Recursive helper for find(s).
     * @param n The current node being visited
     * @param sci An iterator into the current position in the string
     */
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
    /** Determine if a string is in this Trie.
     * @param s The string to search for
     * @return SearchResult.{NOPREFIX, FOUND, NOTFOUND} for the string
     *
     * NOPREFIX implies that s is not a prefix to any string in the trie.
     * NOTFOUND implies that s may be a prefix to one or more strings in
     *      the Trie, but s itself is not in the Trie.
     * FOUND implies that the string is in fact in the Trie.
     */
    public SearchResult find(String s) {
        StringCharacterIterator sci = new StringCharacterIterator(s);
        return find(root.children[Node.index(sci.first())], sci);
    }
    /** Determine if a string is in this Trie.
     * @param s The string to search for
     * @return True if s is in the Trie, false otherwise
     *
     * This is exactly the same as find(s) == FOUND.
     */
    public boolean get(String s) {
        return find(s) == SearchResult.FOUND;
    }
    /** Helper method for enqueue()
     * @param n The current node being visited
     * @param prefix The prefix associated with the current node
     * @param q The queue to add items to
     */
    private static void enqueue(Node n, String prefix, Queue<String> q) {
        if (n == null) return;
        if (n.value_here) {
            q.offer(prefix);
        }
        for (int i = 0; i < 26; ++i) {
            enqueue(n.children[i], prefix + ((char)('A' + i)), q);
        }
    }
    /** Enqueue all of the elements of this Trie.
     * @return A queue containing all of the elements of this Trie
     */
    public Queue<String> enqueue() {
        //give us a decent initial size - don't worry about memory
        Queue<String> q = new ArrayDeque<String>(size);
        enqueue(q);
        return q;
    }
    /** Enqueue all of the elements of this Trie into a given queue.
     * @param q The queue to enqueue all of the elements into
     */
    public void enqueue(Queue<String> q) {
        for (int i = 0; i < 26; ++i) {
            enqueue(root.children[i], Character.toString((char)('A' + i)), q);
        }
    }
}
        
