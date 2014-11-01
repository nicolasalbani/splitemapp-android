package com.splitemapp.android;


import com.fairpay.android.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;


public abstract class SingleFragmentActivity extends FragmentActivity {

	protected abstract Fragment createFragment(); 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Setting the content view to be the layout for our crime activity
		setContentView(R.layout.activity_fragment);
		
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
