package com.splitemapp.android.screen;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.screen.home.HomeActivity;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.ExpenseCategory;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectStatus;
import com.splitemapp.commons.domain.ProjectType;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.UserExpense;
import com.splitemapp.commons.domain.UserSession;
import com.splitemapp.commons.domain.UserToProject;
import com.splitemapp.commons.domain.UserToProjectStatus;
import com.splitemapp.commons.domain.dto.ProjectDTO;
import com.splitemapp.commons.domain.dto.UserToProjectDTO;
import com.splitemapp.commons.domain.dto.request.PullAllSyncRequest;
import com.splitemapp.commons.domain.dto.response.PullAllSyncResponse;

public abstract class BaseFragment extends Fragment {

	protected DatabaseHelper databaseHelper = null;

	static{
		OpenHelperManager.setOpenHelperClass(DatabaseHelper.class);

		// We initialize logging
		java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
		java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);

		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
	}

	/**
	 * This method returns the desired TAG to be used for logging purposes
	 * @return
	 */
	public abstract String getLoggingTag();

	/**
	 * This method is called when the fragment is destroyed, releasing the database helper object
	 */
	public void onDestroy() {
		super.onDestroy();
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
	}

	/**
	 * This method calls the OpenHelperManager getHelper static method with the proper DatabaseHelper class reference 
	 * @return DatabaseHelper object which offers DAO for every domain entity
	 */
	protected DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
		}
		return databaseHelper;
	}

	/**
	 * 
	 * @param servicePath String containing the rest service name
	 * @param request <E> The request object used in the rest service call
	 * @param responseType <T> The response class that the rest service call is supposed to return
	 * @return	<T> An instance of the response type specified as a parameter
	 */
	protected <E,T> T callRestService(String servicePath, E request, Class<T> responseType){
		// We create the url based on the provider serviceName
		String url = "http://"+Constants.BACKEND_HOST+":"+Constants.BACKEND_PORT+"/"+Constants.BACKEND_PATH+servicePath;

		// We get an instance of the spring framework RestTemplate and configure wrapping the root XML element
		RestTemplate restTemplate = new RestTemplate();
		MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

		// We use old version of request factory that uses HTTPClient instead of HttpURLConnection to avoid bugs
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());  

		// We make the POST rest service call
		T response = restTemplate.postForObject(url, request, responseType);
		return response;
	}

	/**
	 * Convenience method to show a Toast with a particular message
	 * @param message String to be shown in the Toast
	 */
	protected void showToast(String message){
		Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * Gets the full path from an image URI
	 * @param uri URI from the image
	 * @return String containing the full path to the image
	 */
	protected String getImagePath(Uri uri) {
		// just some safety built in 
		if( uri == null ) {
			return null;
		}

		// try to retrieve the image from the media store first
		// this will only work for images selected from gallery
		String[] filePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = getActivity().getContentResolver().query(uri,filePathColumn, null, null, null);
		if( cursor != null ){
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			String imagePath = cursor.getString(column_index);
			cursor.close();
			return imagePath;
		}
		// this is our fallback here
		return uri.getPath();
	}

	/**
	 * Gets the logged user, if any
	 * @return User instance if logged, null otherwise
	 */
	protected User getLoggedUser(){
		User user = null;
		try {
			List<UserSession> userSessionList = getHelper().getUserSessionDao().queryForAll();
			if(userSessionList.size() > 0){
				UserSession userSession = userSessionList.get(userSessionList.size()-1);
				user = getUserById(userSession.getUser().getId());
			}
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}
		return user;
	}

	/**
	 * Deletes all existing user sessions in the DB
	 */
	protected void deleteAllUserSessions(){
		try {
			Dao<UserSession, Integer> userSessionDao = getHelper().getUserSessionDao();
			List<UserSession> userSessionList = userSessionDao.queryForAll();
			for(UserSession us:userSessionList){
				userSessionDao.deleteById(us.getId().intValue());
			}
		} catch (SQLException e) {
			Log.e(getLoggingTag(), e.getMessage(), e);
		}
	}

	/**
	 * Gets the User object for the userId
	 * @param userId Long containing the user id in the DB
	 * @return User instance
	 */
	protected User getUserById(Long userId){
		User user = null;
		try {
			user = getHelper().getUserDao().queryForId(userId.intValue());
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}
		return user;
	}

	/**
	 * Gets the Project object for the projectId
	 * @param projectId Long containing the project id in the DB
	 * @return Project instance
	 */
	protected Project getProjectById(Long projectId){
		Project project = null;
		try {
			project = getHelper().getProjectDao().queryForId(projectId.intValue());
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}
		return project;
	}

	/**
	 * Gets the UserExpense instance with its ExpenseCategory instance already loaded
	 * @param userExpenseId Long containing the user expense id from the DB
	 * @return UserExpense instance
	 */
	protected UserExpense getUserExpenseById(Long userExpenseId){
		UserExpense userExpense = null;
		try {
			// We get the user expense
			Dao<UserExpense,Integer> userExpensesDao = getHelper().getUserExpensesDao();
			userExpense = userExpensesDao.queryForId(userExpenseId.intValue());

			// We get the expense category
			Dao<ExpenseCategory,Integer> expenseCategoryDao = getHelper().getExpenseCategoryDao();
			ExpenseCategory expenseCategory = expenseCategoryDao.queryForId(userExpense.getExpenseCategory().getId().intValue());

			userExpense.setExpenseCategory(expenseCategory);
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}
		return userExpense;
	}

	/**
	 * Gets the user contact data from a particular user id
	 * @param userId Long containing the user id in the DB 
	 * @return UserContactData instance
	 */
	protected UserContactData getUserContactData(Long userId){
		UserContactData userContactData = null;
		try {
			Dao<UserContactData, Integer> userContactDataDao = getHelper().getUserContactDataDao();
			for(UserContactData ucd:userContactDataDao){
				if(ucd.getUser().getId().equals(userId)){
					userContactData = ucd;
				}
			}
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}

		return userContactData;
	}

	/**
	 * Starts the Home activity
	 * @param userId Long containing the user id from the local DB
	 */
	protected void startHomeActivity(Long userId){
		Intent intent = new Intent(getActivity(), HomeActivity.class);
		intent.putExtra(Constants.EXTRA_USER_ID, userId);
		startActivity(intent);
	}

	/**
	 * Sync class to pull all data from the remote DB for the last user session
	 * @author nicolas
	 */
	public class PullAllSyncTask extends AsyncTask<Void, Void, PullAllSyncResponse> {
		@Override
		protected PullAllSyncResponse doInBackground(Void... params) {
			try {
				// We create the login request
				PullAllSyncRequest pullAllSyncRequest = new PullAllSyncRequest();
				pullAllSyncRequest.setLastPullSuccessAt(new Date(100));
				UserSession userSession = getHelper().getUserSessionDao().queryForAll().get(0);
				pullAllSyncRequest.setToken(userSession.getToken());

				// We call the rest service and send back the login response
				return callRestService(ServiceConstants.PULL_ALL_SYNC_PATH, pullAllSyncRequest, PullAllSyncResponse.class);
			} catch (Exception e) {
				Log.e(getLoggingTag(), e.getMessage(), e);
			}

			return null;
		}

		@Override
		protected void onPostExecute(PullAllSyncResponse pullAllSyncResponse) {
			boolean loginSuccess = false;

			// We validate the response
			if(pullAllSyncResponse != null){
				loginSuccess = pullAllSyncResponse.getSuccess();
			}

			// We show the status toast
			showToast(loginSuccess ? "PullAllSync Successful!" : "PullAllSync Failed!");

			// We save the user and session information returned by the backend
			if(loginSuccess){
				try {
					// We save all project instances received
					Set<ProjectDTO> projectDTOs = pullAllSyncResponse.getProjectDTOs();
					for(ProjectDTO projectDTO:projectDTOs){
						ProjectStatus projectStatus = getHelper().getProjectStatusDao().queryForId(projectDTO.getProjectStatusId().intValue());
						ProjectType projectType = getHelper().getProjectTypeDao().queryForId(projectDTO.getProjectTypeId().intValue());
						Project project = new Project(projectType, projectStatus, projectDTO);
						getHelper().getProjectDao().createOrUpdate(project);
					}

					// We save all user_to_project instances received
					Set<UserToProjectDTO> userToProjectDTOs = pullAllSyncResponse.getUserToProjectDTOs();
					for(UserToProjectDTO userToProjectDTO:userToProjectDTOs){
						User user = getHelper().getUserDao().queryForId(userToProjectDTO.getUserId().intValue());
						Project project = getHelper().getProjectDao().queryForId(userToProjectDTO.getProjectId().intValue());
						UserToProjectStatus userToProjectStatus = getHelper().getUserToProjectStatusDao().queryForId(userToProjectDTO.getUserToProjectStatusId().intValue());
						UserToProject userToProject = new UserToProject(user, project, userToProjectStatus, userToProjectDTO);
						getHelper().getUserToProjectDao().createOrUpdate(userToProject);
					}
				} catch (SQLException e) {
					Log.e(getLoggingTag(), "SQLException caught while processing PullAllSync response", e);
				}
			}
		}
	}

}
