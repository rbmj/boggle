import java.util.Queue;
import java.util.LinkedList;

public class BSTree<T extends Comparable<T>> {
    //struct to store data
    private static class Node<U extends Comparable<U>> {
        public U data;
        public Node<U> left;
        public Node<U> right;
        public Node(U u) {
            data = u;
        }
    }
    private Node<T> root;
    public BSTree() {
        root = null;
    }
    //basic insertion
    public void insert(T t) {
        if (root == null) {
            root = new Node<T>(t);
        }
        else {
            insert(root, t);
        }
    }
    //static cause we don't need a this pointer or the overhead of indirect
    //calls
    private static <T extends Comparable<T>> void insert(Node<T> n, T t) {
        int cmp = t.compareTo(n.data);
        if (cmp == -1) {
            if (n.left == null) {
                n.left = new Node<T>(t);
            }
            else {
                BSTree.<T>insert(n.left, t);
            }
        }
        else if (cmp == 1) {
            if (n.right == null) {
                n.right = new Node<T>(t);
            }
            else {
                BSTree.<T>insert(n.right, t);
            }
        }
        //do nothing if already in tree
    }
    private static <T extends Comparable<T>> int height(Node<T> n) {
        if (n == null) return -1;
        int lheight = BSTree.<T>height(n.left);
        int rheight = BSTree.<T>height(n.right);
        return ((lheight > rheight) ? lheight : rheight) + 1;
    }
    public int height() {
        return BSTree.<T>height(root);
    }
    //return null if not found!
    public T get(T t) {
        return BSTree.<T>get(root, t);
    }
    private static <T extends Comparable<T>> T get(Node<T> n, T t) {
        if (n == null) {
            return null;
        }
        int cmp = t.compareTo(n.data);
        if (cmp == 0) {
            return n.data;
        }
        if (cmp == -1) {
            return BSTree.<T>get(n.left, t);
        }
        //greater than
        return BSTree.<T>get(n.right, t);
    }
    public boolean find(T t) {
        return get(t) != null;
    }
    public Queue<T> enqueue() {
        Queue<T> q = new LinkedList<T>();
        enqueue(q);
        return q;
    }
    public void enqueue(Queue<T> q) {
        BSTree.<T>enqueue(q, root);
    }
    private static <T extends Comparable<T>> void enqueue(Queue<T> q, Node<T> n) {
        if (n == null) return;
        BSTree.<T>enqueue(q, n.left);
        q.offer(n.data);
        BSTree.<T>enqueue(q, n.right);
    }    
}
    
