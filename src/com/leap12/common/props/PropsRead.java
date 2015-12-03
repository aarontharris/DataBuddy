package com.leap12.common.props;

import com.leap12.common.Log;

public interface PropsRead {

	@SuppressWarnings( "serial" )
	public class ConvertException extends RuntimeException {
		public ConvertException( String msg ) {
			super( msg );
		}

		public ConvertException( Exception e ) {
			super( e );
		}

		public ConvertException( String msg, Exception e ) {
			super( msg, e );
		}
	}



	public interface Converter<T> {
		T convert( String key, boolean found, Exception err, T in );
	}



	public interface Transformer<F, T> {
		T transform( String key, boolean found, Exception err, F in );
	}

	public boolean containsKey( String key );

	/** Return null if key is not present */
	public String getString( String key );

	default <T> T getValue( String key, Transformer<String, T> transform, Converter<T> convert ) {
		T out = null;
		boolean found = false;
		Exception exception = null;

		try {
			if ( containsKey( key ) ) {
				found = true;
				String val = getString( key );
				if ( val != null ) {
					out = transform.transform( key, found, exception, val );
				}
			}
		} catch ( Exception e ) {
			exception = e;
			Log.e( e );
		}

		if ( convert != null ) {
			try {
				out = convert.convert( key, found, exception, out );
			} catch ( Exception e ) {
				throw new ConvertException( e );
			}
		}
		return out;
	}

	/**
	 * Get the String value associated with the given key.<br>
	 * Optionally inspect the value to be returned and alter it with Converter.<br>
	 * 
	 * <pre>
	 * 
	 * // Just null
	 * getString( "myKey", null ); // returns the value as is or null when key is not present.
	 * 
	 * // Java 8 lambda
	 * getString( "myKey", ( k,f,e,n ) -> n == null ? "Unavailable" : n ); // returns the value as is, except when null, then returns "Unavailable"
	 * 
	 * // Java 7 anon-inner
	 * getString( "myKey", new Converter<String>() {
	 *     public String convert( String key, boolean found, Exception err, String in ) {
	 *         return in == null ? "Unavailable"; // returns the value as is, except when null, then returns "Unavailable"
	 *     }
	 * });
	 * </pre>
	 * 
	 * @param key Key used to search for a value
	 * @param convert convert the output
	 * @return
	 */
	default String getString( String key, Converter<String> convert ) {
		return getValue( key, ( k, f, e, n ) -> n, convert );
	}

	/** See {@link #getString(String, Converter)} for usage */
	default String getString( String key, final String defVal ) {
		return getString( key, ( k, f, e, n ) -> f ? n : defVal );
	}

	/** See {@link #getString(String, Converter)} for usage */
	default Boolean getBoolean( String key, Converter<Boolean> convert ) {
		return getValue( key, ( k, f, e, n ) -> Boolean.valueOf( n ), convert );
	}

	/** @param defVal returned when key is not present or parse error as invalid data is considered not present. */
	default Boolean getBoolean( final String key, final boolean defVal ) {
		return getBoolean( key, ( k, f, e, n ) -> f ? n : defVal );
	}

	/** See {@link #getString(String, Converter)} for usage */
	default Integer getInteger( String key, Converter<Integer> convert ) {
		return getValue( key, ( k, f, e, n ) -> Integer.valueOf( n ), convert );
	}

	/** @param defVal returned when key is not present or parse error as invalid data is considered not present. */
	default Integer getInteger( final String key, final int defVal ) {
		return getInteger( key, ( k, f, e, n ) -> f ? n : defVal );
	}

	/** See {@link #getString(String, Converter)} for usage */
	default Long getLong( String key, Converter<Long> convert ) {
		return getValue( key, ( k, f, e, n ) -> Long.valueOf( n ), convert );
	}

	/** @param defVal returned when key is not present or parse error as invalid data is considered not present. */
	default Long getLong( final String key, final long defVal ) {
		return getLong( key, ( k, f, e, n ) -> f ? n : defVal );
	}

	/** See {@link #getString(String, Converter)} for usage */
	default Float getFloat( String key, Converter<Float> convert ) {
		return getValue( key, ( k, f, e, n ) -> Float.valueOf( n ), convert );
	}

	/** @param defVal returned when key is not present or parse error as invalid data is considered not present. */
	default Float getFloat( final String key, final float defVal ) {
		return getFloat( key, ( k, f, e, n ) -> f ? n : defVal );
	}

	/** See {@link #getString(String, Converter)} for usage */
	default Double getDouble( String key, Converter<Double> convert ) {
		return getValue( key, ( k, f, e, n ) -> Double.valueOf( n ), convert );
	}

	/** @param defVal returned when key is not present or parse error as invalid data is considered not present. */
	default Double getDouble( final String key, final double defVal ) {
		return getDouble( key, ( k, f, e, n ) -> f ? n : defVal );
	}
}
