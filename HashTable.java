import java.util.Scanner;
import java.util.LinkedList;
import java.util.Queue;

/** Implements a fixed size hash table.
 * Resizing is not implemented so efficiency requires knowledge of how
 * many elements will be added to the table at construction time.
 *
 * @param <T> The type of object to store.
 */
public class HashTable<T extends Comparable<T>> {
    /** The array of buckets */
    private LinkedList<T>[] buckets;
    /** Construct a HashTable with a given number of buckets.
     * @param num_buckets How many buckets to use
     */
    public HashTable(int num_buckets) {
        init(num_buckets);
    }
    /** Construct a HashTable.
     * The default size is 1024 buckets
     */
    public HashTable() {
        init(1024); //a sane default size for many sets
    }
    /** Initialization routine
     * @param num_buckets How many buckets to initialize
     */
    @SuppressWarnings("unchecked")
    private void init(int num_buckets) {
        //have I spewed enough about my hatred of java generics?
        buckets = (LinkedList<T>[]) new LinkedList[num_buckets];
        for (int i = 0; i < buckets.length; ++i) {
            buckets[i] = new LinkedList<T>();
        }
    }
    /** Convert a hash to an index
     * @param num The hash
     * @param mod The number of buckets
     */
    private static int toi(int num, int mod) {
        int x = num % mod;
        //handle large hashes (which, because java treats me like a child,
        //I can't use unsigned for).
        if (x < 0) x += mod; //I LOVE MY PADDED CELL
        return x;
    }
    /** Insert into the HashTable.
     * @param t The data to insert
     */
    public void insert(T t) {
        buckets[toi(t.hashCode(), buckets.length)].addFirst(s);
    }
    /** Determine if data is in the HashTable.
     * @param t The data to search for
     */
    public boolean find(T t) {
        return buckets[toi(t.hashCode(), buckets.length)].contains(s);
    }
    /** Place all of the elements of this HashTable into a queue.
     * @return A queue containing all of the elements in this HashTable.
     */
    public Queue<T> enqueue() {
        Queue<T> q = new LinkedList<T>();
        enqueue(q);
        return q;
    }
    /** Enqueue all of the elements of this HashTable into a queue.
     * @param q The queue to which to add the elements.
     */
    public void enqueue(Queue<T> q) {
        for (LinkedList<T> bucket : buckets) {
            for (T t : bucket) {
                q.offer(t);
            }
        }
    }
}


