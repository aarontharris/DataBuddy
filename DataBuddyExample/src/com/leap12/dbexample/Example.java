package com.leap12.dbexample;

import com.leap12.common.Log;
import com.leap12.databuddy.DataBuddy;

public class Example {
	public static void main(String args[]) {
		try {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					DataBuddy.get().shutdown();
				}
			});

			DataBuddy dataBuddy = DataBuddy.get();
			dataBuddy.startup(MySpecialDelegate.class);
		} catch (Exception e) {
			Log.e(e, "Trouble during startup");
		}
	}
}
