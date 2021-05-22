package com.leap12.databuddy;

import com.leap12.common.Log;

/*

localhost:25564/echo?query=hi
localhost:25564/write?topic=test&subtopic=test&key=hello&value=world
localhost:25564/read?topic=test&subtopic=test&key=hello

http://data.leap12.com:25566/echo?query=hi
http://data.leap12.com:25566/write?topic=test&subtopic=test&key=hello&value=world
http://data.leap12.com:25566/read?topic=test&subtopic=test&key=hello

 */
public class DefaultLauncher {
	public static void main( String args[] ) {
		try {
			Runtime.getRuntime().addShutdownHook( new Thread() {
				@Override
				public void run() {
					DataBuddy.get().shutdown();
				}
			} );

			DataBuddy dataBuddy = DataBuddy.get();
			dataBuddy.startup();
		} catch ( Exception e ) {
			Log.e( e, "Trouble during startup" );
		}
	}
}
