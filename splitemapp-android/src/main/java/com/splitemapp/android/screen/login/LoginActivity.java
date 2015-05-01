package com.splitemapp.android.screen.login;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.splitemapp.android.screen.SingleFragmentActivity;

public class LoginActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new LoginFragment();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // We hide the action bar
	    getSupportActionBar().hide();;
	}
	
	@Override
	public void onBackPressed() {
		// Creating an intent to go to the main screen
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}
