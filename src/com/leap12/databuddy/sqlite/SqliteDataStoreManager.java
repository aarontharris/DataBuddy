package com.leap12.databuddy.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.leap12.common.Log;
import com.leap12.databuddy.data.DataStore;
import com.leap12.databuddy.data.DataStoreManager;
import com.leap12.databuddy.data.ResultSetJSonAdapter;

public class SqliteDataStoreManager implements DataStoreManager {

	private static void test() {
		testConnect();
		testCreateTable();
		testInsert();
		testSelect();
		testUpdate();
		testDelete();
	}

	private static void testConnect() {
		Connection c = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully");
	}

	private static void testCreateTable() {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			String sql = "CREATE TABLE COMPANY " +
					"(ID INT PRIMARY KEY     NOT NULL," +
					" NAME           TEXT    NOT NULL, " +
					" AGE            INT     NOT NULL, " +
					" ADDRESS        CHAR(50), " +
					" SALARY         REAL)";
			stmt.executeUpdate(sql);
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Table created successfully");
	}

	private static void testInsert() {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			String sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " +
					"VALUES (1, 'Paul', 32, 'California', 20000.00 );";
			stmt.executeUpdate(sql);

			sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " +
					"VALUES (2, 'Allen', 25, 'Texas', 15000.00 );";
			stmt.executeUpdate(sql);

			sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " +
					"VALUES (3, 'Teddy', 23, 'Norway', 20000.00 );";
			stmt.executeUpdate(sql);

			sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " +
					"VALUES (4, 'Mark', 25, 'Rich-Mond ', 65000.00 );";
			stmt.executeUpdate(sql);

			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Records created successfully");
	}

