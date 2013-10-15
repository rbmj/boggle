import java.util.Queue;
import java.util.LinkedList;

/** A generic AVLTree.
 * @param <T> The type of object to store.  It must implement
 * Comparable&lt;T&gt;
 * 
 * Note that you should not attempt to store null values in the tree,
 * as this breaks the comparison logic and error reporting conventions.
 */
public class AVLTree<T extends Comparable<T>> {
    /** A class to represent a node of the AVL Tree
     * @param <U> Should be the same as the enclosing T.
     */
    //type erasure SUCKS so I can't statically use T...
    private static class Node<U extends Comparable<U>> {
        /** A class to propogate errors up the call stack.
         * @param <V> Should be the same as the enclosing U.
         *
         * This class is necessary for the recursive version of insert() as
         * it needs to pass failure conditions up the call stack (i.e.
         * value already in set) without interrupting flow.  Basically
         * a Pair&lt;V, boolean&gt;.
         */
        public static class Error<V> {
            /** The value of the object. */
            public V value;
            /** The error flag. */
            public boolean error;
            /** Construct a new object from a value.
             * @param v the value to take on
             */
            public Error(V v) {
                value = v;
            }
            /** Set the error flag. */
            void set()  {
                error = true;
            }
        }
        /** The left subtree */
        private Node<U> lchild;
        /** The right subtree */
        private Node<U> rchild;
        /** The height of this subtree.
         * This is defined as max(height(lchild), height(rchild)) + 1
         */
        private int subtree_height;
        /** The data stored at this node */
        public U data;

        /** Construct a node.
         * @param u The data to store at this node */
        public Node(U u) {
            lchild = null;
            rchild = null;
            subtree_height = 0;
            data = u;
        }

        /** Get the height of a Node.
         * This is static to allow height(null).
         * @param n The node to get the height of
         * @return The height of n as a subtree
         */
        public static <U extends Comparable<U>> int height(Node<U> n) {
            if (n == null) {
                return -1;
            }
            return n.subtree_height;
        }

        /** Represent a side to make the rotations a bit more generic */
        public enum Side {
            LEFT,
            RIGHT
        }
        /** Represents left (for convenience) */
        public static final Side LEFT = Side.LEFT;
        /** Represents right (for convenience) */
        public static final Side RIGHT = Side.RIGHT;
        
        /** Invert a side.
         * @param s The side to invert.
         * @return The inverted side (e.g. otherSide(RIGHT) == LEFT)
         */
        public static Side otherSide(Side s) {
            if (s == LEFT) {
                return RIGHT;
            }
            return LEFT;
        }

        /** Get the child of this node on one side.
         * @param s Which side to get the child on.
         * @return The child node (may be null)
         */
        public Node<U> child(Side s) {
            if (s == LEFT) {
                return lchild;
            }
            //technically incorrect behavior for child(NULL), but you
            //shouldn't do that anyways, since that's invalid.
            return rchild;
        }

        /** Set the child of this node.
         * This method also updates the cached subtree heights.
         * @param s The side to set.
         * @param n The node to set the child of
         */
        public void setChild(Side s, Node<U> n) {
            if (s == LEFT) {
                lchild = n;
            }
            else {
                rchild = n;
            }
            int rheight = Node.<U>height(rchild);
            int lheight = Node.<U>height(lchild);
            if (lheight < rheight) {
                subtree_height = rheight + 1;
            }
            else {
                subtree_height = lheight + 1;
            }
        }
        
        /** Perform a tree rotation.
         * @param s Which type of rotation to perform.
         * @param n The node about which to rotate.
         */
        public static <U extends Comparable<U>> Node<U> rotate(Side s, Node<U> n) {
            Node<U> cld = n.child(otherSide(s));
            Node<U> tmp = cld.child(s);
            cld.setChild(s, n);
            n.setChild(otherSide(s), tmp);
            return cld;
        }

        /** The balance of this tree.
         * @return The balance of this, height(rchild) - height(lchild).
         */
        public int balance() {
            return height(rchild) - height(lchild);
        }

        /** The balance of some arbitrary tree
         * @param n The node to get the balance of.
         * @return The balance of n, height(n.rchild) - height(n.lchild).
         */
        public static <U extends Comparable<U>> int balance(Node<U> n) {
            if (n != null) return n.balance();
            return 0;
        }

