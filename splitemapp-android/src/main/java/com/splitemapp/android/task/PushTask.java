package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import android.util.Log;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.utils.NetworkUtils;
import com.splitemapp.commons.domain.dto.request.PushRequest;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;
import com.splitemapp.commons.domain.dto.response.PushResponse;

/**
 * Base Push task
 * @author nicolas
 *
 * @param <E>
 */
public abstract class PushTask <F, E extends Number, R extends PushResponse<E>> extends BaseAsyncTask<F, E, PushLongResponse> {

	public PushTask(DatabaseHelper databaseHelper) {
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
	protected abstract void processResult(PushLongResponse response) throws SQLException;

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
	protected PushLongResponse doInBackground(Void... params) {
		try {
			// We get the session token
			String sessionToken = databaseHelper.getSessionToken();

			// We create the push request
			PushRequest<F> pushRequest = new PushRequest<F>();
			pushRequest.setToken(sessionToken);

			// We get the date in which this table was last successfully pulled
			Date lastPushSuccessAt = databaseHelper.getLastSuccessPushAt(getTableName());
			pushRequest.setLastPushSuccessAt(lastPushSuccessAt);

			// Getting the list of items to push
			List<F> requestItemList = getRequestItemList(lastPushSuccessAt);

			// Only calling push service if there are items to push
			if(requestItemList.size() > 0){
				pushRequest.setItemList(requestItemList);

				// We call the rest service and send back the login response
				return NetworkUtils.callRestService(getServicePath(), pushRequest, PushLongResponse.class);
			} else {
				PushLongResponse response = new PushLongResponse();
				response.setSuccess(true);
				return response;
			}
		} catch (Exception e) {
			Log.e(getLoggingTag(), e.getMessage(), e);
		}

		return null;
	}

	@Override
	protected void onPostExecute(PushLongResponse response) {
		boolean success = false;

		// We validate the response
		if(response != null){
			success = response.getSuccess();
		}

		// We show the status toast if it failed
		String pushMessage = "Push " +getTableName();
		if(!success){
			executeOnFail();
		} else {
			try {
				// We process the service response
				processResult(response);
			} catch (SQLException e) {
				Log.e(getLoggingTag(), "SQLException caught while processing " +pushMessage+ " response", e);
			}

			// Executing next synchronized action
			executeOnSuccess();
		}
	}

}