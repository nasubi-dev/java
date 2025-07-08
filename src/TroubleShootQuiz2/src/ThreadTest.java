
public class ThreadTest {

    public static void main(String[] args) {
        MyThread t1 = new MyThread("thread1", 5);
        MyThread t2 = new MyThread("thread2", 3);

        System.out.println("Thread1 Start!");
        t1.start();
        System.out.println("Thread2 Start!");
        t2.start();

        try {
            // スレッドの完了を待つ
            t1.join();
            t2.join();
            System.out.println("All threads completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Main thread was interrupted");
        }
    }

    private static class MyThread extends Thread {
        private String threadName;
        private int count;

        public MyThread(String threadName, int count) {
            this.threadName = threadName;
            this.count = count;
        }

        @Override
        public void run() {
            for (int i = 1; i <= count; i++) {
                System.out.println(threadName + ":" + i);
                try {
                    Thread.sleep(100); // スレッドの実行を可視化するため
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println(threadName + " was interrupted");
                    return;
                }
            }
            System.out.println(threadName + " completed");
        }
    }
}
