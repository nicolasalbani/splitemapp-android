package com.splitemapp.android.screen;

import java.util.List;

import com.splitemapp.android.dialog.CustomProgressDialog;
import com.splitemapp.android.task.CreateAccountRequestTask;
import com.splitemapp.android.task.LoginRequestTask;
import com.splitemapp.android.task.PullProjectCoverImagesTask;
import com.splitemapp.android.task.PullProjectsTask;
import com.splitemapp.android.task.PullUserAvatarsTask;
import com.splitemapp.android.task.PullUserContactDatasTask;
import com.splitemapp.android.task.PullUserExpensesTask;
import com.splitemapp.android.task.PullUserInvitesTask;
import com.splitemapp.android.task.PullUserToProjectsTask;
import com.splitemapp.android.task.PullUsersTask;
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
		// Showing the progress indicator
		showProgressIndicator();

		// Creating the CreateAccountRequestTask instance with a custom executeOnSuccess
		CreateAccountRequestTask createAccountRequestTask = new CreateAccountRequestTask(getHelper(), this, email, userName, password, avatar){
			@Override
			protected void executeOnSuccess() {
				hideProgressIndicator();
				// We login the user after the account was created
				login(email, password);
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
		// Showing the progress indicator
		showProgressIndicator();

		// Creating the LoginRequestTask instance with a custom executeOnSuccess
		LoginRequestTask loginRequestTask = new LoginRequestTask(getHelper(), this, userName, password){
			@Override
			protected void executeOnSuccess() {
				// Hiding the progress indicator
				hideProgressIndicator();
				// Starting the home activity
				startHomeActivity();
			}
		};
		loginRequestTask.execute();
	}

	/**
	 * Create an asynchronous synchronize contacts request
	 * @param contactsEmailAddressList List containing contacts email addresses
	 */
	public void synchronizeContacts(List<String> contactsEmailAddressList){
		// Showing the progress indicator
		showProgressIndicator();

		SynchronizeContactsRequestTask synchronizeContactsRequestTask = new SynchronizeContactsRequestTask(getHelper(), this,contactsEmailAddressList){
			@Override
			protected void executeOnSuccess() {
				hideProgressIndicator();
			}
		};
		synchronizeContactsRequestTask.execute();
	}

	/**
	 * Creates a linked list of asynchronous pull and push requests
	 */
	protected void syncAllTables(){
		// Showing progress indicator
		showProgressIndicator();

		// Calling all push services
		final PushUserExpensesTask pushUserExpensesTask = new PushUserExpensesTask(getHelper(), this){protected void executeOnSuccess() {hideProgressIndicator();}};
		final PushUserInvitesTask pushUserInvitesTask = new PushUserInvitesTask(getHelper(), this){protected void executeOnSuccess() {pushUserExpensesTask.execute();}};
		final PushUserToProjectsTask pushUserToProjectsTask = new PushUserToProjectsTask(getHelper(), this){protected void executeOnSuccess() {pushUserInvitesTask.execute();}};
		final PushProjectCoverImagesTask pushProjectCoverImagesTask = new PushProjectCoverImagesTask(getHelper(), this){protected void executeOnSuccess() {pushUserToProjectsTask.execute();}};
		final PushProjectsTask pushProjectsTask = new PushProjectsTask(getHelper(), this){protected void executeOnSuccess() {pushProjectCoverImagesTask.execute();}};
		final PushUserContactDatasTask pushUserContactDatasTask = new PushUserContactDatasTask(getHelper(), this){protected void executeOnSuccess() {pushProjectsTask.execute();}};
		final PushUserAvatarsTask pushUserAvatarsTask = new PushUserAvatarsTask(getHelper(), this){protected void executeOnSuccess() {pushUserContactDatasTask.execute();}};
		final PushUsersTask pushUsersTask = new PushUsersTask(getHelper(), this){protected void executeOnSuccess() {pushUserAvatarsTask.execute();}};

		// Calling all pull services
		final PullUserExpensesTask pullUserExpensesTask = new PullUserExpensesTask(getHelper(), this){protected void executeOnSuccess() {pushUsersTask.execute();}};
		final PullUserInvitesTask pullUserInvitesTask = new PullUserInvitesTask(getHelper(), this){protected void executeOnSuccess() {pullUserExpensesTask.execute();}};
		final PullUserToProjectsTask pullUserToProjectsTask = new PullUserToProjectsTask(getHelper(), this){protected void executeOnSuccess() {pullUserInvitesTask.execute();}};
		final PullProjectCoverImagesTask pullProjectCoverImagesTask = new PullProjectCoverImagesTask(getHelper(), this){protected void executeOnSuccess() {pullUserToProjectsTask.execute();}};
		final PullProjectsTask pullProjectsTask = new PullProjectsTask(getHelper(), this){protected void executeOnSuccess() {pullProjectCoverImagesTask.execute();}};
		final PullUserContactDatasTask pullUserContactDatasTask = new PullUserContactDatasTask(getHelper(), this){protected void executeOnSuccess() {pullProjectsTask.execute();}};
		final PullUserAvatarsTask pullUserAvatarsTask = new PullUserAvatarsTask(getHelper(), this){protected void executeOnSuccess() {pullUserContactDatasTask.execute();}};
		PullUsersTask pullUsersTask = new PullUsersTask(getHelper(), this){protected void executeOnSuccess() {pullUserAvatarsTask.execute();}};
		pullUsersTask.execute();
	}

	/**
	 * Creates a linked list of asynchronous pull requests
	 */
	protected void pullAllTables(){
		// Showing progress indicator
		showProgressIndicator();

		// Calling all pull services
		final PullUserExpensesTask pullUserExpensesTask = new PullUserExpensesTask(getHelper(), this){protected void executeOnSuccess() {hideProgressIndicator();}};
		final PullUserInvitesTask pullUserInvitesTask = new PullUserInvitesTask(getHelper(), this){protected void executeOnSuccess() {pullUserExpensesTask.execute();}};
		final PullUserToProjectsTask pullUserToProjectsTask = new PullUserToProjectsTask(getHelper(), this){protected void executeOnSuccess() {pullUserInvitesTask.execute();}};
		final PullProjectCoverImagesTask pullProjectCoverImagesTask = new PullProjectCoverImagesTask(getHelper(), this){protected void executeOnSuccess() {pullUserToProjectsTask.execute();}};
		final PullProjectsTask pullProjectsTask = new PullProjectsTask(getHelper(), this){protected void executeOnSuccess() {pullProjectCoverImagesTask.execute();}};
		final PullUserContactDatasTask pullUserContactDatasTask = new PullUserContactDatasTask(getHelper(), this){protected void executeOnSuccess() {pullProjectsTask.execute();}};
		final PullUserAvatarsTask pullUserAvatarsTask = new PullUserAvatarsTask(getHelper(), this){protected void executeOnSuccess() {pullUserContactDatasTask.execute();}};
		PullUsersTask pullUsersTask = new PullUsersTask(getHelper(), this){protected void executeOnSuccess() {pullUserAvatarsTask.execute();}};
		pullUsersTask.execute();
	}

	/**
	 * Creates an asynchronous user table pull request
	 */
	protected void pullUsers(){
		PullUsersTask task = new PullUsersTask(getHelper(), this);
		task.execute();
	}

	/**
	 * Creates an asynchronous user_contact_data table pull request
	 */
	protected void pullUserContactDatas(){
		PullUserContactDatasTask task = new PullUserContactDatasTask(getHelper(), this);
		task.execute();
	}

	/**
	 * Creates an asynchronous user_avatar table pull request
	 */
	protected void pullUserAvatars(){
		PullUserAvatarsTask task = new PullUserAvatarsTask(getHelper(), this);
		task.execute();
	}

	/**
	 * Creates an asynchronous project table pull request
	 */
	protected void pullProjects(){
		PullProjectsTask task = new PullProjectsTask(getHelper(), this);
		task.execute();
	}

	/**
	 * Creates an asynchronous project_cover_image table pull request
	 */
	protected void pullProjectCoverImages(){
		PullProjectCoverImagesTask task = new PullProjectCoverImagesTask(getHelper(), this);
		task.execute();
	}

	/**
	 * Creates an asynchronous user_to_project table pull request
	 */
	protected void pullUserToProjects(){
		PullUserToProjectsTask task = new PullUserToProjectsTask(getHelper(), this);
		task.execute();
	}

	/**
	 * Creates an asynchronous user_invite table pull request
	 */
	protected void pullUserInvites(){
		PullUserInvitesTask task = new PullUserInvitesTask(getHelper(), this);
		task.execute();
	}

	/**
	 * Creates an asynchronous user_expense table pull request
	 */
	protected void pullUserExpenses(){
		PullUserExpensesTask task = new PullUserExpensesTask(getHelper(), this);
		task.execute();
	}

	/**
	 * Creates an asynchronous user table push request
	 */
	protected void pushUsers(){
		PushUsersTask task = new PushUsersTask(getHelper(), this);
		task.execute();
	}

	/**
	 * Creates an asynchronous user_contact_data table push request
	 */
	protected void pushUserContactDatas(){
		PushUserContactDatasTask task = new PushUserContactDatasTask(getHelper(), this);
		task.execute();
	}

	/**
	 * Creates an asynchronous user_avatar table push request
	 */
	protected void pushUserAvatars(){
		PushUserAvatarsTask task = new PushUserAvatarsTask(getHelper(), this);
		task.execute();
	}

	/**
	 * Creates an asynchronous project table push request
	 */
	protected void pushProjects(){
		PushProjectsTask task = new PushProjectsTask(getHelper(), this);
		task.execute();
	}

	/**
	 * Creates an asynchronous project_cover_image table push request
	 */
	protected void pushProjectCoverImages(){
		PushProjectCoverImagesTask task = new PushProjectCoverImagesTask(getHelper(), this);
		task.execute();
	}

	/**
	 * Creates an asynchronous user_to_project table push request
	 */
	protected void pushUserToProjects(){
		PushUserToProjectsTask task = new PushUserToProjectsTask(getHelper(), this);
		task.execute();
	}

	/**
	 * Creates an asynchronous user_invite table push request
	 */
	protected void pushUserInvites(){
		PushUserInvitesTask task = new PushUserInvitesTask(getHelper(), this);
		task.execute();
	}

	/**
	 * Creates an asynchronous user_expense table pull request
	 */
	protected void pushUserExpenses(){
		PushUserExpensesTask task = new PushUserExpensesTask(getHelper(), this);
		task.execute();
	}
}
