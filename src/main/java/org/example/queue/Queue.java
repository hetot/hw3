package org.example.queue;

public interface Queue<E> {
    void add(E e);

    E poll();

    boolean isEmpty();

    int size();
}
