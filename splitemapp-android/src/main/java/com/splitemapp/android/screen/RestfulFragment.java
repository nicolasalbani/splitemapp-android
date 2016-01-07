package com.splitemapp.android.screen;

import java.sql.SQLException;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.splitemapp.android.dialog.CustomProgressDialog;
import com.splitemapp.android.gcm.RegistrationIntentService;
import com.splitemapp.android.task.CreateAccountRequestTask;
import com.splitemapp.android.task.LoginRequestTask;
import com.splitemapp.android.task.PullAllTask;
import com.splitemapp.android.task.PullProjectCoverImagesTask;
import com.splitemapp.android.task.PullProjectsTask;
import com.splitemapp.android.task.PullUserAvatarsTask;
import com.splitemapp.android.task.PullUserContactDatasTask;
import com.splitemapp.android.task.PullUserExpensesTask;
import com.splitemapp.android.task.PullUserInvitesTask;
import com.splitemapp.android.task.PullUserToProjectsTask;
import com.splitemapp.android.task.PullUsersTask;
import com.splitemapp.android.task.PushAllTask;
import com.splitemapp.android.task.PushProjectCoverImagesTask;
import com.splitemapp.android.task.PushProjectsTask;
import com.splitemapp.android.task.PushUserAvatarsTask;
import com.splitemapp.android.task.PushUserContactDatasTask;
import com.splitemapp.android.task.PushUserExpensesTask;
import com.splitemapp.android.task.PushUserInvitesTask;
import com.splitemapp.android.task.PushUserToProjectsTask;
import com.splitemapp.android.task.PushUsersTask;
import com.splitemapp.android.task.SynchronizeContactsRequestTask;
import com.splitemapp.commons.constants.Action;

