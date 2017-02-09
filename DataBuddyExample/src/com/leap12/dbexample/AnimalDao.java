package com.leap12.dbexample;

import org.json.JSONObject;

import com.leap12.common.Log;
import com.leap12.databuddy.data.BaseDao;
import com.leap12.databuddy.data.DataStore;
import com.leap12.databuddy.data.ShardKey;
import com.leap12.databuddy.data.TopicShardKey;

public class AnimalDao implements Dao {

	private final ShardKey animalKey = new TopicShardKey( "animals" );
	private DataStore db;

	public AnimalDao() {
		try {
			db = BaseDao.getInstance( this, animalKey );
		} catch ( Exception e ) {
			throw new IllegalStateException( e );
		}
	}

	@Override
	public void ensureTables() throws Exception {
		db.begin();
		try {
			String table = null;
			if ( ( null != ( table = "animal" ) ) && db.ensureTable( table, toAnimalTable( table ) ) ) {
				Log.d( "Created %s", table );
			}
			if ( ( null != ( table = "animal_facts" ) ) && db.ensureTable( table, toAnimalFactsTable( table ) ) ) {
				Log.d( "Created %s", table );
			}

			// for ( int i = 0; i < 10; i++ ) {
			// test();
			// }
		} finally {
			db.end();
		}
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
		        + "( %s, '%s' )";
		return String.format( sql, animal );
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

	public void test() throws Exception {
		new Thread( ( ) -> {
			try {
				for ( int i = 0; i < 1000; i++ ) {
					final int idx = i;
					String name = String.format( "%s.%s.%s", Thread.currentThread().getId(), System.currentTimeMillis(), idx );
					db.doInLock( ( ) -> {
						db.update( String.format( "insert into animal ( name ) values ( '%s' )", name ) );
						JSONObject object = db.selectOne( "select * from animal where id in ( select max(id) from animal )" );
						if ( !name.equals( object.getString( "name" ) ) ) {
							Log.e( "MISMATCH %s - expected %s got %s", idx, name, object.getString( "name" ) );
						}
					} );
				}
				Log.d( "Finish" );
			} catch ( Exception e ) {
				Log.e( e );
			}
		} ).start();
	}

	public void writeAnimal( Animal animal ) throws Exception {
		db.begin();
		try {
			// db.update(
		} finally {
			db.end();
		}
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
