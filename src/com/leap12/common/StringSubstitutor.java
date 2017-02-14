package com.leap12.common;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 *
 * Non-Typed Use-Case:
 * IN:  "Hello my name is {name} and I am {age} years old", "name", "Aaron", "age", 100
 * OUT: "Hello my name is Aaron and I am 100 years old"
 * 
 * Typed Use-Case:
 * IN:  "Hello my name is {str$name} and I am {int$age} years old", "name", "Aaron", "age", 100
 * OUT: "Hello my name is Aaron and I am 100 years old"
 * 
 * 
 * 
 * Non-Typed:
 * {name} // name is enclosed in curly braces and only replaced if there is a matching key-value pair
 * type - defaults to "Str" indicates how to parse the value.
 * name - indicates which key to use from the substition map, when key is not present the {name} token is left as is.
 * 
 * Typed:
 * {type$name} // type and name are enclosed in curly braces and separated by $.
 * type - indicates how to parse the value.
 * name - indicates which key to use from the substition map, when key is not present, null is assumed and "" is substituted.
 * 
 * 
 * 
 * Conventions:
 * Types beginning with a lowercase letter indicate the value is required.
 * Types beginning with an uppercase letter indicate the value is optional.
 * 
 * 
 * 
 * valid types are:
 * "str" = NonNull and internally stored as a String
 * "Str" = Nullable and internally stored as a String
 * "int" = NonNull and internally stored as a Integer
 * "Int" = Nullable and internally stored as a Integer
 * "float" = NonNull and internally stored as a Float
 * "Float" = Nullable and internally stored as a Float
 * "bool" = NonNull and internally stored as a Boolean
 * "Bool" = Nullable and internally stored as a Boolean
 * 
 * Customization of behavior:
 * {@link #onValidateCustomType(String, String, Object)} // accept your own types.
 * {@link #onTypeCast(Class, String, String, Object)} // how to handle cast errors of non required fields
 * 
 * Be aware of Exceptions thrown:
 * {@link SubstitutionException}
 * {@link SubstitutionRequiredException}
 * {@link SubstitutionCastException}
 * {@link SubstitutionInvalidFormatException}
 * {@link SubstitutionUnsupportedException}
 *
 * </pre>
 */
public class StringSubstitutor {
	private static final boolean REQUIRED = true;
	private static final boolean NOTREQUIRED = false;
	private static final String EMPTY_STRING = "";



	private static enum SubType {
		Tstr( "str", String.class, REQUIRED ),
		TStr( "Str", String.class, NOTREQUIRED ),
		Tint( "int", Integer.class, REQUIRED ),
		TInt( "Int", Integer.class, NOTREQUIRED ),
		Tfloat( "float", Float.class, REQUIRED ),
		TFloat( "Float", Float.class, NOTREQUIRED ),
		Tbool( "bool", Boolean.class, REQUIRED ),
		TBool( "Bool", Boolean.class, NOTREQUIRED );

		private static Map<String, SubType> typeMap = new HashMap<>(); // for reverse lookup

		static {
			for ( SubType type : SubType.values() ) {
				typeMap.put( type.getSymbol(), type );
			}
		}

		/**
		 * Symbol to SubType translation
		 *
		 * @param value the symbol representing the desired type
		 * @return null when the value is not a default implementation type
		 */
		@Nullable
		public static SubType toType( String value ) {
			return typeMap.get( value );
		}

		private String mSymbol;
		private Class mType;
		private boolean mRequired;

		SubType( String symbol, Class type, boolean required ) {
			this.mSymbol = symbol;
			this.mType = type;
			this.mRequired = required;
		}

		public SubType fromString( String value ) {
			return null;
		}

		public String getSymbol() {
			return mSymbol;
		}

		public Class getType() {
			return mType;
		}

		public boolean isRequired() {
			return mRequired;
		}

	}



	/**
	 * General exceptions - something related to {@link StringSubstitutor} but not specific.
	 * Also a base-type for all {@link StringSubstitutor} exceptions.
	 */
	public static class SubstitutionException extends Exception {
		public SubstitutionException( Exception exception ) {
			super( exception );
		}

		public SubstitutionException( String message ) {
			super( message );
		}

		public SubstitutionException( String message, Exception exception ) {
			super( message, exception );
		}
	}



	/**
	 * When a field is required but missing.
	 */
	public static class SubstitutionRequiredException extends SubstitutionException {
		public SubstitutionRequiredException( Exception exception ) {
			super( exception );
		}

		public SubstitutionRequiredException( String message ) {
			super( message );
		}

		public SubstitutionRequiredException( String message, Exception exception ) {
			super( message, exception );
		}
	}



	/**
	 * When the given value is not an instance of the expected type.
	 */
	public static class SubstitutionCastException extends SubstitutionException {
		public SubstitutionCastException( Exception exception ) {
			super( exception );
		}

		public SubstitutionCastException( String message ) {
			super( message );
		}

		public SubstitutionCastException( String message, Exception exception ) {
			super( message, exception );
		}
	}



	/**
	 * When the value does not meet the format expectations for the type.
	 */
	public static class SubstitutionInvalidFormatException extends SubstitutionException {
		public SubstitutionInvalidFormatException( Exception exception ) {
			super( exception );
		}

		public SubstitutionInvalidFormatException( String message ) {
			super( message );
		}

		public SubstitutionInvalidFormatException( String message, Exception exception ) {
			super( message, exception );
		}
	}



	/**
	 * When the type is not a default implementation type or not identified as a custom type.
	 */
	public static class SubstitutionUnsupportedException extends SubstitutionException {
		public SubstitutionUnsupportedException( Exception exception ) {
			super( exception );
		}

		public SubstitutionUnsupportedException( String message ) {
			super( message );
		}

		public SubstitutionUnsupportedException( String message, Exception exception ) {
			super( message, exception );
		}
	}

	/**
	 * <pre>
	 * List of default implementation types
	 * Does not include custom types
	 * </pre>
	 */
	public static List<String> getTypes() {
		List<String> out = new ArrayList<>();
		for ( SubType type : SubType.values() ) {
			out.add( type.getSymbol() );
		}
		return out;
	}

	public static Float promote( Object o ) {
		return null;
	}

	public StringSubstitutor() {
	}

	/**
	 * <pre>
	 * Override to deal with a type mismatch found on a field.
	 * The returned value will be used for substitution or fail.
	 * The given value at this point should never be null.
	 * 
	 * Note:
	 * The system will first try to promote the value in the event of:
	 * System expects Integer and is given "100"
	 * and similar...
	 * take a look at the promote() series methods -- all overridable for custom behavior.
	 * 
	 * Default behavior:
	 * When field is required = throw {@link SubstitutionCastException}
	 * When field is not required return null;
	 * 
	 * Best Practices:
	 * throw {@link SubstitutionCastException} when the given value is not an instance of the expected type.
	 * throw {@link SubstitutionInvalidFormatException} when the given value doesn't match your desired format.
	 * </pre>
	 *
	 * @param requiredType - the java class type expected relating to the typeStr.
	 * @param typeStr - the substitution type used for type matching.
	 * @param name - the key for looking up the value
	 * @param value - the value found associated with the given name in the given data
	 * @return used for substitution. If null (default), then a literal empty string will be used for substitution.
	 */
	@Nullable
	protected Object onTypeCast( @NonNull Class requiredType, @NonNull String typeStr, @NonNull String name, @NonNull Object value, boolean requiredField )
	        throws SubstitutionException {
		if ( requiredField ) {
			throw new SubstitutionCastException( String.format( "Type mismatch for field '%s' - ReqType='%s' ValType='%s' Value='%s'", name, requiredType,
			        value.getClass(), value ) );
		}
		return null;
	}

	/**
	 * <pre>
	 * Override to handle unknown types.
	 * If the system encounters {type$name} and type is not known to StringSubstitutor, this method will be called.
	 * If you can handle the type, return the resulting value.
	 * If you cannot handle the type, call super.
	 * Default behavior is to throw a {@link SubstitutionUnsupportedException}
	 * 
	 * Best Practices:
	 * throw {@link SubstitutionRequiredException} when type is required but value is null.
	 * throw {@link SubstitutionCastException} when the given value is not an instance of the expected type.
	 * throw {@link SubstitutionInvalidFormatException} when the given value doesn't match your desired format.
	 * call super to throw {@link SubstitutionUnsupportedException} when you are unable to identify the type.
	 * </pre>
	 *
	 * @param typeStr - the substitution type used for type matching.
	 * @param name - the key for looking up the value
	 * @param value - the value found associated with the given name in the given data
	 * @return value to be substituted
	 * @throws SubstitutionException
	 */
	@Nullable
	protected Object onValidateCustomType( @NonNull String typeStr, @NonNull String name, @Nullable Object value ) throws SubstitutionException {
		throw new SubstitutionUnsupportedException( String.format( "Type '%s' is not supported", typeStr ) );
	}

	/**
	 * Attempt to promote an Object to the requiredType or fail.
	 * Note: String literals are redirected to String object overload cases.
	 *
	 * @param requiredType
	 * @param object
	 * @return promoted value or fail
	 */
	protected Object promote( @NonNull Class requiredType, @NonNull Object object ) {

		// very unfortunate that we must do this but String Literals are not String Objects.
		// When doing overloading, a String Literal value will not overload to a signature with a String
		// and instead overloads to a signature with Object.
		// to correct this, we shoehorn anything assignableFrom String and cast it as String
		// to ensure the right path.
		boolean isString = false;
		if ( String.class.isAssignableFrom( object.getClass() ) ) {
			isString = true;
		}

		// FIXME: not sure why this coercion is necessary, polymorphism should work...

		if ( String.class.equals( requiredType ) ) {
			if ( object instanceof String ) {
				return promoteToString( (String) object );
			}
			if ( object instanceof Integer ) {
				return promoteToString( (Integer) object );
			}
			if ( object instanceof Float ) {
				return promoteToString( (Float) object );
			}
			if ( object instanceof Boolean ) {
				return promoteToString( (Boolean) object );
			}
			return promoteToString( object );
		}
		if ( Integer.class.equals( requiredType ) ) {
			if ( object instanceof String ) {
				return promoteToInteger( (String) object );
			}
			if ( object instanceof Integer ) {
				return promoteToInteger( (Integer) object );
			}
			if ( object instanceof Float ) {
				return promoteToInteger( (Float) object );
			}
			return promoteToInteger( object );
		}
		if ( Float.class.equals( requiredType ) ) {
			if ( object instanceof String ) {
				return promoteToFloat( (String) object );
			}
			if ( object instanceof Integer ) {
				return promoteToFloat( (Integer) object );
			}
			if ( object instanceof Float ) {
				return promoteToFloat( (Float) object );
			}
			return promoteToFloat( object );
		}
		if ( Boolean.class.equals( requiredType ) ) {
			if ( object instanceof String ) {
				return promoteToBoolean( (String) object );
			}
			if ( object instanceof Boolean ) {
				return promoteToBoolean( (Boolean) object );
			}
			return promoteToBoolean( object );
		}
		throw new ClassCastException( "cannot cast " + object + " to " + requiredType );
	}

	protected String promoteToString( @NonNull String object ) {
		return object;
	}

	protected String promoteToString( @NonNull Integer object ) {
		return String.valueOf( object );
	}

	protected String promoteToString( @NonNull Float object ) {
		return String.valueOf( object );
	}

	protected String promoteToString( @NonNull Object object ) {
		throw new ClassCastException( "cannot cast " + object + " to String" );
	}

	protected String promoteToString( @NonNull Boolean object ) {
		return String.valueOf( object );
	}

	protected Float promoteToFloat( @NonNull String object ) {
		return Float.parseFloat( object );
	}

	protected Float promoteToFloat( @NonNull Integer object ) {
		return object * 1.0f;
	}

	protected Float promoteToFloat( @NonNull Float object ) {
		return object;
	}

	protected Float promoteToFloat( @NonNull Object object ) {
		throw new ClassCastException( "cannot cast " + object + " to Float" );
	}

	protected Boolean promoteToBoolean( @NonNull String object ) {
		return Boolean.valueOf( object.toLowerCase() );
	}

	protected Boolean promoteToBoolean( @NonNull Boolean object ) {
		return object;
	}

	protected Boolean promoteToBoolean( @NonNull Object object ) {
		throw new ClassCastException( "cannot cast " + object + " to Boolean" );
	}

	protected Integer promoteToInteger( @NonNull Integer object ) {
		return object;
	}

	protected Integer promoteToInteger( @NonNull String object ) {
		return Integer.parseInt( object );
	}

	protected Integer promoteToInteger( @NonNull Float object ) {
		return object.intValue();
	}

	protected Integer promoteToInteger( @NonNull Object object ) {
		throw new ClassCastException( "cannot cast " + object + " to Integer" );
	}

	/**
	 * <pre>
	 * A fancier form of String.format() but with named pairs and validation.
	 * Read the class javadoc {@link StringSubstitutor} for more explicit usage details.
	 * </pre>
	 *
	 * @param format expects to be in the form of "some text {type$name} some text"
	 * @param values map should old keys which map to values that match the expected type defined in format
	 * @return format with substituted values
	 * @throws SubstitutionException
	 * @throws SubstitutionRequiredException
	 * @throws SubstitutionCastException
	 */
	@Nullable
	public String substitute( @NonNull String format, @NonNull Object... keyvals ) throws SubstitutionException {
		Map<String, Object> params = null;
		if ( keyvals != null && keyvals.length > 0 ) {
			if ( ( keyvals.length % 2 ) != 0 ) {
				throw new IllegalStateException( "keyvals must be a multiple of 2" );
			}
			params = new HashMap<>();
			for ( int i = 0; i < keyvals.length; i += 2 ) {
				String key = (String) keyvals[i];
				Object value = keyvals[i + 1];
				params.put( key, value );
			}
		}
		return substitute( format, params );
	}

	private static final String strPattern = "\\{([^}]+)\\}";
	private static final Pattern pattern = Pattern.compile( strPattern ); // Pattern.MULTILINE ?


	/**
	 * <pre>
	 * A fancier form of String.format() but with named pairs and validation.
	 * Read the class javadoc {@link StringSubstitutor} for more explicit usage details.
	 * 
	 * NOTE:
	 * There is a difference in behavior between typed tokens and non-typed tokens.
	 * For typed tokens, a missing key in the values results in an empty string substitution or failure if field is required.
	 * However...
	 * For non-typed tokens, a missing key in the values results in no action, the token is not replaced.
	 * 
	 * REASONING:
	 * Typed tokens are explicit and should reflect literally whats in the substitution values.
	 * Non-Typed tokens are flexible / loose and potentially not even something to be parsed.
	 *
	 * </pre>
	 *
	 * @param format expects to be in the form of "some text {type$name} or {other} some text"
	 * @param values map should old keys which map to values that match the expected type defined in format
	 * @return format with substituted values
	 * @throws SubstitutionException
	 * @throws SubstitutionRequiredException
	 * @throws SubstitutionCastException
	 */
	@Nullable
	public <T> String substitute( @NonNull String format, @NonNull Map<String, T> values ) throws SubstitutionException {
		try {
			if ( values == null ) {
				values = Collections.emptyMap(); // a smell, but safe and convenient in our case.
			}

			Matcher matcher = pattern.matcher( format );

			StringBuilder sb = new StringBuilder();

			int start = 0;
			while ( matcher.find() ) {

				String val = matcher.group();
				String inside = val.substring( 1, val.length() - 1 );
				String typeStr = SubType.TStr.getSymbol();
				boolean anonType = false;
				boolean keyIsPresent = false;

				int pos = inside.indexOf( '$' );
				if ( pos == -1 ) {
					anonType = true;
				} else {
					anonType = false;
					typeStr = inside.substring( 0, pos );
				}
				String name = inside.substring( pos + 1, inside.length() ); // hack to solve matcher.group(n) issue
				keyIsPresent = values.containsKey( name );

				SubType type = SubType.toType( typeStr );

				String replacement = null;

				if ( anonType ) {
					replacement = val;
					if ( keyIsPresent ) {
						Object value = values.get( name );
						replacement = String.valueOf( validateType( type, typeStr, name, value ) );
					}
				} else {
					Object value = values.get( name );
					replacement = String.valueOf( validateType( type, typeStr, name, value ) );
				}

				sb.append( format.substring( start, matcher.start() ) );

				sb.append( replacement );
				start = matcher.end();
			}

			sb.append( format.substring( start, format.length() ) );

			String parsed = sb.toString();
			return parsed;
		} catch ( SubstitutionException e ) {
			throw e;
		} catch ( Exception e ) {
			throw new SubstitutionException( String.format( "format='%s'", format ), e );
		}
	}

	private Object validateType( SubType type, String typeStr, String name, Object value ) throws SubstitutionException {
		// System.out.println(String.format("type=%s, typeStr=%s, name=%s, value=%s", type, typeStr, name, value));
		// if (SubType.TRegx.equals(type) || SubType.Tregx.equals(type)) {
		// return validateRegexType(type, typeStr, name, value);
		if ( null == type ) {
			return validateUnknownType( type, typeStr, name, value );
		} else {
			return validateStandardType( type, typeStr, name, value );
		}
	}

	private Object validateStandardType( SubType type, String typeStr, String name, Object value ) throws SubstitutionException {

		// bail out early if null
		try {
			if ( value == null ) {
				if ( type.isRequired() ) {
					throw new SubstitutionRequiredException( String.format( "Required Type %s was null", typeStr ) );
				}
				return EMPTY_STRING;
			}
		} catch ( SubstitutionException e ) {
			throw e;
		} catch ( Exception e ) {
			System.out.println( String.format( "validateStandardType Failed: type='%s', typeStr='%s', name='%s', value='%s'", type, typeStr, name, value ) );
			throw new SubstitutionException( e );
		}

		// must have a non-null value, lets compare it
		// if its a type match return it
		// first try to promote
		// if that fails, delegate the cast failure
		// if that fails then give up and report
		try {
			if ( type.getType().isAssignableFrom( value.getClass() ) ) {
				return value;
			}
			return promote( type.getType(), value );
		} catch ( Exception ignore ) {
			try {
				return onTypeCast( type.getType(), typeStr, name, value, type.isRequired() );
			} catch ( SubstitutionException e ) {
				throw e;
			} catch ( Exception e ) {
				System.out.println( String.format( "validateStandardType Failed: type='%s', typeStr='%s', name='%s', value='%s'", type, typeStr, name, value ) );
				throw new SubstitutionException( e );
			}
		}

	}

	// FIXME: regex type
	private Object validateRegexType( SubType type, String typeStr, String name, Object value ) throws SubstitutionException {
		throw new SubstitutionException( new UnsupportedOperationException( String.format( "Type '%s' is not supported", typeStr ) ) );
	}

	private Object validateUnknownType( SubType type, String typeStr, String name, Object value ) throws SubstitutionException {
		return onValidateCustomType( typeStr, name, value );
	}



	public static class SlashPattern {
		String slashPattern;
		List<String> keys = new ArrayList<>();
		Pattern pattern;
	}

	private static Pattern patternTokenName = Pattern.compile( "\\{[^}${]*\\$([^}{]*)\\}" );

	// : /test/{Str$name}/and/{Str$attr}/search
	public static SlashPattern createSlashPattern( String slashPatternStr ) throws Exception {
		SlashPattern slashPattern = new SlashPattern();

		// the pattern string
		{
			slashPattern.slashPattern = slashPatternStr;
		}

		// the pattern
		{
			String regex = slashPatternStr;
			regex = regex.replaceAll( "\\{[^}{]*\\}", "([^/]+)" );
			slashPattern.pattern = Pattern.compile( regex );
		}

		// the keys
		{
			Matcher m = patternTokenName.matcher( slashPatternStr );
			while ( m.find() ) {
				slashPattern.keys.add( m.group( 1 ) );
			}
		}

		return slashPattern;
	}

	/**
	 * @return null on no match
	 */
	public static Map<String, String> fromSlashParams( SlashPattern pattern, String slashParams ) {
		Matcher m = pattern.pattern.matcher( slashParams );
		if ( m.matches() ) {
			m.reset();
			Map<String, String> out = new HashMap<>();
			while ( m.find() ) {
				for ( int i = 0; i < m.groupCount(); i++ ) {
					out.put( pattern.keys.get( i ), m.group( i + 1 ) );
				}
			}
			return out;
		}
		return null;
	}
}
