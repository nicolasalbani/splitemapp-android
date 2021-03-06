package com.splitemapp.android.service.sync;

import android.content.Context;
import android.content.Intent;

import com.splitemapp.android.service.BaseTask;
import com.splitemapp.commons.constants.ServiceConstants;

public class StopRefreshAnimationTask extends BaseTask {

	public StopRefreshAnimationTask(Context context) {
		super(context);
	}

	@Override
	public void executeService(Intent intent) {
		broadcastMessage(STOP_ANIMATION, ServiceConstants.UI_MESSAGE);
	}

}
