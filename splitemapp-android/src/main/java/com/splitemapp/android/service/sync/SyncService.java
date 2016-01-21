package com.splitemapp.android.service.sync;

import android.app.IntentService;
import android.content.Intent;

import com.splitemapp.android.service.BaseTask;

public class SyncService extends IntentService {

	private static final String TAG = SyncService.class.getSimpleName();
	
	public SyncService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		BaseTask task = null;

		// Obtaining the name of the task to be executed
		String taskName = intent.getExtras().getString(BaseTask.TASK_NAME);

		if(taskName.equals(StartRefreshAnimationTask.class.getSimpleName())){
			task = new StartRefreshAnimationTask(this);
		} else if(taskName.equals(StopRefreshAnimationTask.class.getSimpleName())){
			task = new StopRefreshAnimationTask(this);
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

		// Executing the task
		task.executeService(intent);
	}

}
