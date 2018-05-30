package ninja.egg82.primitive.ints;

import java.util.Iterator;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.AbstractIntIterator;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public abstract class AbstractObject2IntMap<K> extends AbstractObject2IntFunction<K> implements Object2IntMap<K> {
	//vars
	private static final long serialVersionUID = -4940583368468432370L;
	
	//constructor
	protected AbstractObject2IntMap() {
		super();
	}
	
	//public
	public boolean containsValue(Object ov) {
		return containsValue(((Integer) ov).intValue());
	}
	public boolean containsValue(int v) {
		return values().contains(v);
	}
	public boolean containsKey(Object k) {
		return keySet().contains(k);
	}
	
	@SuppressWarnings("unchecked")
	public void putAll(Map<? extends K,? extends Integer> m) {
		int n = m.size();
		final Iterator<? extends Map.Entry<? extends K,? extends Integer>> i = m.entrySet().iterator();
		if (m instanceof Object2IntMap) {
			Object2IntMap.Entry <? extends K> e;
			while (n-- != 0) {
				e = (Object2IntMap.Entry <? extends K>)i.next();
				put(e.getKey(), e.getIntValue());
			}
		} else {
			Map.Entry<? extends K,? extends Integer> e;
			while (n-- != 0) {
				e = i.next();
				put(e.getKey(), e.getValue());
			}
		}
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public static class BasicEntry<K> implements Object2IntMap.Entry<K> {
		//vars
		protected K key;
		protected int value;
		
		//constructor
		public BasicEntry( final K key, final Integer value ) {
			this.key = (key);
			this.value = ((value).intValue());
		}
		public BasicEntry( final K key, final int value ) {
			this.key = key;
			this.value = value;
		}
		
		//public
		public K getKey() {
			return (key);
		}
		public Integer getValue() {
			return (Integer.valueOf(value));
		}
		public int getIntValue() {
			return value;
		}
		
		public int setValue( final int value ) {
			throw new UnsupportedOperationException();
		}
		public Integer setValue( final Integer value ) {
			return Integer.valueOf(setValue(value.intValue()));
		}
		
		public boolean equals( final Object o ) {
			if (!(o instanceof Map.Entry)) return false;
			Map.Entry<?,?> e = (Map.Entry<?,?>)o;
			return ( (key) == null ? ((e.getKey())) == null : (key).equals((e.getKey())) ) && ( (value) == (((((Integer)(e.getValue())).intValue()))) );
		}
		public int hashCode() {
			return ( (key) == null ? 0 : (key).hashCode() ) ^ (value);
		}
		public String toString() {
			return key + "->" + value;
		}
		
		//private
		
	}
	
	public ObjectSet<K> keySet() {
		return new AbstractObjectSet<K>() {
			public boolean contains(final Object k) {
				return containsKey(k);
			}
			public int size() {
				return AbstractObject2IntMap.this.size();
			}
			public void clear() {
				AbstractObject2IntMap.this.clear();
			}
			public ObjectIterator<K> iterator() {
				return new AbstractObjectIterator<K>() {
					final ObjectIterator<Map.Entry<K, Integer>> i = entrySet().iterator();
					public K next() {
						return ((Object2IntMap.Entry<K>)i.next()).getKey();
					}
					public boolean hasNext() {
						return i.hasNext();
					}
				};
			}
		};
	}
	public IntCollection values() {
		return new AbstractIntCollection() {
			public boolean contains(final int k) {
				return containsValue(k);
			}
			public int size() {
				return AbstractObject2IntMap.this.size();
			}
			public void clear() {
				AbstractObject2IntMap.this.clear();
			}
			public IntIterator iterator() {
				return new AbstractIntIterator() {
					final ObjectIterator<Map.Entry<K, Integer>> i = entrySet().iterator();
					public int nextInt() {
						return ((Object2IntMap.Entry <K>)i.next()).getIntValue();
					}
					public boolean hasNext() {
						return i.hasNext();
					}
				};
			}
		};
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ObjectSet<Map.Entry<K, Integer>> entrySet() {
		return (ObjectSet)object2IntEntrySet();
	}
	public int hashCode() {
		int h = 0, n = size();
		final ObjectIterator<? extends Map.Entry<K,Integer>> i = entrySet().iterator();
		
		while (n-- != 0) {
			h += i.next().hashCode();
		}
		return h;
	}
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if(!(o instanceof Map)) {
			return false;
		}
		
		Map<?, ?> m = (Map<?, ?>)o;
		if (m.size() != size()) {
			return false;
		}
		return entrySet().containsAll(m.entrySet());
	}
	public String toString() {
		final StringBuilder s = new StringBuilder();
		final ObjectIterator<? extends Map.Entry<K, Integer>> i = entrySet().iterator();
		int n = size();
		Object2IntMap.Entry <K> e;
		boolean first = true;
		
		s.append("{");
		while (n-- != 0) {
			if (first) {
				first = false;
			} else {
				s.append(", ");
			}
			e = (Object2IntMap.Entry<K>) i.next();
			if (this == e.getKey()) {
				s.append("(this map)");
			} else {
				s.append(String.valueOf(e.getKey()));
			}
			s.append("=>");
			s.append(String.valueOf(e.getIntValue()));
		}
		s.append("}");
		
		return s.toString();
	}
}