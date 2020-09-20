package q6;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class SimpleTest_Plot {

    // run the tests and output to a csv file
    @Test
    public void main() {

        int n = 100;
        int increment = 1200000;

        String file_name = "C:\\Users\\zhvhe\\Documents\\IntelliJ\\MultiCore\\HW1\\100_results_1_to_8.csv";

        File file = new File(file_name);
        String buffer = "test_name, num_threads, time\n";
        write_to(file_name, buffer, false);

        for (int j = 0; j < 100; j++) {
            for (int i = 1; i <= 8; i++) {
                long nanotime;
                int res;

                buffer = "tournament, " + i + ", ";
                do {
                    long startTime = System.nanoTime();
                    res = q6.Tournament.PIncrement.parallelIncrement(0, i);
                    long stopTime = System.nanoTime();
                    nanotime = stopTime - startTime;
                } while (res != increment);
                buffer += nanotime + "\n";
                write_to(file_name, buffer, true);

                buffer = "atomic, " + i + ", ";
                do {
                    long startTime = System.nanoTime();
                    res = q6.AtomicInteger.PIncrement.parallelIncrement(0, i);
                    long stopTime = System.nanoTime();
                    nanotime = stopTime - startTime;
                } while (res != increment);
                buffer += nanotime + "\n";
                write_to(file_name, buffer, true);

                buffer = "sync, " + i + ", ";
                do {
                    long startTime = System.nanoTime();
                    res = q6.Synchronized.PIncrement.parallelIncrement(0, i);
                    long stopTime = System.nanoTime();
                    nanotime = stopTime - startTime;
                } while (res != increment);
                buffer += nanotime + "\n";
                write_to(file_name, buffer, true);

                buffer = "lock, " + i + ", ";
                do {
                    long startTime = System.nanoTime();
                    res = q6.ReentrantLock.PIncrement.parallelIncrement(0, i);
                    long stopTime = System.nanoTime();
                    nanotime = stopTime - startTime;
                } while (res != increment);
                buffer += nanotime + "\n";
                write_to(file_name, buffer,true);
            }
        System.out.println("j= "+j);
        }
    }

    @Test
    public void TestTournament() {
        int res = q6.Tournament.PIncrement.parallelIncrement(0, 8);
        assertTrue("Result is " + res + ", expected result is 1200000.", res == 1200000);
    }

// We can skip this part
    @Test
    public void TestAtomicInteger() {
        int res = q6.AtomicInteger.PIncrement.parallelIncrement(0, 8);
        assertTrue("Result is " + res + ", expected result is 1200000.", res == 1200000);
    }

    @Test
    public void TestSynchronized() {
        int res = q6.Synchronized.PIncrement.parallelIncrement(0, 8);
        assertTrue("Result is " + res + ", expected result is 1200000.", res == 1200000);
    }

    @Test
    public void TestReentrantLock() {
        int res = q6.ReentrantLock.PIncrement.parallelIncrement(0, 8);
        assertTrue("Result is " + res + ", expected result is 1200000.", res == 1200000);
    }

    public void open_file(String file_name) {
        try {
            File myObj = new File(file_name);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch(IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void write_to(String file_name, String buffer, boolean append) {
        try {
            FileWriter myWriter = new FileWriter(file_name, append);
            myWriter.write(buffer);
            myWriter.close();
            //System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}