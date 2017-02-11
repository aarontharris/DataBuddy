package com.leap12.dbexample;

import org.json.JSONObject;

import com.leap12.common.Log;
import com.leap12.databuddy.data.DataStore;
import com.leap12.databuddy.sqlite.SqliteDataStoreManager;
import com.leap12.databuddy.sqlite.SqliteShardKey;

public class AnimalDao implements Dao {

	private final SqliteShardKey animalKey = new TopicShardKey( "animals" );
	private DataStore dataStore;

	public AnimalDao() {
		try {
			dataStore = SqliteDataStoreManager.get().attainDataStore( animalKey, 1000 );
		} catch ( Exception e ) {
			throw new IllegalStateException( e );
		}
	}

	@Override
	public void ensureTables() throws Exception {
		dataStore.readWrite( ( db ) -> {
			String table = null;
			if ( ( null != ( table = "animal" ) ) && db.ensureTable( table, toAnimalTable( table ) ) ) {
				Log.d( "Created %s", table );
			}
			if ( ( null != ( table = "animal_facts" ) ) && db.ensureTable( table, toAnimalFactsTable( table ) ) ) {
				Log.d( "Created %s", table );
			}
			return null;
		} );

		test1();
		// for ( int i = 0; i < 10; i++ ) { test(); }
	}

	private static final String toAnimalTable( String table ) {
		String sql = ""
		        + "CREATE TABLE %s "
		        + "("
		        + "  id             INTEGER PRIMARY KEY NOT NULL, "
		        + "  name	        TEXT UNIQUE NOT NULL"
		        + ")";
		return String.format( sql, table );
	}

	private static final String toAnimalInsert( Animal animal ) {
		String sql = ""
		        + "INSERT INTO animal"
		        + "( name )"
		        + " VALUES "
		        + "( '%s' )";
		return String.format( sql, animal.name );
	}

	private static final String toAnimalFactsTable( String table ) {
		String sql = ""
		        + "CREATE TABLE %s "
		        + "("
		        + "  id             INTEGER PRIMARY KEY NOT NULL, "
		        + "  fact           TEXT UNIQUE NOT NULL, "
		        + "  animal_id      INTEGER, "
		        + "  FOREIGN KEY(animal_id) REFERENCES animal(id)"
		        + ")";
		return String.format( sql, table );
	}



	public class Animal {
		private int id;
		private String name;

		private Animal() {
		}

		public Animal( String name ) {
			this.name = name;
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public void setName( String name ) {
			this.name = name;
		}
	}

	public void test1() throws Exception {
		Animal animal = new Animal( "dog" );
		writeAnimal( animal );
		Log.d( "Animal: %s %s", animal.id, animal.name );
	}

	public void test2() throws Exception {
		new Thread( ( ) -> {
			try {
				for ( int i = 0; i < 1000; i++ ) {
					final int idx = i;
					String name = String.format( "%s.%s.%s", Thread.currentThread().getId(), System.currentTimeMillis(), idx );
					dataStore.readWrite( ( db ) -> {
						db.update( String.format( "insert into animal ( name ) values ( '%s' )", name ), null );
						JSONObject object = db.selectOne( "select * from animal where id in ( select max(id) from animal )" );
						if ( !name.equals( object.getString( "name" ) ) ) {
							Log.e( "MISMATCH %s - expected %s got %s", idx, name, object.getString( "name" ) );
						}
						return null;
					} );
				}
				Log.d( "Finish" );
			} catch ( Exception e ) {
				Log.e( e );
			}
		} ).start();
	}

	public void writeAnimal( Animal animal ) throws Exception {
		JSONObject json = dataStore.read( db -> db.selectOne( String.format( "select * from animal where name='%s'", animal.name ) ) );
		if ( json == null ) {
			json = dataStore.readWrite( db -> db.insertAndSelect( "animal", "id", toAnimalInsert( animal ) ) );
		} else {
			final int id = json.getInt( "id" );
			dataStore.readWrite( db -> {
				db.update( "update animal set name='%s' where id=%s", animal.name, id );
				return null;
			} );
		}
		animal.id = json.getInt( "id" );
	}



	public class AnimalFact {
		private int id;
		private String fact;
		private int animalId;
		private transient Animal animal;

		private AnimalFact() {
		}

		public AnimalFact( Animal animal, String fact ) {
			this.animal = animal;
			this.animalId = animal.getId();
			this.fact = fact;
		}

		public int getId() {
			return id;
		}

		public String getFact() {
			return this.fact;
		}

		public void setFact( String fact ) {
			this.fact = fact;
		}
	}
}