	private static void testSelect() {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM COMPANY;");
			while (rs.next()) {
				int id = rs.getInt("id");
				String name = rs.getString("name");
				int age = rs.getInt("age");
				String address = rs.getString("address");
				float salary = rs.getFloat("salary");
				System.out.println("ID = " + id);
				System.out.println("NAME = " + name);
				System.out.println("AGE = " + age);
				System.out.println("ADDRESS = " + address);
				System.out.println("SALARY = " + salary);
				System.out.println();
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Operation done successfully");
	}

	private static void testUpdate() {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			String sql = "UPDATE COMPANY set SALARY = 25000.00 where ID=1;";
			stmt.executeUpdate(sql);
			c.commit();

			ResultSet rs = stmt.executeQuery("SELECT * FROM COMPANY;");
			while (rs.next()) {
				int id = rs.getInt("id");
				String name = rs.getString("name");
				int age = rs.getInt("age");
				String address = rs.getString("address");
				float salary = rs.getFloat("salary");
				System.out.println("ID = " + id);
				System.out.println("NAME = " + name);
				System.out.println("AGE = " + age);
				System.out.println("ADDRESS = " + address);
				System.out.println("SALARY = " + salary);
				System.out.println();
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Operation done successfully");
	}

	private static void testDelete() {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			stmt = c.createStatement();
			String sql = "DELETE from COMPANY where ID=2;";
			stmt.executeUpdate(sql);
			c.commit();

			ResultSet rs = stmt.executeQuery("SELECT * FROM COMPANY;");
			while (rs.next()) {
				int id = rs.getInt("id");
				String name = rs.getString("name");
				int age = rs.getInt("age");
				String address = rs.getString("address");
				float salary = rs.getFloat("salary");
				System.out.println("ID = " + id);
				System.out.println("NAME = " + name);
				System.out.println("AGE = " + age);
				System.out.println("ADDRESS = " + address);
				System.out.println("SALARY = " + salary);
				System.out.println();
			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Operation done successfully");
	}

	private static SqliteDataStoreManager self = new SqliteDataStoreManager();

	public static SqliteDataStoreManager getInstance() {
		return self;
	}

	private final Gson gson;

	private SqliteDataStoreManager() {
		gson = new GsonBuilder().create();
	}

	@Override
	public void startup() throws Exception {
	}

	@Override
	public void shutdown() throws Exception {
	}

	@Override
	public DataStore attainDataStore() {
		SqliteDataStore store = new SqliteDataStore();
		store.openConnection("dbBuddy.db");
		return store;
	}

	@Override
	public void releaseDataStore(DataStore store) {
		if (store instanceof SqliteDataStore) {
			((SqliteDataStore) store).closeConnection();
		}
	}

	public Gson getGson() {
		return gson;
	}


	private static final String toQuerySchema(String table) {
		String format = ""
				+ "CREATE TABLE %s "
				+ "("
				+ "  idkey          TEXT PRIMARY KEY NOT NULL, "
				+ "  valtype        INT NOT NULL, "
				+ "  textval        TEXT, "
				+ "  intval         INT, "
				+ "  floatval       REAL "
				+ ")";
		return String.format(format, table);
	}

	private static final String toQueryInsert(String table, String idKey, Type type, String strVal, int intVal, float floatVal) {
		String format = "INSERT OR REPLACE INTO %s "
				+ "(idkey,valtype,textval,intval,floatval) VALUES "
				+ "('%s', %s, '%s', %s, %s );";
		return String.format(format, table, idKey, type.getTypeId(), strVal, intVal, floatVal);
	}

	private static final String toQuerySelect(String table, String key) {
		String format = "SELECT * FROM %s WHERE idkey='%s'";
		return String.format(format, table, key);
	}

	public enum Type {
		// Careful this order can never change without a data migration
		// as the ids will be stored in the database and may may the wrong value if changed.
		BooleanValue(1, "intval", Boolean.class),
		IntegerValue(2, "intval", Integer.class),
		FloatValue(3, "floatval", Float.class),
		StringValue(4, "textval", String.class),
		JsonValue(5, "textval", JsonObject.class);

		private static final Type[] idMap = new Type[] {
				// ZERO is invalid
				BooleanValue, // 1
				IntegerValue, // 2
				FloatValue, //   3
				StringValue, //  4
				JsonValue, //    5
		};

		private int typeId;
		private String fieldName;
		private Class<?> type;

		Type(int typeId, String fieldName, Class<?> type) {
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

		public static Type fromTypeId(int id) {
			return idMap[id - 1]; // we do the -1 so we can do 1-5 instead of 0-4 because we dont want to use zeros in the database.
		}
	}

	private static class Row {
		String key;
		int type;
		String textVal;
		int intVal;
		float floatVal;
	}

	/** Thread safe */
	public static class SqliteDataStore implements DataStore {
		private Connection connection;
		private final Set<String> knownTables;

		public SqliteDataStore() {
			knownTables = new HashSet<>();
		}

		private void openConnection(String dbFile) {
			try {
				Class.forName("org.sqlite.JDBC");
				connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
				connection.setAutoCommit(true); // we're only doing simple queries.
			} catch (Exception e) {
				Log.e("Unable to open sqlite connection File: " + dbFile);
				throw new IllegalStateException(e);
			}
		}

		private void closeConnection() {
			try {
				if (connection != null) {
					// connection.rollback();
					connection.close();
					connection = null;
				}
			} catch (Exception e) {
				Log.e(e);
			}
		}

		private JSONObject selectOne(String query) throws Exception {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			JSONObject json = ResultSetJSonAdapter.toJson(rs);
			rs.close();
			stmt.close();
			return json;
		}

		private JSONArray selectMany(String query) throws Exception {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			JSONArray json = ResultSetJSonAdapter.toJsonArray(rs);
			rs.close();
			stmt.close();
			return json;
		}

		private Row selectOne(String table, String key, Type type) throws Exception {
			String query = SqliteDataStoreManager.toQuerySelect(table, key);
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			Row row = null;
			if (rs.next()) {
				row = new Row();
				row.type = rs.getInt("valtype");
				if (type.getTypeId() == row.type) {
					switch (type) {
					case StringValue:
						row.textVal = rs.getString(type.getFieldName());
						break;
					default:
						throw new UnsupportedOperationException(type + " not yet supported");
					}
				} else {
					if (row.type == 0) {
						throw new IllegalStateException("Type mismatch, requested " + type + " but found no type");
					} else {
						throw new IllegalStateException("Type mismatch, requested " + type + " but found " + Type.fromTypeId(row.type));
					}
				}
			}
			rs.close();
			stmt.close();
			return row;
		}

		// just synchronize the whole method, it should be very fast except the one time and everyone needs to wait for that anyway
		private synchronized void ensureTable(String table) throws Exception {
			if (!knownTables.contains(table)) {
				Statement stmt = connection.createStatement();
				String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + table + "'";
				ResultSet rs = stmt.executeQuery(sql);
				if (rs.next() && table.equals(rs.getString("name"))) {
					rs.close();
					stmt.close();
					knownTables.add(table);
					return;
				}

				stmt.executeUpdate(SqliteDataStoreManager.toQuerySchema(table));
				stmt.close();
				//				connection.commit();
				knownTables.add(table);
			}
		}


		private void update(String query) throws Exception {
			Statement stmt = connection.createStatement();
			stmt.executeUpdate(query);
			stmt.close();
			// connection.commit();
		}

		private void insertOrReplace(String table, String key, String value) throws Exception {
			ensureTable(table);
			String query = SqliteDataStoreManager.toQueryInsert(table, key, Type.StringValue, value, 0, 0f);
			update(query);
		}

		private String toTableName(String topic, String subtopic) {
			return topic + "_" + subtopic;
		}

		@Override
		public void saveString(String topic, String subtopic, String key, String value) throws Exception {
			String table = toTableName(topic, subtopic);
			insertOrReplace(table, key, value);
		}

		@Override
		public String loadString(String topic, String subtopic, String key) throws Exception {
			String table = toTableName(topic, subtopic);
			Row row = selectOne(table, key, Type.StringValue);
			if (row != null) {
				return row.textVal;
			}
			return null;
		}
	}
}
