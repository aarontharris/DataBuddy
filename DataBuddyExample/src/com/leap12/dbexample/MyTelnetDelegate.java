package com.leap12.dbexample;

import com.leap12.common.ClientConnection;
import com.leap12.databuddy.aspects.UserDelegate;

public class MyTelnetDelegate extends UserDelegate {

	@Override
	protected void onAttached(ClientConnection connection) throws Exception {
		super.onAttached(connection);
		writeMsg("type 'help' or 'quit'");
	}

}
