import java.util.Queue;
import java.util.LinkedList;

/** A generic AVLTree.
 * @param <T> The type of object to store.  It must implement Comparable&lt;T&gt;
 * 
 * Note that you should not attempt to store null values in the tree, as this
 * breaks the comparison logic and error reporting conventions.
 */
public class AVLTree<T extends Comparable<T>> {
    /** A class to represent a node of the AVL Tree
     * @param <U> Should be the same as the enclosing T.
     */
    private static class Node<U extends Comparable<U>> {
        /** A class to propogate errors up the call stack.
         * @param <V> Should be the same as the enclosing U.
         *
         * This class is necessary for the recursive version of insert() as
         * it needs to pass failure conditions up the call stack (i.e. value already
         * in set) without interrupting flow.  Basically a Pair&lt;V, boolean&gt;.
         */
        public static class Error<V> {
            /** The value of the object. */
            public V value;
            /** The error flag. */
            public boolean error;
            /** Construct a new object from a value.
             * @param V the value to take on
             */
            public Error(V v) {
                value = v;
            }
            /** Set the error flag. */
            void set()  {
                error = true;
            }
        }

        private Node<U> lchild;
        private Node<U> rchild;
        private int subtree_height;
        public U data;

        public Node(U u) {
            lchild = null;
            rchild = null;
            subtree_height = 0;
            data = u;
        }

        public static <U extends Comparable<U>> int height(Node<U> n) {
            if (n == null) {
                return -1;
            }
            return n.subtree_height;
        }

        public enum Side {
            LEFT,
            RIGHT
        }
        public static final Side LEFT = Side.LEFT;
        public static final Side RIGHT = Side.RIGHT;
        
        public static Side otherSide(Side s) {
            if (s == LEFT) {
                return RIGHT;
            }
            return LEFT;
        }

        public Node<U> child(Side s) {
            if (s == LEFT) {
                return lchild;
            }
            //technically incorrect behavior for child(NULL), but you
            //shouldn't do that anyways, since that's invalid.
            return rchild;
        }

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
        
        public static <U extends Comparable<U>> Node<U> rotate(Side s, Node<U> n) {
            Node<U> cld = n.child(otherSide(s));
            Node<U> tmp = cld.child(s);
            cld.setChild(s, n);
            n.setChild(otherSide(s), tmp);
            return cld;
        }

        
        public int balance() {
            return height(rchild) - height(lchild);
        }

        public static <U extends Comparable<U>> int balance(Node<U> n) {
            if (n != null) return n.balance();
            return 0;
        }

        public static <U extends Comparable<U>> Node<U> rebalance(Node<U> n) {
            //precondition: each subtree must be properly balanced
            if (n.balance() == 2 && balance(n.child(RIGHT)) == 1) {
                return Node.<U>rotate(LEFT, n);
            }
            if (n.balance() == 2 && balance(n.child(RIGHT)) == -1) {
                n.setChild(RIGHT, Node.<U>rotate(RIGHT, n.rchild));
                return Node.<U>rotate(LEFT, n);
            }
            if (n.balance() == -2 && balance(n.child(LEFT)) == -1) {
                return Node.<U>rotate(RIGHT, n);
            }
            if (n.balance() == -2 && balance(n.child(LEFT)) == 1) {
                n.setChild(LEFT, Node.<U>rotate(LEFT, n.lchild));
                return Node.<U>rotate(RIGHT, n);
            }
            return n;
        }

        //we use error class because we need to both propogate errors AND
        //also need to keep working if there's a failure
        public static <U extends Comparable<U>> Error<Node<U>> insert(Node<U> n, U u) {
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
        
        //return null if not found
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

        public static <U extends Comparable<U>> void enqueue(Node<U> n, Queue<U> q) {
            if (n == null) return;
            Node.<U>enqueue(n.lchild, q);
            q.offer(n.data);
            Node.<U>enqueue(n.rchild, q);
        }
    }
    private Node<T> root;

    public boolean insert(T t) {
        Node.Error<Node<T>> e = Node.<T>insert(root, t);
        root = Node.<T>rebalance(e.value);
        return !(e.error);
    }

    public int height() {
        return Node.<T>height(root);
    }

    //return null if not found
    public T get(T t) {
        return Node.<T>get(root, t);
    }

    public boolean find(T t) {
        return get(t) != null;
    }

    public Queue<T> enqueue() {
        Queue<T> q = new LinkedList<T>();
        Node.<T>enqueue(root, q);
        return q;
    }

    public void enqueue(Queue<T> q) {
        Node.<T>enqueue(root, q);
    }
}
