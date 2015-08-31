package com.leap12.databuddy;

import com.leap12.common.Log;

public class Main {
	public static void main(String args[]) {
		try {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					DataBuddy.get().shutdown();
				}
			});
			DataBuddy dataBuddy = DataBuddy.get();
			dataBuddy.startup();
		} catch (Exception e) {
			Log.e(e, "Trouble during startup");
		}
	}
}
