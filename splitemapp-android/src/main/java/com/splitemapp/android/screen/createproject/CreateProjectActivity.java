package com.splitemapp.android.screen.createproject;


import android.content.Intent;
import android.support.v4.app.Fragment;

import com.splitemapp.android.screen.SingleFragmentActivity;

public class CreateProjectActivity extends SingleFragmentActivity {
	
	public static final int MANAGE_USERS_REQUEST = 0;
	
	private CreateProjectFragment createProjectFragment = null;

	@Override
	protected Fragment createFragment() {
		createProjectFragment = new CreateProjectFragment(); 
		return createProjectFragment;
	}

	@Override
	public void onBackPressed() {
		// We create an intent to the ProjectActivity sending the information from the clicked project
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == MANAGE_USERS_REQUEST){
			createProjectFragment.updateUsersList(data);
		}
	}
}
