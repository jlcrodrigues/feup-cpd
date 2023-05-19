package server.concurrent;
import java.util.ArrayDeque;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Defines a concurrent array deque.
 * This can be used when thread-safety is needed and works as a list, queue, etc.
 * It uses a ReentrantLock with SE's ArrayDeque.
 */
public class MyConcurrentArrayDeque<T> {
    private ArrayDeque<T> deque;
    private ReentrantLock lock;

    public MyConcurrentArrayDeque() {
        deque = new ArrayDeque<T>();
        lock = new ReentrantLock();
    }

    public void addFirst(T element) {
        lock.lock();
        try {
            deque.addFirst(element);
        } finally {
            lock.unlock();
        }
    }

    public void addLast(T element) {
        lock.lock();
        try {
            deque.add(element);
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(T element) {
        lock.lock();
        try {
            return deque.remove(element);
        } finally {
            lock.unlock();
        }
    }

    public T removeFirst() {
        lock.lock();
        try {
            return deque.removeFirst();
        } finally {
            lock.unlock();
        }
    }

    public T removeLast() {
        lock.lock();
        try {
            return deque.removeLast();
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return deque.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return deque.size();
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            deque.clear();
        } finally {
            lock.unlock();
        }
    }

    public ArrayDeque<T> getQueue() {
        lock.lock();
        try {
            return new ArrayDeque<>(deque);
        } finally {
            lock.unlock();
        }
    }
}