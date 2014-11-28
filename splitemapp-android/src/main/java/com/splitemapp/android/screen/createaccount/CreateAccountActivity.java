package com.splitemapp.android.screen.createaccount;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.SingleFragmentActivity;

public class CreateAccountActivity extends SingleFragmentActivity {

	private TextView mCancel;

	@Override
	protected Fragment createFragment() {
		return new CreateAccountFragment();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We set the action bar title
		getSupportActionBar().setTitle(R.string.ca_create_account_title);

		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
		getSupportActionBar().setCustomView(R.layout.action_menu_create_account);

		View actionBarView = getWindow().getDecorView();
		mCancel = (TextView) actionBarView.findViewById(R.id.ca_cancel_action);
		mCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

}
