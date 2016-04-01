package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.splitemapp.android.service.BaseTask;
import com.splitemapp.android.utils.NetworkUtils;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.dto.request.PullRequest;
import com.splitemapp.commons.domain.dto.response.PullResponse;
import com.splitemapp.commons.utils.TimeUtils;

public abstract class PullTask <E, R extends PullResponse<E>> extends BaseTask {
	
	public static final String EXTRA_PULL_ALL_DATES = "com.splitemapp.android.service.PULL_ALL_DATES";
	public static final String EXTRA_PROJECT_ID = "com.splitemapp.android.service.PROJECT_ID";
	
	public PullTask(Context context) {
		super(context);
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
	 * Returns the tag to be used for logging events 
	 * @return
	 */
	protected abstract String getLoggingTag();

	/**
	 * Processes the results coming from the service. This will typically contain DB inserts or updates
	 * @param response ServiceResponse that contains the list returned by the server
	 * @throws SQLException
	 */
	protected abstract void processResult(R response) throws SQLException;
	

	@Override
	public void executeService(Intent intent) throws Exception {
		Log.i(getLoggingTag(), getServicePath() +" START");
		try {
			// We get the date in which this table was last successfully pulled
			Date lastPullSuccessAt = TimeUtils.getUTCDate(getHelper().getLastSuccessPullAt(getTableName()));

			// We get the session token
			String sessionToken = getHelper().getSessionToken();

			// We create the pull request
			PullRequest pullRequest = new PullRequest();
			pullRequest.setLastPullSuccessAt(lastPullSuccessAt);
			pullRequest.setToken(sessionToken);
			
			// If the EXTRA_PULL_ALL_DATES was set
			if(intent.getExtras().containsKey(EXTRA_PULL_ALL_DATES)){
				pullRequest.setPullAllDates(true);
			}
			
			// If the EXTRA_PROJECT_ID was set
			if(intent.getExtras().containsKey(EXTRA_PROJECT_ID)){
				pullRequest.setProjectId(Long.valueOf(intent.getExtras().getString(EXTRA_PROJECT_ID)));
			}

			// We call the rest service and send back the pull response
			R response = NetworkUtils.callRestService(getServicePath(), pullRequest, getResponseType());
			
			// Setting success to false by default
			boolean success = false;

			// We validate the response
			if(response != null){
				success = response.getSuccess();
			}

			String pullMessage = "Pull " +getTableName();
			if(!success){
				executeOnFail();
				broadcastMessage(pullMessage + " fail", ServiceConstants.UI_MESSAGE);
			} else {
				// Saving the information returned by the back-end
				try {
					if(response.getItemSet().size() > 0){
						Log.i(getLoggingTag(), getServicePath() +" Pulling " +response.getItemSet().size()+ " items");
					}
					
					// We process the service response
					processResult(response);
				} catch (SQLException e) {
					Log.e(getLoggingTag(), "SQLException caught while processing " +pullMessage+ " response", e);
				}

				// Executing next synchronized action
				executeOnSuccess();
				
				broadcastMessage(pullMessage + " success", ServiceConstants.UI_MESSAGE);
			}
		} finally {
			Log.i(getLoggingTag(), getServicePath() +" END");
		}
	}

}
