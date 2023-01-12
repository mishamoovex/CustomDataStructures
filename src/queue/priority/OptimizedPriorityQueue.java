package queue.priority;

import java.util.*;

public class OptimizedPriorityQueue<T extends Comparable<T>> {

    //The number of elements currently inside the heap
    private int heapSize = 0;

    //The internal capacity of the heap
    private int heapCapacity = 0;

    //A dynamic list ot track the elements inside the heap
    private final List<T> heap;

    //This map keeps track of the possible indices a particular
    //node value is found in the heap. Having this mapping lets
    //us have O(log(n)) removals and 0(1) element containment check
    //at hte cost of some additional space and minor overhead
    private final Map<T, TreeSet<Integer>> map = new HashMap<>();

    //Constructs a priority queue with an initial capacity
    public OptimizedPriorityQueue(int size) {
        heap = new ArrayList<>(size);
    }

    //Constructs and initially empty priority queue
    public OptimizedPriorityQueue() {
        this(1);
    }

    // Construct a priority queue using heapify in O(n) time, a great explanation can be found at:
    // http://www.cs.umd.edu/~meesh/351/mount/lectures/lect14-heapsort-analysis-part.pdf
    public OptimizedPriorityQueue(T[] elements) {
        heapSize = heapCapacity = elements.length;
        heap = new ArrayList<>(heapCapacity);
        //Place all element in heap
        for (int i = 0; i < heapSize; i++) {
            mapAdd(elements[i], i);
            heap.add(elements[i]);
        }
        //Heapify process, 0(n)
        for (int i = Math.max(0, (heapSize / 2) - 1); i >= 0; i--) {
            sink(i);
        }
    }

    //Priority queue construction, O(log(n))
    public OptimizedPriorityQueue(Collection<T> elements) {
        this(elements.size());
        for (T elem : elements) add(elem);
    }

    public int size() {
        return heapSize;
    }

    public boolean isEmpty() {
        return heapSize == 0;
    }

    //Clears everything inside the heap, O(n)
    public void clear() {
        for (int i = 0; i < heapCapacity; i++) {
            heap.set(i, null);
        }
        heapSize = 0;
        map.clear();
    }

    //Returns the value of the element with the lowest
    //priority in this priority queue. If the priority
    //queue is empty null is returned.
    public T peek() {
        if (isEmpty()) return null;
        return heap.get(0);
    }

    //Removed the root of the heap, 0(log(n))
    public T poll() {
        return removeAt(0);
    }

    //Checks is an element is in the heap, 0(1)
    public boolean contains(T element) {
        //Map lookup to check containment, 0(1)
        if (element == null) return false;
        return map.containsKey(element);

        //Linear scan to check containment, O (n)
        //for(int i = 0; i < heapSize; i++){
        //    if (heap.get(i).equals(element)){
        //        return true;
        //    }
        //}
        //return false;
    }

    //Adds an element to the priority queue, the
    //element must not be null, O(log(n))
    public void add(T element) {
        if (element == null) throw new IllegalArgumentException();

        if (heapSize < heapCapacity) {
            heap.set(heapSize, element);
        } else {
            heap.add(element);
            heapCapacity++;
        }
        mapAdd(element, heapSize);
        swim(heapSize);
        heapSize++;
    }


    //Tests if the value of node i <= j
    //This method assumes i & j are valid indices, O(1)
    private boolean less(int i, int j) {
        T node1 = heap.get(i);
        T node2 = heap.get(j);
        return node1.compareTo(node2) <= 0;
    }


    //Bottom up node swim, O(log(n))
    private void swim(int k) {
        //Grav the index of the next parent node WRT to k
        int parent = (k - 1) / 2;
        //Keep swimming while we have not reached the
        //root and while we're less than our parent.
        while (k > 0 && less(k, parent)) {
            //Exchange k with the parent
            swap(parent, k);
            k = parent;

            //Grab the index of the next parent node WRT to k
            parent = (k - 1) / 2;
        }
    }

    //Top down node sink, O(long(n))
    private void sink(int k) {
        while (true) {

            int left = 2 * k + 1;  //Left node
            int right = 2 * k + 2; //Right node
            int smallest = left;   //Assume left is the smallest node of the two children

            //Find is smaller left or right
            //If right is smaller set smallest to be right
            if (right < heapSize && less(right, left)) {
                smallest = right;
            }

            //Stop if we're outside the bounds of the tree
            //or stop early if we cannot sink k anymore
            if (left >= heapSize || less(k, smallest)) break;

            //Move  down the tree following the smallest node
            swap(smallest, k);
            k = smallest;
        }
    }

