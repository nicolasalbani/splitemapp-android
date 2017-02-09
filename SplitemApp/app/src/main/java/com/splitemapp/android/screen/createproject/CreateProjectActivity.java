package com.splitemapp.android.screen.createproject;


import android.support.v4.app.Fragment;

import com.splitemapp.android.screen.SingleFragmentActivity;

public class CreateProjectActivity extends SingleFragmentActivity {
	
	@Override
	protected Fragment createFragment() {
        return new CreateProjectFragment();
	}

	@Override
	public void onBackPressed() {
		// We create an intent to the ProjectActivity sending the information from the clicked project
		finish();
	}

}
