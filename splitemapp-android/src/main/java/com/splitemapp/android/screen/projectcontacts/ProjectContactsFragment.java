package com.splitemapp.android.screen.projectcontacts;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.BaseFragmentWithBlueActionbar;

public class ProjectContactsFragment extends BaseFragmentWithBlueActionbar {

	private static final String TAG = ProjectContactsFragment.class.getSimpleName();

	private ContactsAdapter mContactsAdapter;

	private RecyclerView mContactsRecyclerView;
	private RecyclerView.LayoutManager mLayoutManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflating the action bar and obtaining the View object
		View v = super.onCreateView(inflater, container, savedInstanceState);

		mContactsAdapter = new ContactsAdapter(this);

		// We populate the list of contacts not yet added to the project
		mContactsRecyclerView = (RecyclerView) v.findViewById(R.id.pc_contacts_recyclerView);
		mContactsRecyclerView.setAdapter(mContactsAdapter);

		// Using this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		mContactsRecyclerView.setHasFixedSize(true);

		// Using a linear layout manager
		mLayoutManager = new LinearLayoutManager(getActivity());
		mContactsRecyclerView.setLayoutManager(mLayoutManager);

		return v;
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
