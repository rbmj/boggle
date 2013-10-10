import java.util.Queue;
public class TwoPlayers{
  public static void main(String[] args) {
    Board b = new Board();
    Queue<String> q = b.getWords();
    Player p = new Player(b, q);
    long t = System.currentTimeMillis();
    p.start();
    try {
      Thread.sleep(60000);
    } catch (InterruptedException ie){}
    p.interrupt();
    p.getScanner().close();
    Queue<String> playerWords = p.allWords();
    System.out.println("You got " + Board.countPoints(playerWords)
        + " points");
    System.out.println("The computer got " + Board.countPoints(q) + " points");
    System.out.println("The following words were found:");
    try {
      Thread.sleep(3000);
    } catch (InterruptedException ie) {}
    for (String s : q)
      System.out.println(s);
  }
}
