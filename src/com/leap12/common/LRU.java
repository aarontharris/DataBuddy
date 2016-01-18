package com.leap12.common;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.leap12.common.LinkedList.Node;

/** Thread Safe */
public class LRU<T> {

	public static interface AttainDelegate<T> {
		public T attain( String key );
	}

	private final Map<String, T> cache;

	public LRU( final int maxEntries ) {
		this.maxSize = maxEntries;
		this.cache = new LinkedHashMap<String, T>( maxEntries, 0.75F, true ) {
			private static final long serialVersionUID = -1236481390177598762L;

			@Override
			protected boolean removeEldestEntry( Map.Entry<String, T> eldest ) {
				return size() > maxEntries;
			}
		};
	}

	public T attain2( String key, AttainDelegate<T> delegate ) {
		T item = get( key );
		if ( item == null ) {
			item = delegate.attain( key );
			put( key, item );
		}
		return item;
	}

	private void put( String key, T value ) {
		synchronized ( cache ) {
			cache.put( key, value );
		}
	}

	private T get( String key ) {
		synchronized ( cache ) {
			return cache.get( key );
		}
	}

	private final Map<String, ReentrantLock> locks = new HashMap<>();
	private final Map<String, Node<Pair<String, T>>> itemMap = new ConcurrentHashMap<>();
	private final LinkedList<Pair<String, T>> items = new LinkedList<>();
	private final int maxSize;

	public LRU( int maxSize, int x ) {
		this.maxSize = maxSize;
		cache = null;
	}

	private synchronized ReentrantLock attainLock( String key ) {
		ReentrantLock lock = locks.get( key );
		if ( lock == null ) {
			lock = new ReentrantLock();
			locks.put( key, lock );
		}
		return lock;
	}

	public T attain( String key, AttainDelegate<T> delegate ) {
		ReentrantLock lock = attainLock( key );
		lock.lock();
		try {
			Node<Pair<String, T>> cachedItem = itemMap.get( key );

			if ( cachedItem != null ) {
				// items.relinkLast( cachedItem );
				items.remove( cachedItem.getItem() );
				items.add( cachedItem.getItem() );
				itemMap.put( key, cachedItem );
			} else {
				T item = delegate.attain( key );
				cachedItem = new Node<>( new Pair<>( key, item ) );
				items.add( cachedItem.getItem() );
				itemMap.put( key, cachedItem );
			}

			if ( items.size() > maxSize ) {
				T poll = poll();
			}

			if ( cachedItem.getItem() == null ) {
				Log.d( "Cached Item is Null!" );
			}
			return cachedItem.getItem().b;
		} finally {
			lock.unlock();
		}
	}

	private T poll() {
		Pair<String, T> cachedItem = items.poll();
		Log.d( "removing %s", cachedItem );
		if ( cachedItem != null ) {
			itemMap.remove( cachedItem.a );
			return cachedItem.b;
		}
		return null;
	}

	public static String build( int id ) {
		String out = "val_" + id;
		try {
			counter.incrementAndGet();
			// Log.d( "building " + out );
			Thread.sleep( 10 );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		// Log.d( "build %s return %s", id, out );
		return out;
	}

	static final AtomicInteger counter = new AtomicInteger();

	public static void main( String args[] ) {
		final int max = 10;
		final int tot = 10000;
		final int variations = 100;
		final long[] times = new long[tot];
		final long mainStart = System.currentTimeMillis();
		final CountDownLatch latch = new CountDownLatch( tot );

		// String item = build( id );
		final LRU<String> lru = new LRU<>( max );
		for ( int i = 0; i < tot; i++ ) {
			final int itr = i;
			final int id = i % variations;
			String key = "key_" + id;

			new Thread( new Runnable() {
				@Override
				public void run() {
					long start = System.currentTimeMillis();
					String item = lru.attain( key, k -> build( itr ) );
					if ( item == null ) {
						throw new NullPointerException( "Got Null" );
					} else {
						// Log.d( "Got: " + item );
					}
					long stop = System.currentTimeMillis();
					long time = stop - start;
					times[itr] = time;
					latch.countDown();
				}
			} ).start();
		}

		try {
			latch.await();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		long runTime = System.currentTimeMillis() - mainStart;
		long procTime = 0;
		for ( long l : times ) {
			procTime += l;
		}
		float avg = ( (float) procTime / (float) tot );
		float perSec = 1.0f / avg;
		Log.d( "Ran For: T=%s, P=%s, avg=%s or %s/s, built=%s", runTime, procTime, avg, perSec, counter.intValue() );
	}
}
