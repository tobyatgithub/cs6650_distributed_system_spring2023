import java.util.concurrent.atomic.AtomicInteger;

public class ThreadsExample implements Runnable {
    static AtomicInteger counter = new AtomicInteger(0); // a global counter

//    static void incrementCounter() {
//        System.out.println(Thread.currentThread().getName() + ": " + counter.getAndIncrement());
//    }

    @Override
    public void run() {
        for (int j = 0; j < 10; j++) {
            counter.getAndIncrement();
//            System.out.println(Thread.currentThread().getName() + ": " + counter.get());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final int NUM_THREADS = 1000;
        ThreadsExample te = new ThreadsExample();
        Thread[] threads = new Thread[NUM_THREADS];
        long start = System.currentTimeMillis();


        // fire up all threads
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(te);
            threads[i].start();
        }

        // wait for all threads to finish
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;

        System.out.println(Thread.currentThread().getName() + ": " + counter.get());
        System.out.println("Time spent: " + timeElapsed + " milliseconds");
    }
}