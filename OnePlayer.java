import java.util.Queue;
public class OnePlayer {
    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        Board b;
        //0th argument gets used as a seed for the random number generator.  If
        //the seed is identical, the dice rolls will be identical.  To have a
        //surprise roll happen, use no arguments, and the current time in
        //milliseconds will be used as the seed.
        if (args.length > 0) {
            b = new Board(Long.parseLong(args[0]));
        }
        else {
            b = new Board();
        }
        System.out.println(b);
        Queue<String> q = b.allWords();
        for (String s : q) {
            System.out.println(s);
        }
        System.out.println(Board.countPoints(q) + " points");
        time = System.currentTimeMillis() - time;
        System.out.println((time / 1000.0) + " seconds");
    }
}