    //Swap two nodes. Assumes i & j are valid, 0(1)
    private void swap(int i, int j) {
        T j_elem = heap.get(j);
        T i_elem = heap.get(i);

        heap.set(i, j_elem);
        heap.set(j, i_elem);

        mapSwap(i_elem, j_elem, i, j);
    }

    //Removed a particular element in the heap, O(long(n))
    public boolean remove(T element) {
        if (element == null) return false;

        //Linear removal via search, O(n)
        //for (int i = 0; i < heapSize; i++) {
        //    if (heap.get(i).equals(element)) {
        //        removeAt(i);
        //        return true;
        //    }
        //}

        //Logarithmic removal with map, O(log(n))
        Integer index = mapGet(element);
        if (index != null) removeAt(index);
        return index != null;
    }

    //Removes a node at particular index, O(long(n))
    public T removeAt(int i) {
        if (isEmpty()) return null;

        heapSize--;
        T removed_data = heap.get(i);
        swap(i, heapSize);

        //Obliterate the value
        heap.set(i, null);
        mapRemove(removed_data, heapSize);

        //If happened that we just remove last element
        if (i == heapSize) return removed_data;

        //Use this element to check is singing has change the nodes
        T element = heap.get(i);

        //Try sinking element
        sink(i);

        //If sinking did not work try swimming
        if (heap.get(i).equals(element)) {
            swim(i);
        }

        return removed_data;
    }

    //Add a node value and its index to the map
    private void mapAdd(T value, int index) {
        TreeSet<Integer> set = map.get(value);

        //New value being inserted in map
        if (set == null) {
            set = new TreeSet<>();
            set.add(index);
            map.put(value, set);
        }
        //Value already exists in map
        else {
            set.add(index);
        }
    }

    //Removes the index at a given value, O(log(n))
    private void mapRemove(T value, int index) {
        TreeSet<Integer> set = map.get(value);
        set.remove(index); //TreeSets take O(long(n)) removal time
        if (set.size() == 0) map.remove(value);
    }

    //Extract an index position for the given value
    //NOTE: If a value exists multiple times in the heap the highest
    //index is returned (this has arbitrarily been chosen)
    private Integer mapGet(T value) {
        TreeSet<Integer> set = map.get(value);
        if (set != null) return set.last();
        return null;
    }

    //Exchange the index of two nodes internally within the map
    private void mapSwap(T val1, T val2, int val1Index, int val2Index) {
        Set<Integer> set1 = map.get(val1);
        Set<Integer> set2 = map.get(val2);

        set1.remove(val1Index);
        set2.remove(val2Index);

        set1.add(val2Index);
        set2.add(val1Index);
    }

    @Override
    public String toString() {
        return heap.toString();
    }

    //Recursively checks if this heap is a min heap
    //This method is just for testing purposes to make
    //sure the heap invariants is still being maintained
    //Called this method with k=0 to start at the root
    public boolean isMinHeap(int k) {
        //If we are outside the bounds of the heap return true
        if (k >= heapSize) return true;

        int left = 2 * k + 1;
        int right = 2 * k + 2;

        if (left < heapSize && !less(k, left)) return false;
        if (right < heapSize && !less(k, right)) return false;

        //Recurse on both children to make sure they're also valid heaps
        return isMinHeap(left) && isMinHeap(right);
    }

    /*
            .
            1
        .       .
        3       2
     .    .   .   .
     6    4   9   5
   .   .  .
   7  10  8

     */
    public static void main(String[] args) {
        Integer[] data = new Integer[10];
        data[0] = 2;
        data[1] = 3;
        data[2] = 1;
        data[3] = 10;
        data[4] = 4;
        data[5] = 9;
        data[6] = 5;
        data[7] = 7;
        data[8] = 6;
        data[9] = 8;

        System.out.println(Arrays.toString(data));

        ArrayPriorityQueue<Integer> queue = new ArrayPriorityQueue<>(data);

        System.out.println(queue);
        System.out.println(queue.isMinHeap(0));
    }


}
