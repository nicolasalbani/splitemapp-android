package com.splitemapp.android.screen;

import java.sql.SQLException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.splitemapp.android.dialog.CustomProgressDialog;
import com.splitemapp.android.globals.Globals;
import com.splitemapp.android.service.BaseTask;
import com.splitemapp.android.service.SyncTablesService;
import com.splitemapp.android.service.gcm.RegistrationIntentService;
import com.splitemapp.android.service.sync.PullProjectCoverImagesTask;
import com.splitemapp.android.service.sync.PullProjectsTask;
import com.splitemapp.android.service.sync.PullTask;
import com.splitemapp.android.service.sync.PullUserAvatarsTask;
import com.splitemapp.android.service.sync.PullUserContactDatasTask;
import com.splitemapp.android.service.sync.PullUserExpensesTask;
import com.splitemapp.android.service.sync.PullUserInvitesTask;
import com.splitemapp.android.service.sync.PullUserToProjectsTask;
import com.splitemapp.android.service.sync.PullUsersTask;
import com.splitemapp.android.service.sync.PushProjectCoverImagesTask;
import com.splitemapp.android.service.sync.PushProjectsTask;
import com.splitemapp.android.service.sync.PushUserAvatarsTask;
import com.splitemapp.android.service.sync.PushUserContactDatasTask;
import com.splitemapp.android.service.sync.PushUserExpensesTask;
import com.splitemapp.android.service.sync.PushUserInvitesTask;
import com.splitemapp.android.service.sync.PushUserSessionsTask;
import com.splitemapp.android.service.sync.PushUserToProjectsTask;
import com.splitemapp.android.service.sync.PushUsersTask;
import com.splitemapp.android.service.sync.StartRefreshAnimationTask;
import com.splitemapp.android.service.sync.StopRefreshAnimationTask;
import com.splitemapp.android.service.sync.SynchronizeContactsTask;
import com.splitemapp.android.task.CreateAccountRequestTask;
import com.splitemapp.android.task.LoginRequestTask;
import com.splitemapp.commons.constants.Action;
import com.splitemapp.commons.constants.ServiceConstants;

public abstract class RestfulFragment extends BaseFragment {

	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private CustomProgressDialog waitDialog = null;
	private BroadcastReceiver mBroadcastReceiver;
	private SwipeRefreshLayout mSwipeRefresh;