public abstract class RestfulFragment extends BaseFragment{

	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private CustomProgressDialog waitDialog = null;
	private BroadcastReceiver mBroadcastReceiver;

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
			public void onReceive(Context context, Intent intent) {
				// If the message contains an action, execute the proper method
				String action = intent.getStringExtra("ACTION");
				if(action != null){
					if(action.equals(Action.ADD_USER) || action.equals(Action.UPDATE_USER)){
						pullUsers();
					} else if (action.equals(Action.ADD_USER_AVATAR) || action.equals(Action.UPDATE_USER_AVATAR)){
						pullUserAvatars();
					} else if (action.equals(Action.ADD_USER_CONTACT_DATA) || action.equals(Action.UPDATE_USER_CONTACT_DATA)){
						pullUserContactDatas();
					} else if (action.equals(Action.ADD_PROJECT) || action.equals(Action.UPDATE_PROJECT)){
						pullProjects();
					} else if (action.equals(Action.ADD_PROJECT_COVER_IMAGE) || action.equals(Action.UPDATE_PROJECT_COVER_IMAGE)){
						pullProjectCoverImages();
					} else if (action.equals(Action.ADD_USER_TO_PROJECT) || action.equals(Action.UPDATE_USER_TO_PROJECT)){
						pullUserToProjects();
					} else if (action.equals(Action.ADD_USER_INVITE) || action.equals(Action.UPDATE_USER_INVITE)){
						pullUserInvites();
					} else if (action.equals(Action.ADD_USER_EXPENSE) || action.equals(Action.UPDATE_USER_EXPENSE)){
						pullUserExpenses();
					}
				}
			}
		};
	}

	@Override
	public void onStart() {
		super.onStart();

		// Registering broadcast receiver
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver((mBroadcastReceiver), 
				new IntentFilter("com.splitemapp.android.GCM_MESSAGE")
				);
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
	 * Creates an asynchronous new account request
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
	 * Creates an asynchronous login request
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
	 * Create an asynchronous synchronize contacts request
	 * @param contactsEmailAddressList List containing contacts email addresses
	 */
	public void synchronizeContacts(List<String> contactsEmailAddressList){
		SynchronizeContactsRequestTask synchronizeContactsRequestTask = new SynchronizeContactsRequestTask(getHelper(),contactsEmailAddressList){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Synchronize Contacts Failed!");
			}
		};
		synchronizeContactsRequestTask.execute();
	}

	/**
	 * Executes a linked list of asynchronous pull and push requests
	 */
	protected void syncAllTables(){
		// Calling all pull services
		final PullAllTask pullAllTask = new PullAllTask(getHelper()){
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Sync All Tables Failed!");
			}
		};

		// Calling all push services
		PushAllTask pushAllTask = new PushAllTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				pullAllTask.execute();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Sync All Tables Failed!");
			}
		};
		pushAllTask.execute();
	}

	/**
	 * Executes a linked list of asynchronous pull requests and initializes the Push sync data
	 */
	protected void syncAllTablesFirstTime(){
		PullAllTask task = new PullAllTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				try {
					databaseHelper.initializePushStatus();
				} catch (SQLException e) {
					showToast("Initialize Push Status Failed!");
				}
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Sync All Tables Failed!");
			}
		};
		task.execute();
	}

	/**
	 * Creates an asynchronous user table pull request
	 */
	protected void pullUsers(){
		PullUsersTask task = new PullUsersTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Pull Users Failed!");
			}
		};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_contact_data table pull request
	 */
	protected void pullUserContactDatas(){
		PullUserContactDatasTask task = new PullUserContactDatasTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Pull UserContactDatas Failed!");
			}
		};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_avatar table pull request
	 */
	protected void pullUserAvatars(){
		PullUserAvatarsTask task = new PullUserAvatarsTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Pull UserAvatars Failed!");
			}
		};
		task.execute();
	}

	/**
	 * Creates an asynchronous project table pull request
	 */
	protected void pullProjects(){
		PullProjectsTask task = new PullProjectsTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Pull Projects Failed!");
			}
		};
		task.execute();
	}

	/**
	 * Creates an asynchronous project_cover_image table pull request
	 */
	protected void pullProjectCoverImages(){
		PullProjectCoverImagesTask task = new PullProjectCoverImagesTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Pull ProjectCoverImages Failed!");
			}
		};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_to_project table pull request
	 */
	protected void pullUserToProjects(){
		PullUserToProjectsTask task = new PullUserToProjectsTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Pull UserToProjects Failed!");
			}
		};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_invite table pull request
	 */
	protected void pullUserInvites(){
		PullUserInvitesTask task = new PullUserInvitesTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Pull UserInvites Failed!");
			}
		};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_expense table pull request
	 */
	protected void pullUserExpenses(){
		PullUserExpensesTask task = new PullUserExpensesTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Pull UserExpenses Failed!");
			}
		};
		task.execute();
	}

	/**
	 * Creates an asynchronous user table push request
	 */
	protected void pushUsers(){
		PushUsersTask task = new PushUsersTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Push Users Failed!");
			}
		};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_contact_data table push request
	 */
	protected void pushUserContactDatas(){
		PushUserContactDatasTask task = new PushUserContactDatasTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Push UserContactDatas Failed!");
			}
		};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_avatar table push request
	 */
	protected void pushUserAvatars(){
		PushUserAvatarsTask task = new PushUserAvatarsTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Push UserAvatars Failed!");
			}
		};
		task.execute();
	}

	/**
	 * Creates an asynchronous project table push request
	 */
	protected void pushProjects(){
		PushProjectsTask task = new PushProjectsTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Push Projects Failed!");
			}
		};
		task.execute();
	}

	/**
	 * Creates an asynchronous project_cover_image table push request
	 */
	protected void pushProjectCoverImages(){
		PushProjectCoverImagesTask task = new PushProjectCoverImagesTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Push ProjectCoverImages Failed!");
			}
		};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_to_project table push request
	 */
	protected void pushUserToProjects(){
		PushUserToProjectsTask task = new PushUserToProjectsTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Push UserToProjects Failed!");
			}
		};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_invite table push request
	 */
	protected void pushUserInvites(){
		PushUserInvitesTask task = new PushUserInvitesTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Push UserInvites Failed!");
			}
		};
		task.execute();
	}

	/**
	 * Creates an asynchronous user_expense table pull request
	 */
	protected void pushUserExpenses(){
		PushUserExpensesTask task = new PushUserExpensesTask(getHelper()){
			@Override
			public void executeOnStart() {
				showProgressIndicator();
			}
			@Override
			public void executeOnSuccess() {
				hideProgressIndicator();
				refreshFragment();
			}
			@Override
			public void executeOnFail() {
				hideProgressIndicator();
				showToast("Push UserExpenses Failed!");
			}
		};
		task.execute();
	}
}
