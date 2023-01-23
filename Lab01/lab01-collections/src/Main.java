import java.util.*;

public class Main {
    public static void main(String[] args) {
        final int ELEMENTS_TO_ADD = 1_000_000;
        final int NUM_THREADS = 100;

        // part 1, add into vector and arraylist (single thread)
        long start = System.currentTimeMillis();

        int intArray[] = new int[ELEMENTS_TO_ADD+1];
        Vector<Integer> intVector = new Vector<Integer>();

        for (int i = 0; i < ELEMENTS_TO_ADD; i++) {
            intArray[i] = i;
            intVector.add(i);
        }

        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("Time spent: " + timeElapsed + " milliseconds");

        // part 2, add into hashtable, hashmap, concurrent hashmap (single thread)
        System.out.println("Part2:");
        start = System.currentTimeMillis();
        Hashtable<Integer, Integer> intHashTable = new Hashtable<Integer, Integer>();
        HashMap<Integer, Integer> intHashMap = new HashMap<Integer, Integer>();
        Map<Integer, Integer> intConcurMap = Collections.synchronizedMap(intHashMap);


        for (int i = 0; i < ELEMENTS_TO_ADD; i++) {
            intHashTable.put(i, 1);
            intHashMap.put(i,1);
            intConcurMap.put(i,1);
        }
        System.out.println("OK");
        finish = System.currentTimeMillis();
        timeElapsed = finish - start;
        System.out.println("Time spent: " + timeElapsed + " milliseconds");
    }
}