package com.splitemapp.android.screen.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.RestfulFragmentWithBlueActionbar;

public class SettingsFragment extends RestfulFragmentWithBlueActionbar {

	private static final String TAG = SettingsFragment.class.getSimpleName();


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = super.onCreateView(inflater, container, savedInstanceState);

		
		return v;
	}

	
	@Override
	protected void onRefresh(String response) {
		// Refreshing contacts list after making the sync
		refreshFragment();
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}

	@Override
	protected int getFragmentResourceId() {
		return R.layout.fragment_settings;
	}

	@Override
	protected int getTitleResourceId() {
		return R.string.s_title;
	}

	@Override
	protected void doneAction() {
		getActivity().finish();
	}
}
