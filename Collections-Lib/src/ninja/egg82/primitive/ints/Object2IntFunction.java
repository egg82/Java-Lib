package ninja.egg82.primitive.ints;

import it.unimi.dsi.fastutil.Function;

public interface Object2IntFunction <K> extends Function<K, Integer> {
	int put(K key, int value);
	int getInt(Object key);
	int removeInt(Object key);
	void defaultReturnValue(int rv);
	int defaultReturnValue();
}
