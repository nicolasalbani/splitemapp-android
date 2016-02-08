package com.splitemapp.android.service.sync;

import com.splitemapp.android.service.BaseTask;
import com.splitemapp.commons.constants.ServiceConstants;

import android.content.Context;
import android.content.Intent;

public class StopRefreshAnimationTask extends BaseTask {

	public StopRefreshAnimationTask(Context context) {
		super(context);
	}

	@Override
	public void executeService(Intent intent) {
		broadcastMessage(STOP_ANIMATION, ServiceConstants.UI_MESSAGE);
	}

}
