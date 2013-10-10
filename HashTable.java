import java.util.Scanner;
import java.util.LinkedList;
import java.util.Queue;

public class HashTable {
    private static int hash(String s) {
        return s.hashCode();
    }
    //Alternate hash algorithm - test for which is faster
    //for hash time vs. collisions...
    /*
    private static int hash(String s) {
        //murmur hash from wikipedia
        //see http://bit.ly/L0vFbz for rationale
        int len = s.length();

        final int c1 = 0xcc9e2d51;
        final int c2 = 0x1b873593;
        final int m = 5;
        final int n = 0xe6546b64;

        int hash = len; //seed with length
        int i = 0;

        //we know everything's US-ASCII so we can make some simplifications
        for (i = 0; i < len - (len % 4); i = i + 4) {
            //I wish this was C so I could just cast the pointer...
            int val = (s.codePointAt(i) << 24)
                + (s.codePointAt(i+1) << 16)
                + (s.codePointAt(i+2) << 8)
                + (s.codePointAt(i+3));
            val = val * c1;
            val = (val << 15) | (val >>> 17);
            val = val * c2;

            hash = hash ^ val;
            hash = (hash << 13) | (hash >>> 19);
            hash = hash * m + n;
        }
        //deal with remaining bytes
        int rem = 0;
        switch (len % 4) {
            case 3:
                rem = s.codePointAt(i+2) << 16;
            case 2:
                rem = rem + s.codePointAt(i+1) << 8;
            case 1:
                rem = rem + s.codePointAt(i);
                //now everything
                rem = (rem << 15) | (rem >>> 17);
                rem = rem * c2;
                hash = hash ^ rem;
            default:
                break;
        }
        hash = hash ^ len;
        hash = hash ^ (hash >>> 16);
        hash = hash * 0x85ebca6b;
        hash = hash ^ (hash >>> 13);
        hash = hash * 0xc2b2ae35;
        hash = hash ^ (hash >>> 16);
        return hash;
    }
    */
    private AVLTree<String>[] buckets;
    public HashTable(int num_buckets) {
        init(num_buckets);
    }
    public HashTable() {
        init(1024);
    }
    @SuppressWarnings("unchecked")
    private void init(int num_buckets) {
        buckets = (AVLTree<String>[]) new AVLTree[num_buckets]; //blech
        for (int i = 0; i < buckets.length; ++i) {
            buckets[i] = new AVLTree<String>();
        }
    }
    public static int toi(int num, int mod) {
        int x = num % mod;
        if (x < 0) x += mod;
        return x;
    }
    public void insert(String s) {
        buckets[toi(hash(s), buckets.length)].insert(s);
    }
    public boolean find(String s) {
        return buckets[toi(hash(s), buckets.length)].get(s) != null;
    }
    public double avgHeight() {
        int sum = 0;
        for (int i = 0; i < buckets.length; ++i) {
            int x = buckets[i].height();
            if (x > 0) sum += x;
        }
        return sum/((double)buckets.length);
    }
    public Queue<String> enqueue() {
        Queue<String> q = new LinkedList<String>();
        enqueue(q);
        return q;
    }
    public void enqueue(Queue<String> q) {
        for (int i = 0; i < buckets.length; ++i) {
            buckets[i].enqueue(q);
        }
    }
    public static void main(String[] args) {
        //hash test
        HashTable table = new HashTable(1024);
        Scanner in = new Scanner(System.in);
        while (in.hasNextLine()) {
            String ln = in.nextLine();
            table.insert(ln);
        }
        System.out.println(table.avgHeight());
    }
}


