package com.splitemapp.android.screen.addpeople;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.SingleFragmentActivity;

public class AddPeopleActivity extends SingleFragmentActivity {
	
	private TextView mBack;

	@Override
	protected Fragment createFragment() {
		return new AddPeopleFragment();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		// We set the action bar title
		getSupportActionBar().setTitle(R.string.ap_title);

		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
		getSupportActionBar().setCustomView(R.layout.action_menu_add_people);

		View actionBarView = getWindow().getDecorView();
		mBack = (TextView) actionBarView.findViewById(R.id.ap_back_action);
		mBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
