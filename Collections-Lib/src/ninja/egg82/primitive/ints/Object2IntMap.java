package ninja.egg82.primitive.ints;

import java.util.Map;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public interface Object2IntMap<K> extends Object2IntFunction<K>, Map<K, Integer> {
	public interface FastEntrySet<K> extends ObjectSet<Object2IntMap.Entry<K>> {
		public ObjectIterator<Object2IntMap.Entry<K>> fastIterator();
	}
	interface Entry <K> extends Map.Entry <K,Integer> {
		int setValue(int value);
		int getIntValue();
	}
	
	ObjectSet<Map.Entry<K, Integer>> entrySet();
	ObjectSet<Object2IntMap.Entry<K>> object2IntEntrySet();
	ObjectSet <K> keySet();
	IntCollection values();
	boolean containsValue(int value);
}