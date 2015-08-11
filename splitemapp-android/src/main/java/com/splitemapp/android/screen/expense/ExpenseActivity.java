package com.splitemapp.android.screen.expense;


import android.support.v4.app.Fragment;

import com.splitemapp.android.constants.Globals;
import com.splitemapp.android.screen.SingleFragmentActivity;

public class ExpenseActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new ExpenseFragment();
	}
	
	@Override
	public void onBackPressed() {
		// Cleaning up global expense id
		Globals.setExpenseActivityExpenseId(null);

		// Moving back to the project screen
		finish();
	}
}
