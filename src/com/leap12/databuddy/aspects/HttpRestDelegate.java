package com.leap12.databuddy.aspects;

import com.leap12.common.ClientConnection;
import com.leap12.databuddy.BaseConnectionDelegate;

// TODO: HttpRestDelegate
public class HttpRestDelegate extends BaseConnectionDelegate {

	@Override
	protected void onAttached( ClientConnection connection ) throws Exception {
		super.onAttached( connection );
	}

	@Override
	protected void onReceivedMsg( String msg ) throws Exception {
		super.onReceivedMsg( msg );
	}

	@Override
	protected void onDetatched() throws Exception {
		super.onDetatched();
	}

}
