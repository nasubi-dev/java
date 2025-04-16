import java.util.Scanner;

public class MessageInput {
  public static void main(String[] args) {
    var msg = "こんにちは、メッセージをどうぞ";
    System.out.println(msg);

    Scanner in = new Scanner(System.in);
    String inputLine = in.nextLine();
    in.close();

    System.out.println("メッセージを受信しました\n" + //
        "----");
    System.out.println(inputLine);
    System.out.println("----");
  }
}
