package com.splitemapp.android.screen.expense;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.SingleFragmentActivity;

public class ExpenseActivity extends SingleFragmentActivity {
	
	private Fragment mAddExpenseFragment;
	private TextView mCancel;
	private TextView mAdd;

	@Override
	protected Fragment createFragment() {
		mAddExpenseFragment = new ExpenseFragment();
		return mAddExpenseFragment;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We set the action bar title
		getSupportActionBar().setTitle(R.string.e_title);

		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
		getSupportActionBar().setCustomView(R.layout.action_menu_expense);

		View actionBarView = getWindow().getDecorView();
		mCancel = (TextView) actionBarView.findViewById(R.id.ae_cancel_action);
		mCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mAdd = (TextView) actionBarView.findViewById(R.id.ae_save_action);
		mAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((ExpenseFragment)mAddExpenseFragment).saveExpense();
				finish();
			}
		});
	}
}
