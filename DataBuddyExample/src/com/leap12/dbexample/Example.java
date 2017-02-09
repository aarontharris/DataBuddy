package com.leap12.dbexample;

import java.util.Collection;
import java.util.HashSet;

import com.leap12.common.Log;
import com.leap12.databuddy.DataBuddy;

public class Example {

	private static Collection<Class<? extends Dao>> daoClasses = new HashSet<>();

	private static void initializeDaos() throws Exception {
		daoClasses.add( AnimalDao.class );

		for ( Class<? extends Dao> daoClass : daoClasses ) {
			Log.d( "ensuring %s", daoClass.getSimpleName() );
			Dao dao = daoClass.newInstance();
			dao.ensureTables();
		}
	}

	public static void main( String args[] ) {
		try {
			Runtime.getRuntime().addShutdownHook( new Thread() {
				@Override
				public void run() {
					DataBuddy.get().shutdown();
				}
			} );

			initializeDaos();
			DataBuddy dataBuddy = DataBuddy.get();
			dataBuddy.startup( MySpecialDelegate.class );
		} catch ( Exception e ) {
			Log.e( e, "Trouble during startup" );
		}
	}
}
