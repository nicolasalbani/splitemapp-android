package com.splitemapp.android.screen.managecontacts;


import android.support.v4.app.Fragment;

import com.splitemapp.android.screen.SingleFragmentActivity;

public class ManageContactsActivity extends SingleFragmentActivity {
	
	@Override
	protected Fragment createFragment() {
		return new ManageContactsFragment();
	}

}
