package com.splitemapp.android.screen;

import java.sql.SQLException;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.splitemapp.android.R;
import com.splitemapp.android.dialog.CustomProgressDialog;
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
import com.splitemapp.android.task.ChangePasswordTask;
import com.splitemapp.android.task.CheckAccountTask;
import com.splitemapp.android.task.CreateAccountRequestTask;
import com.splitemapp.android.task.InviteTask;
import com.splitemapp.android.task.LoginRequestTask;
import com.splitemapp.android.task.LogoutRequestTask;
import com.splitemapp.android.task.PasswordResetTask;
import com.splitemapp.android.task.QuestionsTask;
import com.splitemapp.android.utils.PreferencesManager;
import com.splitemapp.commons.constants.Action;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.dto.response.CheckAccountResponse;
import com.splitemapp.commons.utils.Utils;

public abstract class RestfulFragment extends BaseFragment {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
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
    public void createAccount(final String email, final String userName, final String password, final String avatarUrl){
        // Creating the CreateAccountRequestTask instance with a custom executeOnSuccess
        CreateAccountRequestTask createAccountRequestTask = new CreateAccountRequestTask(getHelper(), email, userName, password, avatarUrl){
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

                // Logout from facebook
                LoginManager.getInstance().logOut();

                // Removing all data from local database
                try {
                    databaseHelper.clearDatabase();
                } catch (SQLException e) {
                    Log.e(getLoggingTag(), "SQLException caught!", e);
                }

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
                getPrefsManager().setBoolean(PreferencesManager.IS_CONNECTED_TO_SERVER, true);
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
     * Checks whether an account exists or not
     * @param userName String containing the user name
     */
    public void loginWithFacebook(final String email, final String name, final String id, final String pictureUrl){
        // Creating the CheckAccountTask instance with a custom executeOnSuccess
        CheckAccountTask checkAccountTask = new CheckAccountTask(getHelper(), email){
            @Override
            public void executeOnStart() {
                showProgressIndicator();
            }
            @Override
            public void executeOnSuccess() { }
            @Override
            public void executeOnFail(String message) {
                hideProgressIndicator();
                showToastForMessage(message);
            }
            @Override
            public void onPostExecute(CheckAccountResponse response) {
                boolean success = false;

                // Validating the response
                if(response != null){
                    success = response.getSuccess();
                } else {
                    executeOnFail(ServiceConstants.ERROR_MESSAGE_NETWORK_ERROR);
                    return;
                }

                // We login or create account as required
                if(response.getExists()){
                    login(email, com.splitemapp.android.utils.Utils.stringToHash(id));
                } else {
                    createAccount(email, name, com.splitemapp.android.utils.Utils.stringToHash(id), pictureUrl);
                }
            }
        };
        checkAccountTask.execute();
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
        if(checkReadContactPermissions()){
            // Starting refresh animation
            triggerStartRefreshAnimation();

            // Starting sync contacts activity
            Intent intent = new Intent(getActivity(), BaseIntentService.class);
            intent.putExtra(BaseTask.TASK_NAME, SynchronizeContactsTask.class.getSimpleName());
            getActivity().startService(intent);

            // Stopping refresh animation
            triggerStopRefreshAnimation();
        }
    }

    /**
     * Adds the contact corresponding to the email address if found
     * @param email
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
     * Changes the password for the logged user
     * @param currentPassword
     * @param newPassword
     * @param changePassView
     * @param successView
     */
    protected void changePassword(String currentPassword, String newPassword, final View changePassView, final View successView){
        try {
            String currentPasswordHash = databaseHelper.getUser(databaseHelper.getCurrentUserSession().getUser().getId()).getPassword();
            if(!currentPasswordHash.equals(Utils.hashPassword(currentPassword))){
                showToast(getResources().getString(R.string.s_current_pass_invalid));
                return;
            }
        } catch (SQLException e) {
            Log.e(getLoggingTag(), "SQLException exception caught!", e);
        }

        // Creating the ChangePasswordTask instance
        ChangePasswordTask changePasswordTask = new ChangePasswordTask(getHelper(),Utils.hashPassword(currentPassword),Utils.hashPassword(newPassword)){
            @Override
            public void executeOnStart() {
                showProgressIndicator();
            }
            @Override
            public void executeOnSuccess() {
                hideProgressIndicator();

                // Showing success dialog
                changePassView.setVisibility(View.GONE);
                successView.setVisibility(View.VISIBLE);
            }
            @Override
            public void executeOnFail(String message) {
                hideProgressIndicator();
                showToastForMessage(message);
            }
        };
        changePasswordTask.execute();
    }

    /**
     * Sends the invite for the provided email address
     * @param email
     * @param notFoundView
     * @param successView
     */
    protected void sendInvite(String email, final View notFoundView, final View successView){
        // Creating the LogoutRequestTask instance
        InviteTask inviteTask = new InviteTask(getHelper(),email){
            @Override
            public void executeOnStart() {
                showProgressIndicator();
            }
            @Override
            public void executeOnSuccess() {
                hideProgressIndicator();

                // Showing success dialog
                notFoundView.setVisibility(View.GONE);
                successView.setVisibility(View.VISIBLE);
            }
            @Override
            public void executeOnFail(String message) {
                hideProgressIndicator();
                showToastForMessage(message);
            }
        };
        inviteTask.execute();
    }

    /**
     * Sends the password reset email for the provided email address
     * @param email
     * @param emailView
     * @param successView
     */
    protected void sendPasswordReset(String email, final View emailView, final View successView){
        // Creating the PasswordResetTask instance
        PasswordResetTask passwordResetTask = new PasswordResetTask(getHelper(),email){
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
            }
            @Override
            public void executeOnFail(String message) {
                hideProgressIndicator();
                showToastForMessage(message);
            }
        };
        passwordResetTask.execute();
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
    public void pushUserExpenses(){
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
    public void pushUserToProjects(){
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

    /**
     * Validates that the required READ_CONTACTS permissions are in place. It asks for user input if required.
     * @return
     */
    private boolean checkReadContactPermissions(){
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                syncContacts();
            } else {
                showToast(getResources().getString(R.string.perm_contacts));
            }
        }
    }
}
