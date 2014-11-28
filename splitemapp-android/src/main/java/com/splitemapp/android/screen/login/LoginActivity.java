package com.splitemapp.android.screen.login;


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
}
