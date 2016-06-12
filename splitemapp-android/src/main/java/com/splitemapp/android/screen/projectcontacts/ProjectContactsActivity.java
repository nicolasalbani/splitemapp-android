package com.splitemapp.android.screen.projectcontacts;


import android.content.Intent;
import android.support.v4.app.Fragment;

import com.splitemapp.android.screen.SingleFragmentActivity;
import com.splitemapp.android.service.BaseTask;

public class ProjectContactsActivity extends SingleFragmentActivity {
	
	private ProjectContactsFragment projectContactsFragment;
	
	@Override
	protected Fragment createFragment() {
		projectContactsFragment = new ProjectContactsFragment();
		return projectContactsFragment;
	}
	
	@Override
	public void onBackPressed() {
		// We create a intent containing the UserID array
		Intent data = new Intent();
		data.putExtra(BaseTask.USER_ID_ARRAY_EXTRA, projectContactsFragment.getUserIdArray());
		if (getParent() == null) {
		    setResult(RESULT_OK, data);
		} else {
		    getParent().setResult(RESULT_OK, data);
		}
		finish();
	}

}
