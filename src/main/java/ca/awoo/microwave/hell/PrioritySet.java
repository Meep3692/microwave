package ca.awoo.microwave.hell;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class PrioritySet<T> implements Iterable<T>{
    private class Priority implements Comparable<Priority>{
        public final int priority;
        public final T item;
        public Priority(int priority, T item) {
            this.priority = priority;
            this.item = item;
        }
        @Override
        public int compareTo(PrioritySet<T>.Priority o) {
            int diff = priority - o.priority;
            if(diff == 0) diff = 1;
            return diff;
        }
        public T getItem() {
            return item;
        }
    }
    private final SortedSet<Priority> set;
    public PrioritySet(){
        set = new TreeSet<>();
    }

    public void add(T item, int priority){
        set.add(new Priority(priority, item));
    }

    public void clear(){
        set.clear();
    }

    @Override
    public Iterator<T> iterator() {
        return set.stream().map(Priority::getItem).iterator();
    }
}
