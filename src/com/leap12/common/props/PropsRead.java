package com.leap12.common.props;

import java.util.Set;

import com.leap12.common.Log;

public interface PropsRead {

	@SuppressWarnings( "serial" )
	public class FieldException extends RuntimeException {
		public FieldException( String msg ) {
			super( msg );
		}

		public FieldException( Exception e ) {
			super( e );
		}

		public FieldException( String msg, Exception e ) {
			super( msg, e );
		}
	}



	@SuppressWarnings( "serial" )
	public class ConvertException extends FieldException {
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



	@SuppressWarnings( "serial" )
	public class TransformException extends FieldException {
		public TransformException( String msg ) {
			super( msg );
		}

		public TransformException( Exception e ) {
			super( e );
		}

		public TransformException( String msg, Exception e ) {
			super( msg, e );
		}
	}



	@SuppressWarnings( "serial" )
	public class MissingFieldException extends FieldException {
		public MissingFieldException( String msg ) {
			super( msg );
		}

		public MissingFieldException( Exception e ) {
			super( e );
		}

		public MissingFieldException( String msg, Exception e ) {
			super( msg, e );
		}
	}



	/**
	 * Functional Interface (Lambda) allowing the developer fine control of the conditional output.<br>
	 * Intended to provide a hook when searching for a value associated with a key and dealing with the result.<br>
	 */
	public interface PropsConverter<T> {
		/**
		 * @param key the key associated with the desired value
		 * @param found true = key was found
		 * @param err most likely a parse error but could be any error that occurred in the search->tranform->convert process.
		 * @param post {@link PropsTransformer} value found for the key, or null if key not found (could be null even if key was found).
		 */
		T convert( String key, boolean found, Exception err, T in ) throws Exception;
	}



	/**
	 * Functional Interface (Lambda) allowing the developer fine control of the parsing process.<b>
	 * Intended to provide a hook when searching for a value associated with a key and the result needs to be transformed from one type to another.<br>
	 *
	 * @param <F> type from
	 * @param <T> type to
	 */
	public interface PropsTransformer<F, T> {
		/**
		 * @param key the key associated with the desired value
		 * @param found true = key was found
		 * @param err Any error occuring during search or potentially if the found value did not match Type F?
		 * @param the pre-transform value found for the key, or null if key not found (could be null even if key was found).
		 */
		T transform( String key, boolean found, Exception err, F in );
	}

	public boolean containsKey( String key );

	default boolean containsKeys( String keyA, String keyB ) {
		return containsKey( keyA ) && containsKey( keyB );
	}

	default boolean containsKeys( String... keys ) {
		for ( String key : keys ) {
			if ( !containsKey( key ) ) {
				return false;
			}
		}
		return true;
	}

	/** Return null if key is not present */
	public String getString( String key );

	public int size();

	public Set<String> keySet();

