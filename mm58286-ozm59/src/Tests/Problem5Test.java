package Tests;

import q6.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class Problem5Test {

	@Test
	public void TestTournament() {
		int res = Problem5.Tournament.PIncrement.parallelIncrement(0, 8);
		assertTrue("Result is " + res + ", expected result is 1200000.", res == 1200000);
	}

	@Test
	public void TestAtomicInteger() {
    	int res = Problem5.AtomicInteger.PIncrement.parallelIncrement(0, 8);
    	assertTrue("Result is " + res + ", expected result is 1200000.", res == 1200000);
	}

	@Test
	public void TestSynchronized() {
    	int res = Problem5.Synchronized.PIncrement.parallelIncrement(0, 8);
    	assertTrue("Result is " + res + ", expected result is 1200000.", res == 1200000);
	}

	@Test
	public void TestReentrantLock() {
		int res = Problem5.ReentrantLock.PIncrement.parallelIncrement(0, 8);
		assertTrue("Result is " + res + ", expected result is 1200000.", res == 1200000);
	}
}
