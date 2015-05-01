package com.splitemapp.android.screen.managecontacts;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.SingleFragmentActivity;

public class ManageContactsActivity extends SingleFragmentActivity {
	
	private TextView mBack;

	@Override
	protected Fragment createFragment() {
		return new ManageContactsFragment();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		// We set the action bar title
		getSupportActionBar().setTitle(R.string.mc_title);

		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
		getSupportActionBar().setCustomView(R.layout.action_menu_manage_contacts);

		View actionBarView = getWindow().getDecorView();
		mBack = (TextView) actionBarView.findViewById(R.id.mc_back_action);
		mBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
