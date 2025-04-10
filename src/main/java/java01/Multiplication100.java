package java01;

import java.util.Scanner;

public class Multiplication100 {
  public static void main(String[] args) {
    var msg = "整数値を入力してください";
    System.out.println(msg);

    Scanner in = new Scanner(System.in);
    Number inputInt = in.nextInt();
    in.close();

    System.out.println("計算結果：" + (inputInt.intValue() * 100));
  }
}
