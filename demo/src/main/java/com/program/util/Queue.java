package com.program.util;

public class Queue<T> {

    private T[] list;
    private int addindex = 0;
    private int subindex = 0;

    public Queue() {
        list = (T[]) new Object[0];
    }

    public T[] getList() { return list; }

    public int size() { return (addindex - subindex); } // If the addindex and subindex are pointing to the same element, that means the list is empty

    public boolean isEmpty() { return (size() == 0); }

    public String toString() {
        StringBuilder str = new StringBuilder("{");
        if (size() > 0) {
            for (int i = subindex; i < addindex; i++) {
                str.append(list[i]).append(i < addindex - 1 ? ", " : "");
            }
        }
        str.append("}");
        return str.toString();
    }

    public T peek() {
        if (size() > 0)
            return list[subindex];
        else
            return null;
    }

    public void enqueue(T thing) {
        if (addindex == list.length) { // If the addindex is now past the size of the list, create a new list with double the size
            T[] newList = (T[]) new Object[Math.max(1, list.length * 2)]; // If the size is 0 it will become 1
            System.arraycopy(list, 0, newList, 0, list.length);
            list = newList;
        }
        list[addindex] = thing;
        addindex++;
    }

    public T dequeue() {
        T element = null;
        if (size() > 0) {
            element = list[subindex];
            list[subindex] = null;
            subindex++; // The next dequeue will be from the element after this one, since I am not shifting anything
        }
        return element;
    }

    public T get(int element) {
        if (size() > 0)
            return list[subindex + element]; // Add the subindex offset because if we count from 0 the first few elements may be null
        else
            return null;
    }

}
