package com.splitemapp.android.screen.createlist;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.SingleFragmentActivity;

public class CreateListActivity extends SingleFragmentActivity {
	
	private TextView mCancel;

	@Override
	protected Fragment createFragment() {
		return new CreateListFragment();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		// We set the action bar title
		getSupportActionBar().setTitle(R.string.cl_title);

		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
		getSupportActionBar().setCustomView(R.layout.action_menu_create_list);

		View actionBarView = getWindow().getDecorView();
		mCancel = (TextView) actionBarView.findViewById(R.id.cl_cancel_action);
		mCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
