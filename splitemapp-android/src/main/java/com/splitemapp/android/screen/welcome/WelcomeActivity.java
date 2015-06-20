package com.splitemapp.android.screen.welcome;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.createaccount.CreateAccountFragment;
import com.splitemapp.android.screen.login.LoginFragment;

public class WelcomeActivity extends FragmentActivity {

	private static final int NUMBER_OF_PAGES = 2;
	private static final int LOGIN_PAGE = 0;
	private static final int CREATE_ACCOUNT_PAGE = 1;

	private CustomPagerAdapter mAdapter;
	private ViewPager mPager;
	private TextView login;
	private TextView createAccount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_welcome);

		// Watch for TextView clicks.
		login = (TextView)findViewById(R.id.w_login_textView);
		login.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mPager.setCurrentItem(LOGIN_PAGE);
			}
		});
		createAccount = (TextView)findViewById(R.id.w_create_account_textView);
		createAccount.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mPager.setCurrentItem(CREATE_ACCOUNT_PAGE);
			}
		});

		// Setting the pager adapter
		mAdapter = new CustomPagerAdapter(getSupportFragmentManager());

		mAdapter.setLogin(login);
		mAdapter.setCreateAccount(createAccount);

		mPager = (ViewPager)findViewById(R.id.w_viewPager);
		mPager.setAdapter(mAdapter);
	}

	private class CustomPagerAdapter extends FragmentPagerAdapter {
		private TextView login;
		private TextView createAccount;

		public CustomPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return NUMBER_OF_PAGES;
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position, Object object) {
			// Updating text views
			updateTextViews(position);

			// Updating the primary item
			super.setPrimaryItem(container, position, object);
		}

		@Override
		public Fragment getItem(int position) {
			// We define which fragment to return, based on the position
			switch (position){
			case LOGIN_PAGE:
				return new LoginFragment();
			case CREATE_ACCOUNT_PAGE: 
				return new CreateAccountFragment();
			}

			return null;
		}

		public void setLogin(TextView login) {
			this.login = login;
		}

		public void setCreateAccount(TextView createAccount) {
			this.createAccount = createAccount;
		}

		private void updateTextViews(int page){
			switch (page){
			case LOGIN_PAGE:
				login.setTextColor(getResources().getColor(R.color.white));
				createAccount.setTextColor(getResources().getColor(R.color.strongblue));
				break;
			case CREATE_ACCOUNT_PAGE:
				login.setTextColor(getResources().getColor(R.color.strongblue));
				createAccount.setTextColor(getResources().getColor(R.color.white));
				break;
			}
		}
	}

}
