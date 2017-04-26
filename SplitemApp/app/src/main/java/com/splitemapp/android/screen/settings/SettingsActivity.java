package com.splitemapp.android.screen.settings;


import android.support.v4.app.Fragment;

import com.splitemapp.android.screen.SingleFragmentActivity;

public class SettingsActivity extends SingleFragmentActivity {
	
	@Override
	protected Fragment createFragment() {
		return new SettingsFragment();
	}

}
