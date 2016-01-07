package com.splitemapp.android.screen.createproject;


import java.util.ArrayList;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.splitemapp.android.globals.Globals;
import com.splitemapp.android.screen.SingleFragmentActivity;
import com.splitemapp.android.screen.home.HomeActivity;
import com.splitemapp.commons.domain.User;

public class CreateProjectActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new CreateProjectFragment();
	}

	@Override
	public void onBackPressed() {
		// Resetting the global create project - user list
		Globals.setCreateProjectActivityUserList(new ArrayList<User>());

		// Resetting the global create project - project id
		Globals.setCreateProjectActivityProjectId(null);

		// We create an intent to the ProjectActivity sending the information from the clicked project
		Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
		startActivity(intent);
	}
}
