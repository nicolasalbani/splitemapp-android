package com.splitemapp.android.screen.projectcontacts;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.RestfulFragmentWithBlueActionbar;
import com.splitemapp.android.service.BaseTask;
import com.splitemapp.android.utils.Utils;
import com.splitemapp.commons.domain.User;

public class ProjectContactsFragment extends RestfulFragmentWithBlueActionbar {

	private static final String TAG = ProjectContactsFragment.class.getSimpleName();

	private ContactsAdapter mContactsAdapter;

	private RecyclerView mContactsRecyclerView;
	private RecyclerView.LayoutManager mLayoutManager;
	
	private List<User> mListContacts;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Getting list of users already linked
		mListContacts = new ArrayList<User>();
		Bundle extras = getActivity().getIntent().getExtras();
		long[] userIdArray = extras.getLongArray(BaseTask.USER_ID_ARRAY_EXTRA);
		for(Long userId:userIdArray){
			try {
				mListContacts.add(getHelper().getUser(userId));
			} catch (SQLException e) {
				Log.e(TAG, "SQLException caught!", e);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflating the action bar and obtaining the View object
		View v = super.onCreateView(inflater, container, savedInstanceState);

		mContactsAdapter = new ContactsAdapter(this, mListContacts);

		// We populate the list of contacts not yet added to the project
		mContactsRecyclerView = (RecyclerView) v.findViewById(R.id.pc_contacts_recyclerView);
		mContactsRecyclerView.setAdapter(mContactsAdapter);

		// Using this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		mContactsRecyclerView.setHasFixedSize(true);

		// Using a linear layout manager
		mLayoutManager = new LinearLayoutManager(getActivity());
		mContactsRecyclerView.setLayoutManager(mLayoutManager);

		// Disabling DONE action
		setDoneActionGone();
		
		return v;
	}

	/**
	 * Returns an array containing all user IDs
	 * @return
	 */
	public long[] getUserIdArray(){
		return Utils.userListToIdArray(mListContacts);
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}

	@Override
	protected int getFragmentResourceId() {
		return R.layout.fragment_project_contacts;
	}

	@Override
	protected int getTitleResourceId() {
		return R.string.pc_title;
	}

	@Override
	protected void doneAction() {
		getActivity().finish();
	}
}
