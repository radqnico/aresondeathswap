package it.areson.aresondeathswap.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class MutexArrayList<T> extends ArrayList<T> {

    private final Semaphore semaphore;

    public MutexArrayList() {
        super();
        semaphore = new Semaphore(1);
    }


    public synchronized void lock() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void release() {
        semaphore.release();
    }

    @Override
    public void trimToSize() {
        lock();
        super.trimToSize();
        release();
    }

    @Override
    public void ensureCapacity(int minCapacity) {
        lock();
        super.ensureCapacity(minCapacity);
        release();
    }

    @Override
    public int size() {
        lock();
        int size = super.size();
        release();
        return size;
    }

    @Override
    public boolean isEmpty() {
        lock();
        boolean empty = super.isEmpty();
        release();
        return empty;
    }

    @Override
    public boolean contains(Object o) {
        lock();
        boolean contains = super.contains(o);
        release();
        return contains;
    }

    @Override
    public int indexOf(Object o) {
        lock();
        int i = super.indexOf(o);
        release();
        return i;
    }

    @Override
    public int lastIndexOf(Object o) {
        lock();
        int i = super.lastIndexOf(o);
        release();
        return i;
    }

    @Override
    public Object clone() {
        lock();
        Object clone = super.clone();
        release();
        return clone;
    }

    @Override
    public Object[] toArray() {
        lock();
        Object[] objects = super.toArray();
        release();
        return objects;
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        lock();
        T1[] t1s = super.toArray(a);
        release();
        return t1s;
    }

    @Override
    public T get(int index) {
        lock();
        T t = super.get(index);
        release();
        return t;
    }

    @Override
    public T set(int index, T element) {
        lock();
        T set = super.set(index, element);
        release();
        return set;
    }

    @Override
    public boolean add(T t) {
        lock();
        boolean add = super.add(t);
        release();
        return add;
    }

    @Override
    public void add(int index, T element) {
        lock();
        super.add(index, element);
        release();
    }

    @Override
    public T remove(int index) {
        lock();
        T remove = super.remove(index);
        release();
        return remove;
    }

    @Override
    public boolean remove(Object o) {
        lock();
        boolean remove = super.remove(o);
        release();
        return remove;
    }

    @Override
    public void clear() {
        lock();
        super.clear();
        release();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        lock();
        boolean b = super.addAll(c);
        release();
        return b;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        lock();
        boolean b = super.addAll(index, c);
        release();
        return b;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        lock();
        super.removeRange(fromIndex, toIndex);
        release();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        lock();
        boolean b = super.removeAll(c);
        release();
        return b;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        lock();
        boolean b = super.retainAll(c);
        release();
        return b;
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        lock();
        ListIterator<T> tListIterator = super.listIterator(index);
        release();
        return tListIterator;
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        lock();
        ListIterator<T> tListIterator = super.listIterator();
        release();
        return tListIterator;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        lock();
        Iterator<T> iterator = super.iterator();
        release();
        return iterator;
    }

    @Override
    public @NotNull List<T> subList(int fromIndex, int toIndex) {
        lock();
        List<T> ts = super.subList(fromIndex, toIndex);
        release();
        return ts;
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        lock();
        super.forEach(action);
        release();
    }

    @Override
    public Spliterator<T> spliterator() {
        lock();
        Spliterator<T> spliterator = super.spliterator();
        release();
        return spliterator;
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        lock();
        boolean b = super.removeIf(filter);
        release();
        return b;
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        lock();
        super.replaceAll(operator);
        release();
    }

    @Override
    public void sort(Comparator<? super T> c) {
        lock();
        super.sort(c);
        release();
    }
}
