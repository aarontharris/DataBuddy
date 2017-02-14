package com.leap12.databuddy.data.var;

import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

import com.leap12.common.Nullable;
import com.leap12.common.StrUtl;

public class VarType {
	public String key;
	public int type;
	public String textVal;
	public byte[] blobVal;
	public int intVal;
	public float floatVal;

	public VarType( String key ) {
		if ( key == null ) {
			throw new IllegalStateException( "Key cannot be null" );
		}
		this.key = key;
	}

	// FIXME factor out to some place more common
	public static VarType fromResultSet( ResultSet rs, @Nullable Type type ) throws Exception {
		VarType var = new VarType( rs.getString( "idkey" ) );
		var.type = rs.getInt( "valtype" );

		// validate type match if requested
		if ( type != null ) {
			if ( type.getTypeId() == var.type ) {
				// all good
			} else {
				if ( var.type == 0 ) {
					throw new IllegalStateException( "Type mismatch, requested " + type + " but found no type" );
				} else {
					throw new IllegalStateException( "Type mismatch, requested " + type + " but found " + Type.fromTypeId( var.type ) );
				}
			}
		} else {
			type = Type.fromTypeId( var.type );
		}

		switch ( type ) {
		case BlobValue:
			var.blobVal = rs.getBytes( type.getFieldName() );
			break;
		case StringValue:
		case JsonValue:
			var.textVal = rs.getString( type.getFieldName() );
			break;
		case IntegerValue:
		case BooleanValue:
			var.intVal = rs.getInt( type.getFieldName() );
			break;
		case FloatValue:
			var.floatVal = rs.getFloat( type.getFieldName() );
			break;
		default:
			throw new UnsupportedOperationException( type + " not yet supported" );
		}

		return var;
	}

	public static boolean isValid( VarType var ) {
		return ( var != null && var.type >= 0 );
	}

	@Override
	public String toString() {
		try {
			return toJsonObject().toString();
		} catch ( Exception e ) {
			throw new IllegalStateException( e );
		}
	}

	public JSONObject toJsonObject() throws Exception {
		JSONObject out = new JSONObject();
		toJsonObject( out );
		return out;
	}

	/**
	 * Add the key -> value to an existing JSONObject<br>
	 * duplicates will clobber.<br>
	 * 
	 * @param out
	 * @throws Exception
	 */
	public void toJsonObject( JSONObject out ) throws Exception {
		if ( isValid( this ) ) {
			switch ( Type.fromTypeId( type ) ) {
			case BlobValue:
				out.put( this.key, new String( this.blobVal, StrUtl.CHARSET_UTF8 ) );
				break;
			case StringValue:
			case JsonValue:
				out.put( this.key, this.textVal );
				break;
			case IntegerValue:
				out.put( this.key, this.intVal );
				break;
			case BooleanValue:
				out.put( this.key, this.intVal == 1 );
				break;
			case FloatValue:
				out.put( this.key, this.floatVal );
				break;
			default:
				throw new UnsupportedOperationException( "Type: " + type + " not yet supported" );
			}
		}
	}

	/**
	 * Add just the value to a JSONArray
	 */
	public void toJsonArray( JSONArray out ) throws Exception {
		if ( isValid( this ) ) {
			switch ( Type.fromTypeId( type ) ) {
			case BlobValue:
				out.put( new String( this.blobVal, StrUtl.CHARSET_UTF8 ) );
				break;
			case StringValue:
			case JsonValue:
				out.put( this.textVal );
				break;
			case IntegerValue:
				out.put( this.intVal );
				break;
			case BooleanValue:
				out.put( this.intVal == 1 );
				break;
			case FloatValue:
				out.put( this.floatVal );
				break;
			default:
				throw new UnsupportedOperationException( "Type: " + type + " not yet supported" );
			}
		}
	}
}
