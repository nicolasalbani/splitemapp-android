package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.utils.NetworkUtils;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.domain.dto.request.PushRequest;
import com.splitemapp.commons.domain.dto.response.PushResponse;

/**
 * Base Push task
 * @author nicolas
 *
 * @param <E>
 */
public abstract class PushTask <F, E extends Number, R extends PushResponse<E>> extends AsyncTask<Void, Void, R> {

	protected DatabaseHelper databaseHelper;
	protected BaseFragment baseFragment;
	
	public PushTask(DatabaseHelper databaseHelper, BaseFragment baseFragment) {
		this.databaseHelper = databaseHelper;
		this.baseFragment = baseFragment;
	}

	/**
	 * Gets the name of the table to pull the data for
	 * @return
	 */
	protected abstract String getTableName();

	/**
	 * Gets the path for the service to call
	 * @return
	 */
	protected abstract String getServicePath();

	/**
	 * Gets the response type
	 * @return
	 */
	protected abstract Class<R> getResponseType();

	/**
	 * Returns the request item list to be sent to the push service
	 * @param lastPushSuccessAt Date containing the last push success date
	 * @return List<F> containing the list of F objects to be sent to the push service
	 * @throws SQLException
	 */
	protected abstract List<F> getRequestItemList(Date lastPushSuccessAt) throws SQLException;

	/**
	 * Processes the results coming from the service. This will typically contain DB inserts or updates
	 * @param response ServiceResponse that contains the list returned by the server
	 * @throws SQLException
	 */
	protected abstract void processResult(R response) throws SQLException;
	
	/**
	 * Executes a required action on success. This code executes after the processResult method.
	 */
	protected void executeOnSuccess(){};
	
	/**
	 * Returns the tag to be used for logging events 
	 * @return
	 */
	protected abstract String getLoggingTag();

	@Override
	protected R doInBackground(Void... params) {
		try {
			// We get the date in which this table was last successfully pulled
			Date lastPushSuccessAt = databaseHelper.getSyncStatusDao().queryForEq(TableField.SYNC_STATUS_TABLE_NAME, getTableName()).get(0).getLastPushSuccessAt();

			// We get the session token
			String sessionToken = databaseHelper.getSessionToken();

			// We create the push request
			PushRequest<F> pushRequest = new PushRequest<F>();
			pushRequest.setToken(sessionToken);
			pushRequest.setLastPushSuccessAt(lastPushSuccessAt);
			pushRequest.setItemList(getRequestItemList(lastPushSuccessAt));

			// We call the rest service and send back the login response
			return NetworkUtils.callRestService(getServicePath(), pushRequest, getResponseType());
		} catch (Exception e) {
			Log.e(getLoggingTag(), e.getMessage(), e);
		}

		return null;
	}

	@Override
	protected void onPostExecute(R response) {
		boolean success = false;

		// We validate the response
		if(response != null){
			success = response.getSuccess();
		}

		// We show the status toast if it failed
		String pushMessage = "Push " +getTableName();
		if(!success){
			baseFragment.showToast(pushMessage+ " Failed!");
		}

		// We save the user and session information returned by the backend
		if(success){
			try {
				// We process the service response
				processResult(response);
			} catch (SQLException e) {
				Log.e(getLoggingTag(), "SQLException caught while processing " +pushMessage+ " response", e);
			}

			// We refresh the fragment we called the sync service from
			baseFragment.refreshFragment();
			
			// Executing next synchronized action
			executeOnSuccess();
		}
	}

}