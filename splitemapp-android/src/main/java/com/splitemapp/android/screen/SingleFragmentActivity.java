package com.splitemapp.android.screen;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import com.splitemapp.android.R;


public abstract class SingleFragmentActivity extends ActionBarActivity {

	protected abstract Fragment createFragment(); 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		
		//Setting the content view to be the layout for our crime activity
		setContentView(R.layout.activity_fragment);
		
		getSupportActionBar().setTitle(R.string.ca_create_account_button);
		
		//Getting the fragment manager
		FragmentManager fm = getSupportFragmentManager();
		
		//Setting the fragment to the fragment manager (if it's not already there)
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		if(fragment == null){
			fragment = createFragment();
			fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
		}
	}
}
