package com.splitemapp.android.task;

import com.splitemapp.android.dao.DatabaseHelper;

public abstract class PullAllTask {
	
	DatabaseHelper databaseHelper;
	
	public PullAllTask(DatabaseHelper databaseHelper) {
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
		// Calling all pull services
		final PullUserExpensesTask pullUserExpensesTask = new PullUserExpensesTask(databaseHelper){
			@Override
			public void executeOnSuccess() {
				PullAllTask.this.executeOnSuccess();
			}
			@Override
			public void executeOnFail() {
				PullAllTask.this.executeOnFail();
			}
		};
		
		final PullUserInvitesTask pullUserInvitesTask = new PullUserInvitesTask(databaseHelper){
			@Override
			public void executeOnSuccess() {
				pullUserExpensesTask.execute();
			}
			@Override
			public void executeOnFail() {
				PullAllTask.this.executeOnFail();
			}
		};
		
		final PullUserToProjectsTask pullUserToProjectsTask = new PullUserToProjectsTask(databaseHelper){
			@Override
			public void executeOnSuccess() {
				pullUserInvitesTask.execute();
			}
			@Override
			public void executeOnFail() {
				PullAllTask.this.executeOnFail();
			}
		};
		
		final PullProjectCoverImagesTask pullProjectCoverImagesTask = new PullProjectCoverImagesTask(databaseHelper){
			@Override
			public void executeOnSuccess() {
				pullUserToProjectsTask.execute();
			}
			@Override
			public void executeOnFail() {
				PullAllTask.this.executeOnFail();
			}
		};
		
		final PullProjectsTask pullProjectsTask = new PullProjectsTask(databaseHelper){
			@Override
			public void executeOnSuccess() {
				pullProjectCoverImagesTask.execute();
			}
			@Override
			public void executeOnFail() {
				PullAllTask.this.executeOnFail();
			}
		};
		
		final PullUserContactDatasTask pullUserContactDatasTask = new PullUserContactDatasTask(databaseHelper){
			@Override
			public void executeOnSuccess() {
				pullProjectsTask.execute();
			}
			@Override
			public void executeOnFail() {
				PullAllTask.this.executeOnFail();
			}
		};
		
		final PullUserAvatarsTask pullUserAvatarsTask = new PullUserAvatarsTask(databaseHelper){
			@Override
			public void executeOnSuccess() {
				pullUserContactDatasTask.execute();
			}
			@Override
			public void executeOnFail() {
				PullAllTask.this.executeOnFail();
			}
		};
		
		PullUsersTask pullUsersTask = new PullUsersTask(databaseHelper){
			@Override
			public void executeOnStart() {
				PullAllTask.this.executeOnStart();
			}
			@Override
			public void executeOnFail() {
				PullAllTask.this.executeOnFail();
			}
			@Override
			public void executeOnSuccess() {
				pullUserAvatarsTask.execute();
			}
		};
		pullUsersTask.execute();
	}

}
