package com.splitemapp.android.service.sync;

import com.splitemapp.android.service.BaseTask;

import android.content.Context;
import android.content.Intent;

public class StartRefreshAnimationTask extends BaseTask {

	public StartRefreshAnimationTask(Context context) {
		super(context);
	}

	@Override
	public void executeService(Intent intent) {
		broadcastMessage(START_ANIMATION);
	}

}