	static{
		// We initialize logging
		java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
		java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);

		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setting the broadcast receiver for the GCM communication
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public synchronized void onReceive(Context context, Intent intent) {
				// Processing the action after getting a broadcast
				String action = intent.getStringExtra(ServiceConstants.CONTENT_ACTION);
				String projectId = intent.getStringExtra(ServiceConstants.PROJECT_ID);
				processAction(action, projectId);

				String response = intent.getStringExtra(ServiceConstants.CONTENT_RESPONSE);
				if(response!=null){

					// If there is a swipe refresh layout set, we update animation if required
					if(response.equals(BaseTask.START_ANIMATION)){
						if(mSwipeRefresh != null){
							mSwipeRefresh.setEnabled(false);
							mSwipeRefresh.setRefreshing(true);
						}
					} else if (response.equals(BaseTask.STOP_ANIMATION)){
						if(mSwipeRefresh != null){
							mSwipeRefresh.setEnabled(true);
							mSwipeRefresh.setRefreshing(false);
						}
						// We call the overridden onRefresh method
						onRefresh(response);
					} else if (response.equals(BaseTask.NETWORK_ERROR)){
						showToast("Check your network connection!");
					}
				}
			}
		};
	}

	/**
	 * Method called right after stopping the refresh animation
	 * @param response
	 */
	protected void onRefresh(String response){}

	/**
	 * Processes the provided action
	 * @param action
	 */
	private void processAction(String action, String projectId){
		// If the message contains an action, execute the proper method
		if(action != null){
			// Starting refresh animation
			startRefreshAnimation();

			// Checking for GCM actions
			if(action.equals(Action.REGISTER_GCM)){
				pushUserSessions();
			}

			// Checking for SYNC actions
			if(action.equals(Action.UPDATE_USER)){
				pullUsers();
			} else if (action.equals(Action.UPDATE_USER_AVATAR)){
				pullUserAvatars();
			} else if (action.equals(Action.ADD_USER_CONTACT_DATA) || action.equals(Action.UPDATE_USER_CONTACT_DATA)){
				pullUserContactDatas();
			} else if (action.equals(Action.UPDATE_PROJECT)){
				pullProjects();
			} else if (action.equals(Action.ADD_PROJECT_COVER_IMAGE)){
				pullProjects();
				pullProjectCoverImages();
			} else if (action.equals(Action.UPDATE_PROJECT_COVER_IMAGE)){
				pullProjectCoverImages();
			} else if (action.equals(Action.ADD_USER_TO_PROJECT)){
				// In case some users for this project are not in the local database
				pullUsers();
				pullUserAvatars();
				pullUserContactDatas();
				// Assuming this is a new project to which this user was added
				pullProjects();
				pullProjectCoverImages();
				// Actually pulling the user to project relationships 
				pullUserToProjects();
				// Pulling all user expenses for that project
				pullUserExpensesByProject(projectId);
			} else if (action.equals(Action.UPDATE_USER_TO_PROJECT)){
				pullUserToProjects();
			} else if (action.equals(Action.ADD_USER_INVITE) || action.equals(Action.UPDATE_USER_INVITE)){
				pullUserInvites();
			} else if (action.equals(Action.ADD_USER_EXPENSE) || action.equals(Action.UPDATE_USER_EXPENSE)){
				pullUserExpenses();
			}

			// Stopping refresh animation
			stopRefreshAnimation();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		// Registering broadcast receiver
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver((mBroadcastReceiver), 
				new IntentFilter(ServiceConstants.GCM_MESSAGE));
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver((mBroadcastReceiver), 
				new IntentFilter(ServiceConstants.REST_MESSAGE));
	}

	@Override
	public void onStop() {
		// Unregistering broadcast receiver
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);

		super.onStop();
	}

	@Override
	public void onPause() {
		// Unregistering broadcast receiver
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);

		super.onPause();
	}

	/**
	 * Shows the progress indicator
	 */
	public void showProgressIndicator(){
		if(waitDialog == null || !waitDialog.isShowing()){
			waitDialog = CustomProgressDialog.show(getContext());
		}
	}

	/**
	 * Hides the progress indicator
	 */
	public void hideProgressIndicator(){
		if(waitDialog != null && waitDialog.isShowing()){
			waitDialog.dismiss();
		}
	}

	/**
	 * Creates a service new account request
	 * @param email	String containing the email address
	 * @param userName String containing the user name
	 * @param password String containing the password
	 */
	public void createAccount(final String email, final String userName, final String password, final byte[] avatar){
		// Creating the CreateAccountRequestTask instance with a custom executeOnSuccess
		CreateAccountRequestTask createAccountRequestTask = new CreateAccountRequestTask(getHelper(), email, userName, password, avatar){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				login(email, password);
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Create Account Failed!");
			}
		};
		createAccountRequestTask.execute();
	}

	/**
	 * Creates a service login request
	 * @param userName String containing the user name
	 * @param password String containing the password
	 */
	public void login(String userName, String password){
		// Creating the LoginRequestTask instance with a custom executeOnSuccess
		LoginRequestTask loginRequestTask = new LoginRequestTask(getHelper(), userName, password){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				startHomeActivity();

				// Start IntentService to register this application with GCM.
				if (checkPlayServices()) {
					Intent intent = new Intent(getActivity(), RegistrationIntentService.class);
					getActivity().startService(intent);
				}
				
				// Setting the global connected to server to true
				Globals.setIsConnectedToServer(true);
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Login Failed!");
			}
		};
		loginRequestTask.execute();
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
		int resultCode = apiAvailability.isGooglePlayServicesAvailable(getActivity());
		if (resultCode != ConnectionResult.SUCCESS) {
			if (apiAvailability.isUserResolvableError(resultCode)) {
				apiAvailability.getErrorDialog(getActivity(), resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
				.show();
			}
			return false;
		}
		return true;
	}

	/**
	 * Executes a linked list of asynchronous pull and push requests
	 */
	protected void syncAllTables(){
		// Starting refresh animation
		startRefreshAnimation();

		// Calling all push services
		pushUsers();
		pushUserAvatars();
		pushUserContactDatas();
		pushProjects();
		pushProjectCoverImages();
		pushUserToProjects();
		pushUserInvites();
		pushUserExpenses();

		// Calling all pull services
		pullUsers();
		pullUserAvatars();
		pullUserContactDatas();
		pullProjects();
		pullProjectCoverImages();
		pullUserToProjects();
		pullUserInvites();
		pullUserExpenses();

		// Stopping refresh animation
		stopRefreshAnimation();
	}

	/**
	 * Executes a linked list of asynchronous pull requests
	 */
	protected void pullAllTables(){
		// Starting refresh animation
		startRefreshAnimation();

		// Calling all pull services
		pullUsers();
		pullUserAvatars();
		pullUserContactDatas();
		pullProjects();
		pullProjectCoverImages();
		pullUserToProjects();
		pullUserInvites();
		pullUserExpenses();

		// Stopping refresh animation
		stopRefreshAnimation();
	}
	
	/**
	 * Synchronize all contacts available in the device contacts list
	 */
	protected void syncContacts(){
		// Starting refresh animation
		startRefreshAnimation();
		
		// Starting sync contacts activity
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, SynchronizeContactsTask.class.getSimpleName());
		getActivity().startService(intent);
		
		// Stopping refresh animation
		stopRefreshAnimation();
	}

	/**
	 * Executes a linked list of asynchronous pull requests and initializes the Push sync data
	 */
	protected void syncAllTablesFirstTime(){
		pullAllTables();

		// Initializing push status
		try {
			getHelper().initializePushStatus();
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "Exception while initializing push status!", e);
		}
	}

	/**
	 * Starts the refresh animation
	 */
	protected void startRefreshAnimation(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, StartRefreshAnimationTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Stops the refresh animation
	 */
	protected void stopRefreshAnimation(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, StopRefreshAnimationTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user table pull request
	 */
	protected void pullUsers(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullUsersTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_contact_data table pull request
	 */
	protected void pullUserContactDatas(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullUserContactDatasTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_avatar table pull request
	 */
	protected void pullUserAvatars(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullUserAvatarsTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service project table pull request
	 */
	protected void pullProjects(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullProjectsTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service project_cover_image table pull request
	 */
	protected void pullProjectCoverImages(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullProjectCoverImagesTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_to_project table pull request
	 */
	protected void pullUserToProjects(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullUserToProjectsTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_invite table pull request
	 */
	protected void pullUserInvites(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullUserInvitesTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_expense table pull request
	 */
	protected void pullUserExpenses(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullUserExpensesTask.class.getSimpleName());
		getActivity().startService(intent);
	}
	
	/**
	 * Creates a service user_expense table pull request by project id
	 * @param projectId
	 */
	protected void pullUserExpensesByProject(String projectId){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullUserExpensesTask.class.getSimpleName());
		intent.putExtra(PullTask.EXTRA_PULL_ALL_DATES, true);
		intent.putExtra(PullTask.EXTRA_PROJECT_ID, projectId);
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user table push request
	 */
	protected void pushUserExpenses(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PushUserExpensesTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_contact_data table push request
	 */
	protected void pushUserContactDatas(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PushUserContactDatasTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_avatar table push request
	 */
	protected void pushUserAvatars(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PushUserAvatarsTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service project table push request
	 */
	protected void pushProjects(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PushProjectsTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service project_cover_image table push request
	 */
	protected void pushProjectCoverImages(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PushProjectCoverImagesTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_to_project table push request
	 */
	protected void pushUserToProjects(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PushUserToProjectsTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_invite table push request
	 */
	protected void pushUserInvites(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PushUserInvitesTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user table push request
	 */
	protected void pushUsers(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PushUsersTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_session table push request
	 */
	protected void pushUserSessions(){
		Intent intent = new Intent(getActivity(), SyncTablesService.class);
		intent.putExtra(BaseTask.TASK_NAME, PushUserSessionsTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	public SwipeRefreshLayout getSwipeRefresh() {
		return mSwipeRefresh;
	}

	public void setSwipeRefresh(SwipeRefreshLayout swipeRefresh) {
		mSwipeRefresh = swipeRefresh;
	}
}
