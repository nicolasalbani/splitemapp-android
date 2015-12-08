package com.splitemapp.android.task;

import android.os.AsyncTask;

import com.splitemapp.android.dao.DatabaseHelper;

public abstract class BaseAsyncTask <F, E, R> extends AsyncTask<Void, Void, R>{
	
	public DatabaseHelper databaseHelper;

	public BaseAsyncTask(DatabaseHelper databaseHelper) {
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
}
