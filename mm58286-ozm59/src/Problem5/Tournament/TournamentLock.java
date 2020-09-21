package q6.Tournament;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class TournamentLock implements Lock {
    // variable that stored the want bool
    // first index is which 'level'
    // second index is which thread
    private final AtomicBoolean[][] wantPerLevel;

    // variable that stores the turn bool
    // first index is 'level'
    // second index is which pair/set of threads
    private final AtomicBoolean[][] turnPerLevel;

    // number of threads
    private final int numThreadsInternal;

    // max number of levels of the tournament
    private final int maxLevel;

    // map pid's to indexes
    private final HashMap<Integer, Integer> hmap = new HashMap<>();
    // number of indicies to far
    private volatile int currentIndexNum = 0;

    public TournamentLock(int numThreads){

        numThreadsInternal = numThreads;

        // figure out what is the min power of 2 I need to cover all threads
        int temp = numThreads-1;
        int _maxLevel = 0;
        while(temp >= 1) {
            temp = (temp/2);
            _maxLevel++;
        }
        maxLevel = _maxLevel;
        System.out.println("maxLevel is: " + maxLevel + " and numThreads are: " + numThreadsInternal);

        // set up the want array, need one value per thread, and one array per level
        wantPerLevel = new AtomicBoolean[maxLevel][numThreadsInternal];
        for (int curLevel = maxLevel-1; curLevel >= 0; curLevel--) {

            // round is how many levels we have to init (0 to maxLevel-1)
            int round = maxLevel - curLevel - 1;

            // for each level, the number of threads is the previous level's threads,
            // rounded uo to the nearest even and divided by 2
            int threadsPerLevel = numThreadsInternal;
            for (int i = 0; i < round; i++) {
                threadsPerLevel--;
                threadsPerLevel = threadsPerLevel >> 1;
                threadsPerLevel++;
            }

            //System.out.println("Size of this level of want[" + curLevel + "][" + threadsPerLevel + "]");

            for (int curThread = 0; curThread < threadsPerLevel; curThread++) {
                //System.out.println("Setting want[" + curLevel + "][" + curThread + "] to false");
                wantPerLevel[curLevel][curThread] = new AtomicBoolean(false);
            }
        }

        // set up the turn array, need one value per 2 threads, and one array per level
        turnPerLevel = new AtomicBoolean[maxLevel][numThreadsInternal/2 + 1];
        for (int curLevel = maxLevel-1; curLevel >= 0; curLevel--) {

            // round is how many levels we have to init (0 to maxLevel-1)
            int round = maxLevel - curLevel - 1;

            // for each level, the number of threads is the previous level's threads,
            // rounded uo to the nearest even and divided by 2
            int threadsPerNextLevel = numThreadsInternal;
            for (int i = 0; i <= round; i++) {
                threadsPerNextLevel = (threadsPerNextLevel - 1 ) >> 1;
                threadsPerNextLevel++;
            }

            //System.out.println("Size of this level of turn[" + curLevel + "][" + threadsPerNextLevel + "]");

            // init turn array
            for (int curThread = 0; curThread < threadsPerNextLevel; curThread++) {
                //System.out.println("Setting turn[" + curLevel + "][" + curThread + "] to false");
                turnPerLevel[curLevel][curThread] = new AtomicBoolean(false);
            }
        }
    }
    public void lock(int pid) {
        // if pid not in map, its not part of the lock
        if (!hmap.containsKey(pid))
            return;

        int threadID = hmap.get(pid);
        //System.out.println("  Thread: " + threadID + " requesting lock");

        // currentLevel  is what level of the tournament we are in
        for (int curLevel = maxLevel-1; curLevel >= 0; curLevel--) {

            // round is how many levels we have completed (0 to maxLevel-1)
            int round = maxLevel - curLevel - 1;

            // for each level, the number of threads is the previous level's threads,
            // rounded uo to the nearest even and divided by 2
            int threadsPerLevel = numThreadsInternal;
            for (int i = 0; i < round; i++) {
                threadsPerLevel = (threadsPerLevel - 1) >> 1;
                threadsPerLevel++;
            }

            //find where this thread maps to in this level
            int relativeID = threadID;
            for (int i = 0; i < round; i++) {
                relativeID = relativeID >> 1;
            }

            // find which gate this thread must pass in this level
            int relativeGate = relativeID;
            relativeGate = relativeGate >> 1;

            // find what to set the gate to (true == 0, false == 1)
            // and who to watch
            boolean setGate = true;
            int watchThread = relativeID - 1;
            if (relativeID % 2 == 0) {
                // set to other
                setGate = false;
                watchThread = relativeID + 1;
            }

            //set want to true
            wantPerLevel[curLevel][relativeID].set(true);
            //System.out.println("    Thread: " + threadID + "(" + relativeID + ") setting want[" + curLevel + "][" + relativeID + "] to true");

            // see if I am against another thread
            if (relativeID % 2 == 0 && relativeID + 1 >= threadsPerLevel) {
                // I am an oddball, no need to set turn
                //System.out.println("    Thread: " + threadID + "(" + relativeID + ") at level[" + curLevel + "] (odd out of: " + threadsPerLevel + ")");
                continue;
            }

            // set turn to other thread and wait
            //System.out.println("    Thread: " + threadID + "(" + relativeID + ") setting turn[" + curLevel + "][" + relativeGate + "] to " + setGate);
            turnPerLevel[curLevel][relativeGate].set(setGate);

            // test the conditions to see if continue waiting
            //System.out.println("    Thread: " + threadID + "(" + relativeID + ") is waiting on turn[" + curLevel + "][" + relativeGate + "]");
            while (wantPerLevel[curLevel][watchThread].get() && turnPerLevel[curLevel][relativeGate].get() == setGate) {}
        }
        //return;
    }
    public void unlock(int pid) {
        // if pid not in map, its not part of the lock
        if (!hmap.containsKey(pid))
            return;

        int threadID = hmap.get(pid);
        //System.out.println("  Thread: " + threadID + " requesting unlock\n");
        for (int curLevel = 0; curLevel < maxLevel; curLevel++) {

            // round is how many levels we have yet to complete (maxLevel-1 to 0)
            int round = maxLevel - curLevel -1;

            // for each level, the number of threads is the previous level's threads,
            // rounded uo to the nearest even and divided by 2
            int threadsPerLevel = numThreadsInternal;
            for (int i = 0; i < round; i++) {
                if(threadsPerLevel % 2 == 1)
                    threadsPerLevel++;
                threadsPerLevel = threadsPerLevel >> 1;
            }

            //find where this thread maps to in this level
            int relativeID = threadID;
            for (int i = 0; i < round; i++) {
                relativeID = relativeID >> 1;
            }

            //set want to false
            //System.out.println("    Thread: " + threadID + " setting want[" + curLevel + "][" + relativeID + "] to false\n");
            wantPerLevel[curLevel][relativeID].set(false);
        }
        //return;
    }

    public int assignID(int pid) {
        // add the pid to the map if its new, assign index
        Integer index = -1;
        if (!hmap.containsKey(pid)) {
            do {
                hmap.put(pid, currentIndexNum);
                index = hmap.get(pid);
            }
            while(index == null);

            int before, after;
            do {
                before = currentIndexNum;
                before++;
                currentIndexNum = before;
                after = currentIndexNum;
            }
            while(after != before);
        }
        System.out.println("  Thread: " + pid + " assigned to: " + index + "\n");
        return index;
    }
}