package com.splitemapp.android.screen.createproject;


import android.content.Intent;
import android.support.v4.app.Fragment;

import com.splitemapp.android.screen.SingleFragmentActivity;
import com.splitemapp.android.screen.home.HomeActivity;

public class CreateProjectActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new CreateProjectFragment();
	}
	
	@Override
	public void onBackPressed() {
		// We create an intent to the ProjectActivity sending the information from the clicked project
		Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
		startActivity(intent);
	}
}
