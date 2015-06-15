package com.splitemapp.android.screen.addpeople;


import android.support.v4.app.Fragment;

import com.splitemapp.android.screen.SingleFragmentActivity;

public class AddPeopleActivity extends SingleFragmentActivity {
	
	@Override
	protected Fragment createFragment() {
		return new AddPeopleFragment();
	}

}
