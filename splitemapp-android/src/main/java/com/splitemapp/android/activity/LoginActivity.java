package com.splitemapp.android.activity;


import com.splitemapp.android.fragment.LoginFragment;

import android.support.v4.app.Fragment;

public class LoginActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new LoginFragment();
	}
}