	/**
	 * Very fine control of the process when obtaining a value from the props.<br>
	 * 
	 * @param key the key associated with the desired value
	 * @param transform your opportunity to define the String -> T transformation (parsing).
	 * @param convert your opportunity to inspect the post-transform result and change the returned value.
	 * @see PropsTransformer
	 * @see PropsConverter
	 * @throws TransformException if an error occurred during transform.
	 * @throws ConvertException if an error occurred during conversion.
	 */
	default <T> T getValue( String key, PropsTransformer<String, T> transform, PropsConverter<T> convert ) {
		T out = null;
		boolean found = false;
		Exception exception = null;

		try {
			if ( containsKey( key ) ) {
				found = true;
				String val = getString( key );
				if ( val != null ) {
					try {
						out = transform.transform( key, found, exception, val );
					} catch ( Exception e ) {
						throw new TransformException( "Transform error for '" + key + "'", e );
					}
				}
			}
		} catch ( Exception e ) {
			exception = e;
			Log.e( e );
		}

		if ( convert != null ) {
			try {
				out = convert.convert( key, found, exception, out );
			} catch ( MissingFieldException | TransformException e ) {
				throw e;
			} catch ( Exception e ) {
				throw new ConvertException( "Conversion error for '" + key + "'", e );
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
	 * @see PropsConverter
	 * @return
	 */
	default String getString( String key, PropsConverter<String> convert ) {
		return getValue( key, ( k, f, e, n ) -> n, convert );
	}

	/** See {@link #getString(String, PropsConverter)} for usage */
	default String getString( String key, final String defVal ) {
		return getString( key, ( k, f, e, n ) -> f ? n : defVal );
	}

	/** @param defVal returned when key is not present, errors are thrown. */
	default String getStringChecked( final String key, final String defVal ) throws ConvertException, TransformException, Exception {
		return getString( key, ( k, f, e, n ) -> {
			if ( e != null ) {
				throw e;
			} else
				return f ? n : defVal;
		} );
	}

	/** @param error thrown when key is not present in addition to transform or conversion errors */
	default String getStringRequired( final String key ) throws ConvertException, TransformException, MissingFieldException {
		return getString( key, ( k, f, e, n ) -> {
			if ( e != null ) {
				throw e;
			} else if ( !f ) {
				throw new MissingFieldException( "Missing required key " + key );
			}
			return n;
		} );
	}

	/** See {@link #getString(String, PropsConverter)} for usage */
	default Boolean getBoolean( String key, PropsConverter<Boolean> convert ) {
		return getValue( key, ( k, f, e, n ) -> Boolean.valueOf( n ), convert );
	}

	/** @param defVal returned when key is not present or parse error as invalid data is considered not present. */
	default Boolean getBoolean( final String key, final boolean defVal ) {
		return getBoolean( key, ( k, f, e, n ) -> f ? n : defVal );
	}

	/** @param defVal returned when key is not present, errors are thrown. */
	default Boolean getBooleanChecked( final String key, final boolean defVal ) throws ConvertException, TransformException, Exception {
		return getBoolean( key, ( k, f, e, n ) -> {
			if ( e != null ) {
				throw e;
			} else
				return f ? n : defVal;
		} );
	}

	/** @param error thrown when key is not present in addition to transform or conversion errors */
	default Boolean getBooleanRequired( final String key ) throws ConvertException, TransformException, MissingFieldException {
		return getBoolean( key, ( k, f, e, n ) -> {
			if ( e != null ) {
				throw e;
			} else if ( !f )
				throw new MissingFieldException( "Missing required key " + key );
			return n;
		} );
	}

	/** See {@link #getString(String, PropsConverter)} for usage */
	default Integer getIntegerConvert( String key, PropsConverter<Integer> convert ) {
		return getValue( key, ( k, f, e, n ) -> Integer.valueOf( n ), convert );
	}

	/** @param defVal returned when key is not present or parse error as invalid data is considered not present. */
	default Integer getInteger( final String key, final Integer defVal ) {
		return getIntegerConvert( key, ( k, f, e, n ) -> f ? n : defVal );
	}

	/** @param defVal returned when key is not present, errors are thrown. */
	default Integer getIntegerChecked( final String key, final Integer defVal ) throws ConvertException, TransformException, Exception {
		return getIntegerConvert( key, ( k, f, e, n ) -> {
			if ( e != null ) {
				throw e;
			} else
				return f ? n : defVal;
		} );
	}

	/** @param error thrown when key is not present in addition to transform or conversion errors */
	default Integer getIntegerRequired( final String key ) throws ConvertException, TransformException, MissingFieldException {
		return getIntegerConvert( key, ( k, f, e, n ) -> {
			if ( e != null ) {
				throw e;
			} else if ( !f )
				throw new MissingFieldException( "Missing required key " + key );
			return n;
		} );
	}

	/** See {@link #getString(String, PropsConverter)} for usage */
	default Long getLong( String key, PropsConverter<Long> convert ) {
		return getValue( key, ( k, f, e, n ) -> Long.valueOf( n ), convert );
	}

	/** @param defVal returned when key is not present or parse error as invalid data is considered not present. */
	default Long getLong( final String key, final long defVal ) {
		return getLong( key, ( k, f, e, n ) -> f ? n : defVal );
	}

	/** @param defVal returned when key is not present, errors are thrown. */
	default Long getLongChecked( final String key, final long defVal ) throws ConvertException, TransformException, Exception {
		return getLong( key, ( k, f, e, n ) -> {
			if ( e != null ) {
				throw e;
			} else
				return f ? n : defVal;
		} );
	}

	/** @param error thrown when key is not present in addition to transform or conversion errors */
	default Long getLongRequired( final String key ) throws ConvertException, TransformException, MissingFieldException {
		return getLong( key, ( k, f, e, n ) -> {
			if ( e != null ) {
				throw e;
			} else if ( !f )
				throw new MissingFieldException( "Missing required key " + key );
			return n;
		} );
	}

	/** See {@link #getString(String, PropsConverter)} for usage */
	default Float getFloat( String key, PropsConverter<Float> convert ) {
		return getValue( key, ( k, f, e, n ) -> Float.valueOf( n ), convert );
	}

	/** @param defVal returned when key is not present or parse error as invalid data is considered not present. */
	default Float getFloat( final String key, final float defVal ) {
		return getFloat( key, ( k, f, e, n ) -> f ? n : defVal );
	}

	/** @param defVal returned when key is not present, errors are thrown. */
	default Float getFloatUnchecked( final String key, final float defVal ) throws ConvertException, TransformException, Exception {
		return getFloat( key, ( k, f, e, n ) -> {
			if ( e != null ) {
				throw e;
			} else
				return f ? n : defVal;
		} );
	}

	/** @param error thrown when key is not present in addition to transform or conversion errors */
	default Float getFloatRequired( final String key ) throws ConvertException, TransformException, MissingFieldException {
		return getFloat( key, ( k, f, e, n ) -> {
			if ( e != null ) {
				throw e;
			} else if ( !f )
				throw new MissingFieldException( "Missing required key " + key );
			return n;
		} );
	}

	/** See {@link #getString(String, PropsConverter)} for usage */
	default Double getDouble( String key, PropsConverter<Double> convert ) {
		return getValue( key, ( k, f, e, n ) -> Double.valueOf( n ), convert );
	}

	/** @param defVal returned when key is not present or parse error as invalid data is considered not present. */
	default Double getDouble( final String key, final double defVal ) {
		return getDouble( key, ( k, f, e, n ) -> f ? n : defVal );
	}

	/** @param defVal returned when key is not present, errors are thrown. */
	default Double getDoubleChecked( final String key, final double defVal ) throws ConvertException, TransformException, Exception {
		return getDouble( key, ( k, f, e, n ) -> {
			if ( e != null ) {
				throw e;
			} else
				return f ? n : defVal;
		} );
	}

	/** @param error thrown when key is not present in addition to transform or conversion errors */
	default Double getDoubleRequired( final String key ) throws ConvertException, TransformException, MissingFieldException {
		return getDouble( key, ( k, f, e, n ) -> {
			if ( e != null ) {
				throw e;
			} else if ( !f )
				throw new MissingFieldException( "Missing required key " + key );
			return n;
		} );
	}
}
