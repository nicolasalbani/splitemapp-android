package com.splitemapp.android.screen;

import java.sql.SQLException;
import java.util.Date;
import java.util.Set;

import android.os.AsyncTask;
import android.util.Log;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectStatus;
import com.splitemapp.commons.domain.ProjectType;
import com.splitemapp.commons.domain.dto.ProjectDTO;
import com.splitemapp.commons.domain.dto.request.PullRequest;
import com.splitemapp.commons.domain.dto.response.PullProjectsResponse;
import com.splitemapp.commons.domain.dto.response.ServiceResponse;

public abstract class SynchronizableFragment extends RestfulFragment{

	/**
	 * Creates an asynchronous projects pull request
	 */
	protected void pullProjects(){
		new PullProjectsTask().execute();
	}

	/**
	 * Sync Task to pull all data from the remote DB for the last user session
	 * @author nicolas
	 */
	private class PullProjectsTask extends PullTask<PullProjectsResponse> {
		@Override
		protected String getTableName(){
			return "project";
		}

		@Override
		protected String getServicePath(){
			return ServiceConstants.PULL_PROJECTS_PATH;
		}

		@Override
		protected Class<PullProjectsResponse> getResponseClass(){
			return PullProjectsResponse.class;
		}

		@Override
		protected void processResult(PullProjectsResponse response) throws SQLException {
			Set<ProjectDTO> projectDTOs = response.getProjectDTOs();
			for(ProjectDTO projectDTO:projectDTOs){
				ProjectStatus projectStatus = getHelper().getProjectStatusDao().queryForId(projectDTO.getProjectStatusId().shortValue());
				ProjectType projectType = getHelper().getProjectTypeDao().queryForId(projectDTO.getProjectTypeId().shortValue());
				Project project = new Project(projectType, projectStatus, projectDTO);
				CreateOrUpdateStatus createOrUpdate = getHelper().getProjectDao().createOrUpdate(project);
				getHelper().updateSyncStatusPullAt(Project.class, createOrUpdate);
			}
		}
	}

	/**
	 * Base Pull task
	 * @author nicolas
	 *
	 * @param <E>
	 */
	protected abstract class PullTask <E extends ServiceResponse> extends AsyncTask<Void, Void, E> {

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
		 * Gets the response class
		 * @return
		 */
		protected abstract Class<E> getResponseClass();

		/**
		 * Processes the results coming from the service. This will typically contain DB inserts or updates
		 * @param response ServiceResponse that contains the list returned by the server
		 * @throws SQLException
		 */
		protected abstract void processResult(E response) throws SQLException;

		@Override
		protected E doInBackground(Void... params) {
			try {
				// We get the date in which this table was last successfully pulled
				Date lastPullSuccessAt = getHelper().getSyncStatusDao().queryForEq(TableField.SYNC_STATUS_TABLE_NAME, getTableName()).get(0).getLastPullSuccessAt();

				// We get the session token
				String sessionToken = getHelper().getSessionToken();

				// We create the login request
				PullRequest pullRequest = new PullRequest();
				pullRequest.setLastPullSuccessAt(lastPullSuccessAt);
				pullRequest.setToken(sessionToken);

				// We call the rest service and send back the login response
				return callRestService(getServicePath(), pullRequest, getResponseClass());
			} catch (Exception e) {
				Log.e(getLoggingTag(), e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(E response) {
			boolean pullSuccess = false;

			// We validate the response
			if(response != null){
				pullSuccess = response.getSuccess();
			}

			// We show the status toast
			String pullMessage = "Pull " +getTableName();
			showToast(pullSuccess ? pullMessage+ " Successful!" : pullMessage+ " Failed!");

			// We save the user and session information returned by the backend
			if(pullSuccess){
				try {
					// We process the service response
					processResult(response);
				} catch (SQLException e) {
					Log.e(getLoggingTag(), "SQLException caught while processing " +pullMessage+ " response", e);
				}

				// We refresh the fragment we called the sync service from
				refreshFragment();
			}
		}

	}
}
