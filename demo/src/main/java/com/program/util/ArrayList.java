package com.program.util;

public class ArrayList<T> {
    
    protected T[] list;

    public ArrayList() {
        list = (T[]) new Object[0];
    }

    public ArrayList(int size) {
        list = (T[]) new Object[size];
    }
    
    public T[] getList() { return list; }

    public String toString() {
        StringBuilder str = new StringBuilder("{");
        if (list.length > 0) {
            for (int i = 0; i < list.length; i++) {
                str.append(list[i]).append(i < list.length - 1 ? ", " : "");
            }
        }
        str.append("}");
        return str.toString();
    }

    public T get(int element) {
        if (element > list.length - 1 || element < 0)
            return null;
        else
            return list[element];
    }

    public void set(int element, T thing) {
        if (element < list.length && element >= 0) {
            list[element] = thing;
        }
    }

    public int size() {
        return list.length;
    }

    public boolean isEmpty() {
        return (list.length == 0);
    }

    public int indexOf(T thing) {
        for (int i = 0; i < list.length; i++) {
            if (list[i].equals(thing)) {
                return i;
            }
        }
        return -1;
    }

    public void add(T thing) {
        mutateArray(list.length + 1);
        list[list.length - 1] = thing;
    }

    public void removeFirst(T thing) {
        for (int i = 0; i < list.length; i++) {
            if (list[i] == thing) {
                list[i] = null;
                mutateArray(list.length - 1);
                break;
            }
        }
    }

    public void removeAll(T thing) {
        int instances = 0;
        for (int i = 0; i < list.length; i++) {
            if (list[i] == thing) {
                list[i] = null;
                instances++;
            }
        }
        if (instances > 0) // if the thing was found at least once
            mutateArray(list.length - instances);
    }

    public void clear() {
        for (int i = 0; i < list.length; i++) {
            set(i, null);
            mutateArray(0);
        }
    }

    protected void mutateArray(int newSize) {
        T[] newList = (T[]) new Object[newSize];
        int pointer = 0;
        for (T t : list) {
            if (t != null) // forgets all the previous null entries, which shifts elements when removing
                newList[pointer++] = t;
        }
        list = newList;
    }

    public boolean equals(ArrayList<T> check) {
        for (int i = 0; i < check.size(); i++) {
            if (check.get(i) != this.get(i))
                return false;
        }
        return true;
    }

    public ArrayList<T> clone() {
        ArrayList<T> clone = new ArrayList<>();
        for (int i = 0; i < list.length; i++) {
            clone.add(this.get(i));
        }
        return clone;
    }
}
