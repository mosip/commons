package io.mosip.kernel.auth.defaultimpl.util;

import org.apache.commons.collections.map.LRUMap;

import lombok.Getter;

/**
 * Local cache to store admin token
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 *
 * @param <K> type of key
 * @param <T> type of value
 */
public class MemoryCache<K, T> {

	/**
	 * LRU map holding {@link CacheObject} wrappers.
	 */
	private LRUMap cacheMap;

	/**
	 * Cache entry with last-access time and payload.
	 */
	protected class CacheObject {
		/**
		 * Epoch millis when this entry was last read or written.
		 */
		@Getter
		private long lastAccessed = System.currentTimeMillis();
		/**
		 * Cached value.
		 */
		private T value;

		/**
		 * Wraps {@code value} with the current access time.
		 *
		 * @param value value to store
		 */
		protected CacheObject(T value) {
			this.value = value;
		}
	}

	/**
	 * Creates an LRU cache with a maximum number of entries.
	 *
	 * @param maxItems maximum distinct keys retained
	 */
	public MemoryCache(int maxItems) {
		cacheMap = new LRUMap(maxItems);
	}

	/**
	 * Inserts or replaces the value for {@code key}.
	 *
	 * @param key   cache key
	 * @param value value to store
	 */
	public void put(K key, T value) {
		synchronized (cacheMap) {
			cacheMap.put(key, new CacheObject(value));
		}
	}

	/**
	 * Returns the value for {@code key} and updates last-accessed time.
	 *
	 * @param key cache key
	 * @return stored value, or {@code null} if absent
	 */
	@SuppressWarnings("unchecked")
	public T get(K key) {
		synchronized (cacheMap) {
			CacheObject c = (CacheObject) cacheMap.get(key);
			if (c == null)
				return null;
			else {
				c.lastAccessed = System.currentTimeMillis();
				return c.value;
			}
		}
	}

	/**
	 * Removes the entry for {@code key} if present.
	 *
	 * @param key cache key
	 */
	public void remove(K key) {
		synchronized (cacheMap) {
			cacheMap.remove(key);
		}
	}

	/**
	 * Number of entries currently in the cache.
	 *
	 * @return size
	 */
	public int size() {
		synchronized (cacheMap) {
			return cacheMap.size();
		}
	}
}
