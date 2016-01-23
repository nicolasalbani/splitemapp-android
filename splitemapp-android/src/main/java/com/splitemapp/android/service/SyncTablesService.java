package com.splitemapp.android.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.splitemapp.android.service.sync.PullProjectCoverImagesTask;
import com.splitemapp.android.service.sync.PullProjectsTask;
import com.splitemapp.android.service.sync.PullUserAvatarsTask;
import com.splitemapp.android.service.sync.PullUserContactDatasTask;
import com.splitemapp.android.service.sync.PullUserExpensesTask;
import com.splitemapp.android.service.sync.PullUserInvitesTask;
import com.splitemapp.android.service.sync.PullUserToProjectsTask;
import com.splitemapp.android.service.sync.PullUsersTask;
import com.splitemapp.android.service.sync.PushProjectCoverImagesTask;
import com.splitemapp.android.service.sync.PushProjectsTask;
import com.splitemapp.android.service.sync.PushUserAvatarsTask;
import com.splitemapp.android.service.sync.PushUserContactDatasTask;
import com.splitemapp.android.service.sync.PushUserExpensesTask;
import com.splitemapp.android.service.sync.PushUserInvitesTask;
import com.splitemapp.android.service.sync.PushUserSessionsTask;
import com.splitemapp.android.service.sync.PushUserToProjectsTask;
import com.splitemapp.android.service.sync.PushUsersTask;
import com.splitemapp.android.service.sync.StartRefreshAnimationTask;
import com.splitemapp.android.service.sync.StopRefreshAnimationTask;
import com.splitemapp.android.service.sync.SynchronizeContactsTask;

public class SyncTablesService extends IntentService {

	private static final String TAG = SyncTablesService.class.getSimpleName();
	private boolean isConnectedToServer;

	public SyncTablesService() {
		super(TAG);
		isConnectedToServer = true;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		BaseTask task = null;

		// Obtaining the name of the task to be executed
		String taskName = intent.getExtras().getString(BaseTask.TASK_NAME);

		// Checking for start and stop animation tasks
		if(taskName.equals(StartRefreshAnimationTask.class.getSimpleName())){
			task = new StartRefreshAnimationTask(this);
		} else if(taskName.equals(StopRefreshAnimationTask.class.getSimpleName())){
			task = new StopRefreshAnimationTask(this);
		} else {
			if(isConnectedToServer){
				if(taskName.equals(SynchronizeContactsTask.class.getSimpleName())){
					task = new SynchronizeContactsTask(this);
				} else if(taskName.equals(PullProjectCoverImagesTask.class.getSimpleName())){
					task = new PullProjectCoverImagesTask(this);
				} else if(taskName.equals(PullProjectsTask.class.getSimpleName())){
					task = new PullProjectsTask(this);
				} else if(taskName.equals(PullUserAvatarsTask.class.getSimpleName())){
					task = new PullUserAvatarsTask(this);
				} else if(taskName.equals(PullUserContactDatasTask.class.getSimpleName())){
					task = new PullUserContactDatasTask(this);
				} else if(taskName.equals(PullUserExpensesTask.class.getSimpleName())){
					task = new PullUserExpensesTask(this);
				} else if(taskName.equals(PullUserInvitesTask.class.getSimpleName())){
					task = new PullUserInvitesTask(this);
				} else if(taskName.equals(PullUsersTask.class.getSimpleName())){
					task = new PullUsersTask(this);
				} else if(taskName.equals(PullUserToProjectsTask.class.getSimpleName())){
					task = new PullUserToProjectsTask(this);
				} else if(taskName.equals(PushProjectCoverImagesTask.class.getSimpleName())){
					task = new PushProjectCoverImagesTask(this);
				} else if(taskName.equals(PushProjectsTask.class.getSimpleName())){
					task = new PushProjectsTask(this);
				} else if(taskName.equals(PushUserAvatarsTask.class.getSimpleName())){
					task = new PushUserAvatarsTask(this);
				} else if(taskName.equals(PushUserContactDatasTask.class.getSimpleName())){
					task = new PushUserContactDatasTask(this);
				} else if(taskName.equals(PushUserExpensesTask.class.getSimpleName())){
					task = new PushUserExpensesTask(this);
				} else if(taskName.equals(PushUserInvitesTask.class.getSimpleName())){
					task = new PushUserInvitesTask(this);
				} else if(taskName.equals(PushUserSessionsTask.class.getSimpleName())){
					task = new PushUserSessionsTask(this);
				} else if(taskName.equals(PushUsersTask.class.getSimpleName())){
					task = new PushUsersTask(this);
				} else if(taskName.equals(PushUserToProjectsTask.class.getSimpleName())){
					task = new PushUserToProjectsTask(this);
				}
			} else {
				Log.e(TAG, "Network error detected: NOT running " +taskName);
			}
		}

		// Executing the task if it was set
		try {
			if(task != null){
				task.executeService(intent);
			}
		} catch (Exception e) {
			Log.e(TAG, "Network error detected");
			task.broadcastMessage(BaseTask.NETWORK_ERROR);
			isConnectedToServer = false;
		}
	}

}
