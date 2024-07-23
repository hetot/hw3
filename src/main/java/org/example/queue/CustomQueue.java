package org.example.queue;

import lombok.Getter;

public class CustomQueue<E> implements Queue<E> {
    private Node<E> head = null;
    private int size = 0;

    public CustomQueue() {
    }

    @Override
    public void add(E e) {
        head = new Node<>(e, head);
        size += 1;
    }

    @Override
    public E poll() {
        if (head == null) {
            return null;
        }
        var response = head.element;
        head = head.prev;
        size -= 1;
        return response;
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public int size() {
        return size;
    }

    private static class Node<E> {
        private final E element;
        private final Node<E> prev;

        Node(E element, Node<E> prev) {
            this.element = element;
            this.prev = prev;
        }
    }
}
