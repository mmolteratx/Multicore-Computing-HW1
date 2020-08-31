package Problem4;

import java.util.Arrays;
import java.util.concurrent.*;

public class Frequency implements Callable {

    int[] array;
    int key;

    public Frequency(int[] array, int key) {
        this.array = array;
        this.key = key;
    }

    public Integer call() {
        Integer count = 0;

        for(int num: array) {
            if(this.key == num) {
                count++;
            }
        }

        return count;
    }

    public static int parallelFreq(int x, int[] A, int numThreads) {

        int chunkSize = A.length / numThreads;
        int i = 0;
        int j = chunkSize;
        int freq = 0;

        ExecutorService threadPool = Executors.newCachedThreadPool();


        for(int k = 0; k < numThreads; k++) {
            try {
                Future<Integer> freq1 = threadPool.submit(new Frequency(Arrays.copyOfRange(A, i, j), x));
                freq += freq1.get();
                System.out.println("thread");
                i += chunkSize;
                j += chunkSize;

            } catch (Exception e) {
                System.err.println(e);
            }
        }

        threadPool.shutdown();
        return freq;
    }

    public static void main(String[] args) {

        int[] array = new int[]{13, 14, 11, 2, 3, 19, 14, 14};
        int key = 14;

        int freq = parallelFreq(key, array, 4);
        System.out.println("Frequency of " + key + ": " + freq);
    }
}