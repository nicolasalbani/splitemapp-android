package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.Date;

import android.util.Log;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.utils.NetworkUtils;
import com.splitemapp.commons.domain.dto.request.PullRequest;
import com.splitemapp.commons.domain.dto.response.PullResponse;

/**
 * Base Pull task
 * @author nicolas
 *
 * @param <E>
 */
public abstract class PullTask <E, R extends PullResponse<E>> extends BaseAsyncTask<Void, E, R> {

	public PullTask(DatabaseHelper databaseHelper) {
		super(databaseHelper);
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
	 * Processes the results coming from the service. This will typically contain DB inserts or updates
	 * @param response ServiceResponse that contains the list returned by the server
	 * @throws SQLException
	 */
	protected abstract void processResult(R response) throws SQLException;

	/**
	 * Returns the tag to be used for logging events 
	 * @return
	 */
	protected abstract String getLoggingTag();

	@Override
	protected void onPreExecute() {
		executeOnStart();
	}
	
	@Override
	protected R doInBackground(Void... params) {
		try {
			// We get the date in which this table was last successfully pulled
			Date lastPullSuccessAt = databaseHelper.getLastSuccessPullAt(getTableName());

			// We get the session token
			String sessionToken = databaseHelper.getSessionToken();

			// We create the pull request
			PullRequest pullRequest = new PullRequest();
			pullRequest.setLastPullSuccessAt(lastPullSuccessAt);
			pullRequest.setToken(sessionToken);

			// We call the rest service and send back the pull response
			return NetworkUtils.callRestService(getServicePath(), pullRequest, getResponseType());
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
		String pullMessage = "Pull " +getTableName();
		if(!success){
			executeOnFail();
		} else {
			// Saving the information returned by the back-end
			try {
				// We process the service response
				processResult(response);
			} catch (SQLException e) {
				Log.e(getLoggingTag(), "SQLException caught while processing " +pullMessage+ " response", e);
			}

			// Executing next synchronized action
			executeOnSuccess();
		}

	}
}
