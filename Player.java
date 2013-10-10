import java.util.Queue;
import java.util.Scanner;
import java.io.*;
public class Player extends Thread {
  private Board b;
  //TODO: Replace Trie below.
  private Trie allWords;
  private Trie myWords;
  private Scanner in;

  public Player(Board b, Queue<String> compWords) {
    //TODO: Build allWords below
    allWords = new Trie();
    for (String s : compWords)
      allWords.insert(s);
    this.b = b;
    //TODO: Build myWords below
    myWords = new Trie();
  }

  public void run() {
    in = new Scanner(System.in);
    try{
      while(!isInterrupted()) {
        System.out.println(b);
        System.out.print("Find a word? ");
        String word = in.next().toUpperCase();
        if (allWords.find(word) == Trie.SearchResult.FOUND) {
          System.out.println("Yes, that's a word.");
          myWords.insert(word);
        } else
          System.out.println(word + 
              " is either not a word, or does not appear in this board.");
      }
    } catch (IllegalStateException ise) {
      System.out.println("Time's up!");
    }
  }

  public Scanner getScanner() {
    return in;
  }

  public Queue<String> getWords() {
    return myWords.enqueue();
  }
}
