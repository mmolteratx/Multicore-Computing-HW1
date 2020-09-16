package q5;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

        for(int num : array) {
            if(this.key == num) {
                count++;
            }
        }

        return count;
    }

    public static int parallelFreq(int x, int[] A, int numThreads) {

        if(A.length == 1) {
            if(A[0] == x) {return 1;}
            else {return 0;}
        }

        int chunkSize = A.length / numThreads;
        int i = 0;
        int j = chunkSize;
        int freq = 0;

        List<Future<Integer>> resultList = new ArrayList<>();

        ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);

        for(int k = 0; k < numThreads; k++) {
            Future<Integer> freqResult = threadPool.submit(new Frequency(Arrays.copyOfRange(A, i, j), x));
            resultList.add(freqResult);
            i += chunkSize;
            j += chunkSize;
        }

        for(Future<Integer> future : resultList) {
            try {
                freq += future.get();
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        threadPool.shutdown();
        return freq;
    }

    /*
    public static void main(String[] args) {

        int[] array = new int[]{13, 14, 11, 2, 3, 19, 14, 14};
        int key = 14;

        int freq = parallelFreq(key, array, 4);
        System.out.println("Frequency of " + key + ": " + freq);
    }
    */
}
