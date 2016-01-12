package com.splitemapp.android.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class BaseResultReceiver extends ResultReceiver {
	private Receiver mReceiver;

	public void setReceiver(Receiver receiver) {
		mReceiver = receiver;
	}

	public interface Receiver {
		public void onReceiveResult(int resultCode, Bundle resultData);
	}

	public BaseResultReceiver(Handler handler) {
		super(handler);
	}

	@Override
	protected void onReceiveResult(int resultCode, Bundle resultData) {
		if (mReceiver != null) {
			mReceiver.onReceiveResult(resultCode, resultData);
		}
	}
}
