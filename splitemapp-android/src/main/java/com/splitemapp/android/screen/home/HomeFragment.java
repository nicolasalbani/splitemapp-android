package com.splitemapp.android.screen.home;

import java.sql.SQLException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.animator.CustomItemAnimator;
import com.splitemapp.android.gsm.QuickstartPreferences;
import com.splitemapp.android.gsm.RegistrationIntentService;
import com.splitemapp.android.screen.RestfulFragment;
import com.splitemapp.android.screen.createproject.CreateProjectActivity;
import com.splitemapp.android.screen.managecontacts.ManageContactsActivity;
import com.splitemapp.android.screen.welcome.WelcomeActivity;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserContactData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class HomeFragment extends RestfulFragment {
	private static final String TAG = HomeFragment.class.getSimpleName();
	
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	private User mCurrentUser;
	private UserContactData mUserContactData;

	private ImageView mNavAvatar;
	private TextView mNavFullName;
	private TextView mNavEmail;
	private ImageView mMainAvatar;
	private TextView mMainFullName;
	private TextView mMainEmail;
	private FloatingActionButton mFab;

	private RecyclerView mProjectsRecycler;
	private SwipeProjectsAdapter mProjectsAdapter;
	private RecyclerView.LayoutManager mLayoutManager;

	private TextView mEmptyListHintTextView;

	private TextView mLogoutTextView;
	private TextView mManageContactsTextView;
	private TextView mSynchronizeTextView;
	
	private BroadcastReceiver mRegistrationBroadcastReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We inform that the activity hosting this fragment has an options menu
		setHasOptionsMenu(true);

		// We get the user and user contact data instances
		try {
			mCurrentUser = getHelper().getLoggedUser();
			mUserContactData = getHelper().getLoggedUserContactData();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
		
		// Start IntentService to register this application with GCM.
        if (checkPlayServices()) {
            Intent intent = new Intent(getActivity(), RegistrationIntentService.class);
            getActivity().startService(intent);
        }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_home, container, false);

		// We populate the first name in the navigation view
		mNavFullName = (TextView) v.findViewById(R.id.h_nav_full_name_textView);
		mNavFullName.setText(mCurrentUser.getFullName());

		// We populate the email in the navigation view
		mNavEmail = (TextView) v.findViewById(R.id.h_nav_email_textView);
		mNavEmail.setText(mUserContactData.getContactData());

		// We set the user avatar in the navigation view
		mNavAvatar = (ImageView) v.findViewById(R.id.h_nav_avatar_imageView);
		setUsetAvatar(mNavAvatar, mCurrentUser, ImageUtils.IMAGE_QUALITY_MAX);

		// We populate the first name in the main view
		mMainFullName = (TextView) v.findViewById(R.id.h_main_full_name_textView);
		mMainFullName.setText(mCurrentUser.getFullName());

		// We populate the email in the main view
		mMainEmail = (TextView) v.findViewById(R.id.h_main_email_textView);
		mMainEmail.setText(mUserContactData.getContactData());

		// We set the user avatar in the main view
		mMainAvatar = (ImageView) v.findViewById(R.id.h_main_avatar_imageView);
		setUsetAvatar(mMainAvatar, mCurrentUser, ImageUtils.IMAGE_QUALITY_MAX);

		// Creating a projects adapter to be used in the recycler view
		mProjectsAdapter = new SwipeProjectsAdapter(this);

		// We populate the list of projects for this user
		mProjectsRecycler = (RecyclerView) v.findViewById(R.id.h_projects_recyclerView);
		mProjectsRecycler.setAdapter(mProjectsAdapter);

		// Using this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		mProjectsRecycler.setHasFixedSize(true);

		// Using a linear layout manager
		mLayoutManager = new LinearLayoutManager(getActivity());
		mProjectsRecycler.setLayoutManager(mLayoutManager);

		// Setting the default animator for the view
		mProjectsRecycler.setItemAnimator(new CustomItemAnimator());

		// Getting the hint if project list is empty
		mEmptyListHintTextView = (TextView) v.findViewById(R.id.h_empty_list_hint_textView);

		// Adding action FABs to the main FAB
		mFab = (FloatingActionButton) v.findViewById(R.id.h_fab);
		mFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// We move to the project creation screen
				Intent intent = new Intent(getActivity(), CreateProjectActivity.class);
				startActivity(intent);
			}
		});

		mLogoutTextView = (TextView) v.findViewById(R.id.h_logout_textView);
		mLogoutTextView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// We delete all user sessions
				try {
					getHelper().deleteAllUserSessions();
				} catch (SQLException e) {
					Log.e(TAG, "SQLException caught!", e);
				}
				// We move to the welcome screen
				startActivity(new Intent(getActivity(), WelcomeActivity.class));
			}
		});

		mSynchronizeTextView = (TextView) v.findViewById(R.id.h_synchronize_textView);
		mSynchronizeTextView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// Pulling all tables
				syncAllTables();
			}
		});

		mManageContactsTextView = (TextView) v.findViewById(R.id.h_manage_contacts_textView);
		mManageContactsTextView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// We move to the login screen
				startActivity( new Intent(getActivity(), ManageContactsActivity.class));
			}
		});
		
		// If this user never synched before, we initialize the SyncStatus table
		try {
			if(!getHelper().isSyncInitialized()){
				syncAllTablesFirstTime();
			}
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
		
		// Setting the broadcast receiver for the GCM token
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                	showToast("GCM token retrieved and sent to server!");
                } else {
                	showToast("ERROR getting GCM token");
                }
			}
        };
		
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		// Refreshing project list 
		mProjectsAdapter.updateRecycler();

		// Showing or hiding the empty list hint
		if(mProjectsAdapter.getItemCount() == 0){
			mEmptyListHintTextView.setVisibility(View.VISIBLE);
		} else {
			mEmptyListHintTextView.setVisibility(View.GONE);
		}
	}
	
    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
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
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

	@Override
	public String getLoggingTag() {
		return TAG;
	}
}
