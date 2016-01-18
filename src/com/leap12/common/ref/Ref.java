package com.leap12.common.ref;

public interface Ref<T> {

	public static class MutableRef<T> implements Ref<T> {
		private T obj;

		public MutableRef( T obj ) {
			set( obj );
		}

		public void set( T obj ) {
			this.obj = obj;
		}

		@Override
		public T get() {
			return obj;
		}
	}

	public T get();

}
