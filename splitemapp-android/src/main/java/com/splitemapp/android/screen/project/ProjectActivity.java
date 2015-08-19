package com.splitemapp.android.screen.project;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.splitemapp.android.screen.SingleFragmentActivity;

public class ProjectActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new ProjectFragment();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    // We hide the action bar
	    getSupportActionBar().hide();;
	}
	
	@Override
	public void onBackPressed() {
		// We create an intent to the ProjectActivity sending the information from the clicked project
		finish();
	}
}
