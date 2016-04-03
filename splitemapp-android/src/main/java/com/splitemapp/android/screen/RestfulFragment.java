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
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.splitemapp.android.R;
import com.splitemapp.android.dialog.CustomProgressDialog;
import com.splitemapp.android.globals.Globals;
import com.splitemapp.android.screen.managecontacts.ManageContactsFragment;
import com.splitemapp.android.screen.welcome.WelcomeActivity;
import com.splitemapp.android.service.BaseIntentService;
import com.splitemapp.android.service.BaseTask;
import com.splitemapp.android.service.gcm.GcmRegistrationTask;
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
import com.splitemapp.android.service.sync.PushUserToProjectsTask;
import com.splitemapp.android.service.sync.PushUsersTask;
import com.splitemapp.android.service.sync.StartRefreshAnimationTask;
import com.splitemapp.android.service.sync.StopRefreshAnimationTask;
import com.splitemapp.android.service.sync.SynchronizeContactsTask;
import com.splitemapp.android.task.AddContactTask;
import com.splitemapp.android.task.CreateAccountRequestTask;
import com.splitemapp.android.task.LoginRequestTask;
import com.splitemapp.android.task.LogoutRequestTask;
import com.splitemapp.android.task.QuestionsTask;
import com.splitemapp.commons.constants.Action;
import com.splitemapp.commons.constants.ServiceConstants;

public abstract class RestfulFragment extends BaseFragment {

	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private static boolean syncInProgress;
	private CustomProgressDialog waitDialog = null;
	private BroadcastReceiver mRestBroadcastReceiver;
	private BroadcastReceiver mUiBroadcastReceiver;
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

		// Setting the REST broadcast receiver
		mRestBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// Processing the action after getting a broadcast
				String action = intent.getStringExtra(ServiceConstants.CONTENT_ACTION);
				String projectId = intent.getStringExtra(ServiceConstants.PROJECT_ID);

				Log.d("BroadcastReceiver", "Received REST_MESSAGE: " +action+ " for projectId: " +projectId);

