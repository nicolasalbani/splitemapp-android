package com.splitemapp.android.screen.projectcontacts;


import android.support.v4.app.Fragment;

import com.splitemapp.android.screen.SingleFragmentActivity;

public class ProjectContactsActivity extends SingleFragmentActivity {
	
	@Override
	protected Fragment createFragment() {
		return new ProjectContactsFragment();
	}

}
