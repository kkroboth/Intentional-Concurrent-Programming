package icp.wrapper;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class ICPCollections {
  private ICPCollections() {
    // private constructor
  }


  public static <T> List<T> ofList(List<T> wrap) {
    return new WrapperList<>(wrap);
  }

  public static <T> Collection<T> ofCollection(Collection<T> wrap) {
    return new WrapperCollection<>(wrap);
  }

  public static <T> Queue<T> ofQueue(Queue<T> wrap) {
    return new WrapperQueue<>(wrap);
  }

  public static <K, V> Map<K, V> ofMap(Map<K, V> wrap) {
    return new WrapperMap<>(wrap);
  }


  // wrapper collections
  // TODO: Look into iterators, streams, and other loopholes that may miss a
  // a permission check.

  private static final class WrapperMap<K, V> implements Map<K, V> {

    protected final Map<K, V> map;

    private WrapperMap(Map<K, V> map) {
      this.map = map;
    }

    @Override
    public int size() {
      return map.size();
    }

    @Override
    public boolean isEmpty() {
      return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
      return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
      return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
      return map.get(key);
    }

    @Override
    public V put(K key, V value) {
      return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
      return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
      map.putAll(m);
    }

    @Override
    public void clear() {
      map.clear();
    }

    @Override
    public Set<K> keySet() {
      return map.keySet();
    }

    @Override
    public Collection<V> values() {
      return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
      return map.entrySet();
    }
  }

  private static final class WrapperQueue<T> extends WrapperCollection<T> implements Queue<T> {

    protected Queue<T> queue;

    private WrapperQueue(Queue<T> queue) {
      super(queue);
      this.queue = queue;
    }


    @Override
    public boolean offer(T t) {
      return queue.offer(t);
    }

    @Override
    public T remove() {
      return queue.remove();
    }

    @Override
    public T poll() {
      return queue.poll();
    }

    @Override
    public T element() {
      return queue.element();
    }

    @Override
    public T peek() {
      return queue.peek();
    }
  }

  private static final class WrapperList<T> extends WrapperCollection<T> implements List<T> {

    protected final List<T> list;

    private WrapperList(List<T> list) {
      super(list);
      this.list = list;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
      return list.addAll(index, c);
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
      list.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super T> c) {
      list.sort(c);
    }

    @Override
    public T get(int index) {
      return list.get(index);
    }

    @Override
    public T set(int index, T element) {
      return list.set(index, element);
    }

    @Override
    public void add(int index, T element) {
      list.add(index, element);
    }

    @Override
    public T remove(int index) {
      return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
      return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
      return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
      return list.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
      return list.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
      return list.subList(fromIndex, toIndex);
    }
  }

  private static class WrapperCollection<T> implements Collection<T> {
    protected final Collection<T> col;

    private WrapperCollection(Collection<T> col) {
      this.col = col;
    }

    @Override
    public int size() {
      return col.size();
    }

    @Override
    public boolean isEmpty() {
      return col.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
      return col.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
      return col.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
      col.forEach(action);
    }

    @Override
    public Object[] toArray() {
      return col.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
      return col.toArray(a);
    }

    @Override
    public boolean add(T t) {
      return col.add(t);
    }

    @Override
    public boolean remove(Object o) {
      return col.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      return col.contains(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
      return col.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      return col.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
      return col.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      return col.retainAll(c);
    }

    @Override
    public void clear() {
      col.clear();
    }

    @Override
    public Spliterator<T> spliterator() {
      return col.spliterator();
    }

    @Override
    public Stream<T> stream() {
      return col.stream();
    }

    @Override
    public Stream<T> parallelStream() {
      return col.parallelStream();
    }
  }
}
