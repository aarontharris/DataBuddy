package com.leap12.databuddy.data.var;

import com.google.gson.JsonObject;

public enum Type {
	// Careful this order can never change without a data migration
	// as the ids will be stored in the database and may may the wrong value if changed.
	BooleanValue( 1, "intval", Boolean.class ),
	IntegerValue( 2, "intval", Integer.class ),
	FloatValue( 3, "floatval", Float.class ),
	StringValue( 4, "textval", String.class ),
	BlobValue( 5, "blobval", String.class ),
	JsonValue( 6, "textval", JsonObject.class );

	private static final Type[] idMap = new Type[] {
	        // ZERO is invalid
	        BooleanValue, // 1
	        IntegerValue, // 2
	        FloatValue, // 3
	        StringValue, // 4
	        BlobValue, // 5
	        JsonValue, // 6
	};

	private int typeId;
	private String fieldName;
	private Class<?> type;

	Type( int typeId, String fieldName, Class<?> type ) {
		this.typeId = typeId;
		this.fieldName = fieldName;
		this.type = type;
	}

	public int getTypeId() {
		return typeId;
	}

	public String getFieldName() {
		return fieldName;
	}

	public Class<?> getType() {
		return type;
	}

	public static Type fromTypeId( int id ) {
		return idMap[id - 1]; // we do the -1 so we can do 1-5 instead of 0-4 because we dont want to use zeros in the database.
	}
}
