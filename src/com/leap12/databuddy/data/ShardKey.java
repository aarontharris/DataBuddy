package com.leap12.databuddy.data;


public class ShardKey {
	private final Class<? extends DataStore> dataStoreClass;
	private final String shardString;

	protected ShardKey( String shard, Class<? extends DataStore> dataStoreClass ) {
		this.shardString = shard;
		this.dataStoreClass = dataStoreClass;
	}

	@Override
	public String toString() {
		return shardString;
	}

	public Class<? extends DataStore> getDataStoreClass() {
		return dataStoreClass;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( shardString == null ) ? 0 : shardString.hashCode() );
		return result;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		ShardKey other = (ShardKey) obj;
		if ( shardString == null ) {
			if ( other.shardString != null )
				return false;
		} else if ( !shardString.equals( other.shardString ) )
			return false;
		return true;
	}
}
