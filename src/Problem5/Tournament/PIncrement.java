package q6.Tournament;

public class PIncrement implements Runnable{

    private static volatile int cInternal;
    private static volatile int numThreadsInternal;
    private static final int incrementNum = 1200000;
    //private static final int incrementNum = 120;

    private static volatile TournamentLock threadLock;

    public PIncrement(int c) {
        cInternal = c;
    }

    public static int parallelIncrement(int c, int numThreads) {
        System.out.println("parallelIncrement started");

        // set up all the variables
        numThreadsInternal = numThreads;

        // init lock
        //System.out.println("new TournamentLock");
        threadLock = new TournamentLock(numThreadsInternal);

        // init-ing self
        System.out.println("init-ing self");

        if (numThreads <= 0)
            return incrementNum;

        PIncrement runnable = new PIncrement(c);

        // init and start all the threads
        Thread[] threads = new Thread[numThreadsInternal];
        for (int i = 0; i < numThreadsInternal; i++) {
            threads[i] = new Thread(runnable, Integer.toString(i));
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

        c = cInternal;
        return c;
    }
    @Override
    public void run() {
        // get my thread Id
        int pid = (int)Thread.currentThread().getId();

        // get which index for thread and start counter
        int counter = threadLock.assignID(pid);
        int id = counter;
        int myCount = 0;
        int myC =0;

        // while this thread still need to increment, keep going
        while(counter < incrementNum) {
            // request access to critical section, will wait until granted
            threadLock.lock(pid);
            // critical section
            try{
                int temp = cInternal;
                temp++;
                cInternal = temp;
                myCount++;
                counter += numThreadsInternal;
                //System.out.println("My thread ID is: " + id + " count is: " + counter + " cInternal is at: " + myC + "\n");
            }finally{
                // release lock
                threadLock.unlock(pid);
            }
        }
        System.out.println("  My thread ID is: " + id + " and I am DONE!!!! count is at: " + myC + " my count is: " + myCount +"\n");
        //return;
    }
}