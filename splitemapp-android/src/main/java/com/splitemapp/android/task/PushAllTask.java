package com.splitemapp.android.task;

import com.splitemapp.android.dao.DatabaseHelper;

public abstract class PushAllTask {
	
	DatabaseHelper databaseHelper;
	
	public PushAllTask(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	/**
	 * Executes a required action on start.
	 */
	public void executeOnStart(){};
	
	/**
	 * Executes a required action on success. This code executes after the processResult method.
	 */
	public abstract void executeOnSuccess();

	/**
	 * Executes a required action on fail. This code executes after the processResult method.
	 */
	public abstract void executeOnFail();
	
	public void execute(){
		// Calling all push services
		final PushUserExpensesTask pushUserExpensesTask = new PushUserExpensesTask(databaseHelper){
			@Override
			public void executeOnSuccess() {
				PushAllTask.this.executeOnSuccess();
			}
			@Override
			public void executeOnFail() {
				PushAllTask.this.executeOnFail();
			}
		};
		
		final PushUserInvitesTask pushUserInvitesTask = new PushUserInvitesTask(databaseHelper){
			@Override
			public void executeOnSuccess() {
				pushUserExpensesTask.execute();
			}
			@Override
			public void executeOnFail() {
				PushAllTask.this.executeOnFail();
			}
		};
		
		final PushUserToProjectsTask pushUserToProjectsTask = new PushUserToProjectsTask(databaseHelper){
			@Override
			public void executeOnSuccess() {
				pushUserInvitesTask.execute();
			}
			@Override
			public void executeOnFail() {
				PushAllTask.this.executeOnFail();
			}
		};
		
		final PushProjectCoverImagesTask pushProjectCoverImagesTask = new PushProjectCoverImagesTask(databaseHelper){
			@Override
			public void executeOnSuccess() {
				pushUserToProjectsTask.execute();
			}
			@Override
			public void executeOnFail() {
				PushAllTask.this.executeOnFail();
			}
		};
		
		final PushProjectsTask pushProjectsTask = new PushProjectsTask(databaseHelper){
			@Override
			public void executeOnSuccess() {
				pushProjectCoverImagesTask.execute();
			}
			@Override
			public void executeOnFail() {
				PushAllTask.this.executeOnFail();
			}
		};
		
		final PushUserContactDatasTask pushUserContactDatasTask = new PushUserContactDatasTask(databaseHelper){
			@Override
			public void executeOnSuccess() {
				pushProjectsTask.execute();
			}
			@Override
			public void executeOnFail() {
				PushAllTask.this.executeOnFail();
			}
		};
		
		final PushUserAvatarsTask pushUserAvatarsTask = new PushUserAvatarsTask(databaseHelper){
			@Override
			public void executeOnSuccess() {
				pushUserContactDatasTask.execute();
			}
			@Override
			public void executeOnFail() {
				PushAllTask.this.executeOnFail();
			}
		};
		
		final PushUsersTask pushUsersTask = new PushUsersTask(databaseHelper){
			@Override
			public void executeOnStart() {
				PushAllTask.this.executeOnStart();
			}
			@Override
			public void executeOnFail() {
				PushAllTask.this.executeOnFail();
			}
			@Override
			public void executeOnSuccess() {
				pushUserAvatarsTask.execute();
			}
		};
		pushUsersTask.execute();
	}

}