				// If the message contains an action, execute the proper method
				if(action != null){
					// Starting refresh animation
					triggerStartRefreshAnimation();

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
					triggerStopRefreshAnimation();
				}
			}
		};

		// Setting the UI broadcast receiver
		mUiBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// Processing the action after getting a broadcast
				String response = intent.getStringExtra(ServiceConstants.CONTENT_RESPONSE);

				Log.d("BroadcastReceiver", "Received UI_MESSAGE: " +response);

				if(response!=null){
					// If there is a swipe refresh layout set, we update animation if required
					if(response.equals(BaseTask.START_ANIMATION)){
						syncInProgress = true;
						startRefreshAnimation();
					} else if (response.equals(BaseTask.STOP_ANIMATION)){
						syncInProgress = false;
						stopRefreshAnimation();
						// We call the overridden onRefresh method
						onRefresh(response);
					} else if (response.equals(BaseTask.NETWORK_ERROR)){
						syncInProgress = false;
						stopRefreshAnimation();
						showToast(getResources().getString(R.string.network_error));
					} else if (response.equals(BaseTask.GENERIC_ERROR)){
						syncInProgress = false;
						stopRefreshAnimation();
						showToast(getResources().getString(R.string.generic_error));
					} else if(response.equals(BaseTask.EXPENSES_PUSHED)){
						syncInProgress = false;
						stopRefreshAnimation();
						// We call the overridden onRefresh method
						onRefresh(response);
					}
				}
			}
		};
	}

	/**
	 * Starts the refresh animation and disables the refresh swipe
	 */
	private void startRefreshAnimation(){
		if(mSwipeRefresh != null){
			mSwipeRefresh.setEnabled(false);
			mSwipeRefresh.setRefreshing(true);
		}
	}

	/**
	 * Stops the refresh animation and enables the refresh swipe
	 */
	private void stopRefreshAnimation(){
		if(mSwipeRefresh != null){
			mSwipeRefresh.setEnabled(true);
			mSwipeRefresh.setRefreshing(false);
		}
	}

	/**
	 * Method called right after stopping the refresh animation
	 * @param response
	 */
	protected void onRefresh(String response){}

	@Override
	public void onStart() {
		super.onStart();

		if(syncInProgress){
			startRefreshAnimation();
		} else {
			stopRefreshAnimation();
		}

		// Registering broadcast receiver
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver((mRestBroadcastReceiver), 
				new IntentFilter(ServiceConstants.REST_MESSAGE));
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver((mUiBroadcastReceiver), 
				new IntentFilter(ServiceConstants.UI_MESSAGE));
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if(syncInProgress){
			startRefreshAnimation();
		} else {
			stopRefreshAnimation();
		}
	}

	@Override
	public void onStop() {
		// Unregistering broadcast receiver
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRestBroadcastReceiver);
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUiBroadcastReceiver);

		super.onStop();
	}

	@Override
	public void onPause() {
		// Unregistering broadcast receiver
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRestBroadcastReceiver);
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUiBroadcastReceiver);

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
			public void executeOnFail(String message) {
				hideProgressIndicator();
				showToastForMessage(message);
			}
		};
		createAccountRequestTask.execute();
	}

	/**
	 * Creates a service logout request
	 */
	public void logout(){
		// Creating the LogoutRequestTask instance
		LogoutRequestTask logoutRequestTask = new LogoutRequestTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();

				// We move to the welcome screen
				startActivity(new Intent(getActivity(), WelcomeActivity.class));
			}
			@Override
			public void executeOnFail(String message) {
				hideProgressIndicator();
				showToastForMessage(message);
			}
		};
		logoutRequestTask.execute();
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
					registerGcmToken();
				}

				// Synchronizing all tables for the first time
				pullAllTablesFirstTime();

				// Synchronizing contacts
				syncContacts();

				// Setting the global connected to server to true
				Globals.setIsConnectedToServer(true);
			}
			@Override
			public void executeOnFail(String message) {
				hideProgressIndicator();
				showToastForMessage(message);
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
		triggerStartRefreshAnimation();

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
		triggerStopRefreshAnimation();
	}

	/**
	 * Executes a linked list of asynchronous pull requests
	 */
	protected void pullAllTables(){
		// Starting refresh animation
		triggerStartRefreshAnimation();

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
		triggerStopRefreshAnimation();
	}

	/**
	 * Synchronize all contacts available in the device contacts list
	 */
	protected void syncContacts(){
		// Starting refresh animation
		triggerStartRefreshAnimation();

		// Starting sync contacts activity
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, SynchronizeContactsTask.class.getSimpleName());
		getActivity().startService(intent);

		// Stopping refresh animation
		triggerStopRefreshAnimation();
	}
	
	/**
	 * Adds the contact corresponding to the email address if found
	 * @param message
	 */
	protected void addContact(String email, final View emailView, final View successView, final View notFoundView, final ManageContactsFragment fragment){
		// Creating the LogoutRequestTask instance
		AddContactTask addContactTask = new AddContactTask(getHelper(),email){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				
				// Showing success dialog
				emailView.setVisibility(View.GONE);
				successView.setVisibility(View.VISIBLE);
				
				// Refreshing list
				fragment.refreshFragment();
			}
			@Override
			public void executeOnFail(String message) {
				hideProgressIndicator();
				showToastForMessage(message);
			}
			@Override
			protected void executeOnUserNotFound() {
				hideProgressIndicator();
				
				// Showing user not found dialog
				emailView.setVisibility(View.GONE);
				notFoundView.setVisibility(View.VISIBLE);
				
			}
		};
		addContactTask.execute();
	}
	
	/**
	 * Sends the question contained in the message parameter
	 * @param message
	 */
	protected void sendQuestion(String message, final View messageView, final View successView){
		// Creating the LogoutRequestTask instance
		QuestionsTask questionsTask = new QuestionsTask(getHelper(),message){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				
				// Showing success dialog
				messageView.setVisibility(View.GONE);
				successView.setVisibility(View.VISIBLE);
			}
			@Override
			public void executeOnFail(String message) {
				hideProgressIndicator();
				showToastForMessage(message);
			}
		};
		questionsTask.execute();
	}

	/**
	 * Executes an asynchronous pull request and initializes the sync data
	 */
	protected void pullAllTablesFirstTime(){
		// Initializing the synchronization table
		try {
			getHelper().initializeSyncStatus();
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "Exception while initializing synchronization table!", e);
		}

		// Pulling all tables
		pullAllTables();
	}

	/**
	 * Starts the refresh animation
	 */
	protected void triggerStartRefreshAnimation(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, StartRefreshAnimationTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Stops the refresh animation
	 */
	protected void triggerStopRefreshAnimation(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, StopRefreshAnimationTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user table pull request
	 */
	protected void pullUsers(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullUsersTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_contact_data table pull request
	 */
	protected void pullUserContactDatas(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullUserContactDatasTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_avatar table pull request
	 */
	protected void pullUserAvatars(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullUserAvatarsTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service project table pull request
	 */
	protected void pullProjects(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullProjectsTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service project_cover_image table pull request
	 */
	protected void pullProjectCoverImages(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullProjectCoverImagesTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_to_project table pull request
	 */
	protected void pullUserToProjects(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullUserToProjectsTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_invite table pull request
	 */
	protected void pullUserInvites(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullUserInvitesTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_expense table pull request
	 */
	protected void pullUserExpenses(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullUserExpensesTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_expense table pull request by project id
	 * @param projectId
	 */
	protected void pullUserExpensesByProject(String projectId){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, PullUserExpensesTask.class.getSimpleName());
		intent.putExtra(PullTask.EXTRA_PULL_ALL_DATES, true);
		intent.putExtra(PullTask.EXTRA_PROJECT_ID, projectId);
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user table push request
	 */
	protected void pushUserExpenses(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, PushUserExpensesTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_contact_data table push request
	 */
	protected void pushUserContactDatas(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, PushUserContactDatasTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_avatar table push request
	 */
	protected void pushUserAvatars(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, PushUserAvatarsTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a GCM token registration request
	 */
	protected void registerGcmToken(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, GcmRegistrationTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service project table push request
	 */
	protected void pushProjects(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, PushProjectsTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service project_cover_image table push request
	 */
	protected void pushProjectCoverImages(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, PushProjectCoverImagesTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_to_project table push request
	 */
	protected void pushUserToProjects(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, PushUserToProjectsTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user_invite table push request
	 */
	protected void pushUserInvites(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, PushUserInvitesTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	/**
	 * Creates a service user table push request
	 */
	protected void pushUsers(){
		Intent intent = new Intent(getActivity(), BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, PushUsersTask.class.getSimpleName());
		getActivity().startService(intent);
	}

	public SwipeRefreshLayout getSwipeRefresh() {
		return mSwipeRefresh;
	}

	public void setSwipeRefresh(SwipeRefreshLayout swipeRefresh) {
		mSwipeRefresh = swipeRefresh;
	}
}
