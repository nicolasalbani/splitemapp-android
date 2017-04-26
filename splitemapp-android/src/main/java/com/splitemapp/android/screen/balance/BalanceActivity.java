package com.splitemapp.android.screen.balance;


import android.support.v4.app.Fragment;

import com.splitemapp.android.screen.SingleFragmentActivity;

public class BalanceActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new BalanceFragment();
	}
	
	@Override
	public void onBackPressed() {
		finish();
	}
}
