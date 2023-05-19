package server.concurrent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Defines a concurrent hash map.
 * This can be used when thread-safety is needed.
 * It uses a ReadWriteLock with SE's HashMap.
 */
public class MyConcurrentHashMap<K, V> {
    private final Map<K, V> map;
    private final ReadWriteLock lock;

    public MyConcurrentHashMap() {
        map = new HashMap<>();
        lock = new ReentrantReadWriteLock();
    }

    public V get(K key) {
        lock.readLock().lock();
        try {
            return map.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            map.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean containsKey(K key) {
        lock.readLock().lock();
        try {
            return map.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            map.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }


    public Collection<V> values() {
        lock.readLock().lock();
        try {
            return map.values();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void remove(K token) {
        lock.writeLock().lock();
        try {
            map.remove(token);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
