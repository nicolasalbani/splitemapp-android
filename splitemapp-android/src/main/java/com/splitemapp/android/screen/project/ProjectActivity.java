package com.splitemapp.android.screen.project;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.splitemapp.android.screen.SingleFragmentActivity;
import com.splitemapp.android.screen.home.HomeActivity;

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
		Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
		startActivity(intent);
	}
}
