package com.splitemapp.android.screen.createlist;


import android.support.v4.app.Fragment;

import com.splitemapp.android.screen.SingleFragmentActivity;

public class CreateListActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new CreateListFragment();
	}
}
