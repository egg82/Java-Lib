package ninja.egg82.primitive.ints;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntCollections;
import it.unimi.dsi.fastutil.objects.AbstractObjectIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public class Object2IntArrayMap<K> extends AbstractObject2IntMap<K> implements Cloneable {
	//vars
	private static final long serialVersionUID = 1L;
	private transient Object[] key;
	private transient int[] value;
	private int size;
	
	//constructor
	public Object2IntArrayMap(final Object[] key, final int[] value) {
		super();
		
		this.key = key;
		this.value = value;
		size = key.length;
		
		if (key.length != value.length) {
			throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
		}
	}
	public Object2IntArrayMap() {
		super();
		
		this.key = ObjectArrays.EMPTY_ARRAY;
		this.value = IntArrays.EMPTY_ARRAY;
	}
	public Object2IntArrayMap(final int capacity) {
		super();
		
		this.key = new Object[capacity];
		this.value = new int[capacity];
	}
	public Object2IntArrayMap(final Object2IntMap<K> m) {
		this(m.size());
		putAll(m);
	}
	public Object2IntArrayMap(final Map<? extends K, ? extends Integer> m) {
		this(m.size());
		putAll(m);
	}
	public Object2IntArrayMap(final Object[] key, final int[] value, final int size) {
		super();
		
		this.key = key;
		this.value = value;
		this.size = size;
		
		if (key.length != value.length) {
			throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
		}
		if (size > key.length) {
			throw new IllegalArgumentException("The provided size (" + size + ") is larger than or equal to the backing-arrays size (" + key.length + ")");
		}
	}
	
	//public
	public FastEntrySet<K> object2IntEntrySet() {
		return new EntrySet();
	}
	
	public int getInt(final Object k) {
		final Object[] key = this.key;
		for (int i = size; i-- != 0;) {
			if (((key[i]) == null ? k == null : (key[i]).equals(k))) {
				return value[i];
			}
		}
		return defRetValue;
	}

	public int size() {
		  return size;
	}
	public void clear() {
		for (int i = size; i-- != 0;) {
			key[i] = null;
		}
		size = 0;
	}

	public boolean containsKey(final Object k) {
		return findKey(k) != -1;
	}
	public boolean containsValue(int v) {
		for (int i = size; i-- != 0;) {
			if (value[i] == v) {
				return true;
			}
		}
		return false;
	}
	public boolean isEmpty() {
		return size == 0;
	}

	public int put(K k, int v) {
		final int oldKey = findKey(k);
		
		if (oldKey != -1) {
			final int oldValue = value[oldKey];
			value[oldKey] = v;
			return oldValue;
		}
		if (size == key.length) {
			final Object[] newKey = new Object[size == 0 ? 2 : size * 2];
			final int[] newValue = new int[size == 0 ? 2 : size * 2];
			for (int i = size; i-- != 0;) {
				newKey[i] = key[i];
				newValue[i] = value[i];
			}
			key = newKey;
			value = newValue;
		}
		
		key[size] = k;
		value[size] = v;
		size++;
		
		return defRetValue;
	}
	
	public int removeInt(final Object k) {
		final int oldPos = findKey(k);
		if (oldPos == -1) {
			return defRetValue;
		}
		
		final int oldValue = value[oldPos];
		final int tail = size - oldPos - 1;
		
		for( int i = 0; i < tail; i++ ) {
			key[oldPos + i] = key[oldPos + i + 1];
			value[oldPos + i] = value[oldPos + i + 1];
		}
		size--;
		
		key[size] = null;
		return oldValue;
	}
	
	public ObjectSet<K> keySet() {
		return new ObjectArraySet<K>(key, size);
	}
	public IntCollection values() {
		return IntCollections.unmodifiable(new IntArraySet(value, size));
	}
	@SuppressWarnings("unchecked")
	public Object2IntArrayMap<K> clone() {
		Object2IntArrayMap<K> c;
		try {
			c = (Object2IntArrayMap<K>) super.clone();
		} catch (CloneNotSupportedException cantHappen) {
			throw new InternalError();
		}
		
		c.key = key.clone();
		c.value = value.clone();
		return c;
	}
	
	//private
	private final class EntrySet extends AbstractObjectSet<Object2IntMap.Entry<K>> implements FastEntrySet<K> {
		//vars
		
		//constructor
		
		//public
		public ObjectIterator<Object2IntMap.Entry <K> > iterator() {
			return new AbstractObjectIterator<Object2IntMap.Entry<K>>() {
				int next = 0;
				public boolean hasNext() {
					return next < size;
				}
				@SuppressWarnings("unchecked")
				public Entry <K> next() {
					if (!hasNext()) {
						throw new NoSuchElementException();
					}
					return new AbstractObject2IntMap.BasicEntry<K>((K) key[next], value[next++]);
				}
			};
		}
		public ObjectIterator<Object2IntMap.Entry<K>> fastIterator() {
			return new AbstractObjectIterator<Object2IntMap.Entry<K>>() {
				int next = 0;
				final BasicEntry<K> entry = new BasicEntry<K>(null, 0);
				public boolean hasNext() {
					return next < size;
				}
			    @SuppressWarnings("unchecked")
			    public Entry<K> next() {
			    	if (!hasNext()) {
			    		throw new NoSuchElementException();
			    	}
			    	entry.key = (K) key[next];
			    	entry.value = value[next++];
			    	return entry;
			    }
			};
		}
		
		public int size() {
			return size;
		}
		
		@SuppressWarnings("unchecked")
		public boolean contains(Object o) {
			if (!(o instanceof Map.Entry)) {
				return false;
			}
			final Map.Entry<K, Integer> e = (Map.Entry<K, Integer>) o;
			final K k = e.getKey();
			return Object2IntArrayMap.this.containsKey(k) && (Object2IntArrayMap.this.getInt(k) == e.getValue().intValue());
		}
		
		//private
		
	}
	
	private int findKey( final Object k ) {
		final Object[] key = this.key;
		for (int i = size; i-- != 0;) {
			if (((key[i]) == null ? k == null : (key[i]).equals(k))) {
				return i;
			}
		}
		return -1;
	}
	
	private void writeObject(ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();
		for (int i = 0; i < size; i++) {
			s.writeObject(key[i]);
			s.writeInt(value[i]);
		}
	}
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		key = new Object[size];
		value = new int[size];
		
		for (int i = 0; i < size; i++) {
			key[i] = s.readObject();
			value[i] = s.readInt();
		}
	}
}