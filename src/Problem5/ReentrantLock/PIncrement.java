package q6.ReentrantLock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class PIncrement implements Runnable{
    // set up lock
    private static final ReentrantLock lock = new ReentrantLock();

    private static volatile int cInternal = 0;
    private static volatile int numThreadsInternal = 0;
    private static volatile Map<String, Integer> threadIDCount = null;
    private static final int incrementNum = 1200000;

    public static int parallelIncrement(int c, int numThreads){
        System.out.println("parallelIncrement started");

        System.out.println("cInternal has a lock and numThreads are: " + numThreads);

        // init-ing self
        System.out.println("init-ing self");

        if (numThreads <= 0)
            return incrementNum;

        // set up internal variables (that can be seen by the threads)
        threadIDCount = new HashMap<>();
        cInternal = c;
        numThreadsInternal = numThreads;

        PIncrement runnable = new PIncrement();

        // init and start all the threads
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(runnable, Integer.toString(i));
            // map to get relative child thread ids
            threadIDCount.put(Integer.toString(i),i);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        // wait for threads to finish
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
        return cInternal;
    }

    @Override
    public void run() {
        int myThreadID = threadIDCount.get(Thread.currentThread().getName());
        int pid = (int)Thread.currentThread().getId();
        System.out.println("  Thread: " + pid + " assigned to: " + myThreadID + "\n");

        int counter = myThreadID;
        int myCount = 0;

        // run my shared of the increments
        while (counter < incrementNum) {
            // try to get lock
            lock.lock();
            //critical section
            try {
                int temp = cInternal;
                temp++;
                cInternal = temp;
                counter += numThreadsInternal;
                myCount++;
            } finally {
                // release lock
                lock.unlock();
            }
        }
        System.out.println("  My thread ID is: " + myThreadID + " and I am DONE!!!! my count is: " + myCount + "\n");
        //return;
    }
}