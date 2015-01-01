package com.splitemapp.android.screen.home;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.splitemapp.android.screen.SingleFragmentActivity;

public class HomeActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new HomeFragment();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // We hide the action bar
	    getSupportActionBar().hide();
	}
}