        public static <U extends Comparable<U>> Node<U> rebalance(Node<U> n) {
            //precondition: each subtree must be properly balanced
            if (balance(n) == 2 && balance(n.child(RIGHT)) == 1) {
                return Node.<U>rotate(LEFT, n);
            }
            if (balance(n) == 2 && balance(n.child(RIGHT)) == -1) {
                n.setChild(RIGHT, Node.<U>rotate(RIGHT, n.rchild));
                return Node.<U>rotate(LEFT, n);
            }
            if (balance(n) == -2 && balance(n.child(LEFT)) == -1) {
                return Node.<U>rotate(RIGHT, n);
            }
            if (balance(n) == -2 && balance(n.child(LEFT)) == 1) {
                n.setChild(LEFT, Node.<U>rotate(LEFT, n.lchild));
                return Node.<U>rotate(RIGHT, n);
            }
            return n;
        }

        //we use error class because we need to both propogate errors AND
        //also need to keep working if there's a failure
        /** Insert a node.
         * This insert method preserves the AVL invariant.
         * @param n The subtree to insert into.
         * @param u The data to insert
         * @return The new root of the tree (may change due to rotations)
         */
        public static <U extends Comparable<U>>
        Error<Node<U>> insert(Node<U> n, U u) {
            if (n == null) {
                return new Error<Node<U>>(new Node<U>(u));
            }
            int i = u.compareTo(n.data);
            if (i != 0) {
                Side s = (i < 0) ? LEFT : RIGHT;
                Error<Node<U>> e = Node.<U>insert(n.child(s), u);
                n.setChild(s, Node.<U>rebalance(e.value));
                e.value = n;
                return e;
            }
            //Node is already in the tree - error
            Error<Node<U>> e = new Error<Node<U>>(n);
            e.set();
            return e;
        }
        
        /** Get a node from the tree.
         * @param n The tree to search
         * @param u The data to search for
         * @return The found node, or null if not found
         */
        public static <U extends Comparable<U>> U get(Node<U> n, U u) {
            if (n == null) {
                return null;
            }
            int i = u.compareTo(n.data);
            if (i < 0) {
                return Node.<U>get(n.lchild, u);
            }
            if (i > 0) {
                return Node.<U>get(n.rchild, u);
            }
            return n.data;
        }

        /** Insert the sorted elements of this tree into a queue.
         * @param n The tree to traverse
         * @param q The queue into which to insert the elements of n
         */
        public static <U extends Comparable<U>>
        void enqueue(Node<U> n, Queue<U> q) {
            if (n == null) return;
            Node.<U>enqueue(n.lchild, q);
            q.offer(n.data);
            Node.<U>enqueue(n.rchild, q);
        }
    }
    /** The root of the AVL Tree */
    private Node<T> root;

    /** Insert data into the tree.
     * @param t The data to insert
     * @return True if successful, false if the data was already present.
     */
    public boolean insert(T t) {
        Node.Error<Node<T>> e = Node.<T>insert(root, t);
        //need to reset root in case root has changed...
        root = Node.<T>rebalance(e.value);
        return !(e.error);
    }

    /** The height of the tree
     * @return The height
     */
    public int height() {
        return Node.<T>height(root);
    }

    /** Lookup data in the AVL Tree
     * @param t The data to search for
     * @return The data in the tree, or null if not found.
     *
     * Note:  This method is intended for use in maps and other structures
     * which store data that can compare equal but contain different data.
     */
    public T get(T t) {
        return Node.<T>get(root, t);
    }
    
    /** Check if data is in the AVL Tree
     * @param t The data to search for
     * @return True if found, false otherwise
     */
    public boolean find(T t) {
        return get(t) != null;
    }

    /** Create a queue consisting of the elements of this tree in sorted
     * order.
     * @return The queue, containing the sorted elements of this.
     */
    public Queue<T> enqueue() {
        Queue<T> q = new LinkedList<T>();
        Node.<T>enqueue(root, q);
        return q;
    }

    /** Enqueue the elements of this into a queue in sorted order
     * @param q The queue into which to enqueue.
     */
    public void enqueue(Queue<T> q) {
        Node.<T>enqueue(root, q);
    }
}
