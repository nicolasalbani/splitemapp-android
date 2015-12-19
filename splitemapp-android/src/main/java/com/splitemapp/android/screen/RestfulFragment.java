package com.splitemapp.android.screen;

import java.sql.SQLException;
import java.util.List;

import com.splitemapp.android.dialog.CustomProgressDialog;
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

public abstract class RestfulFragment extends BaseFragment{

	private CustomProgressDialog waitDialog = null;

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
