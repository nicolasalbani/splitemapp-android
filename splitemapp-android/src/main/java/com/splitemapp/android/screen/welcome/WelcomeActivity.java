package com.splitemapp.android.screen.welcome;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.createaccount.CreateAccountFragment;
import com.splitemapp.android.screen.login.LoginFragment;

public class WelcomeActivity extends FragmentActivity {

	private static final int NUMBER_OF_PAGES = 2;
	private static final int LOGIN_PAGE = 0;
	private static final int CREATE_ACCOUNT_PAGE = 1;

	private CustomPagerAdapter mAdapter;
	private ViewPager mPager;
	private TabLayout mTabLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_welcome);

		// Setting the pager adapter
		mAdapter = new CustomPagerAdapter(getSupportFragmentManager());

		mPager = (ViewPager)findViewById(R.id.w_viewPager);
		mPager.setAdapter(mAdapter);
		
		mTabLayout = (TabLayout)findViewById(R.id.w_tabLayout);
		mTabLayout.setTabsFromPagerAdapter(mAdapter);
		mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabUnselected(Tab tab) {}
			@Override
			public void onTabSelected(Tab tab) {
				mPager.setCurrentItem(tab.getPosition());
			}
			@Override
			public void onTabReselected(Tab tab) {}
		});
		
		mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
	}

	private class CustomPagerAdapter extends FragmentPagerAdapter {

		public CustomPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return NUMBER_OF_PAGES;
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

		@Override
		public CharSequence getPageTitle(int position) {
			// We define which fragment to return, based on the position
			switch (position){
			case LOGIN_PAGE:
				return getString(R.string.w_login);
			case CREATE_ACCOUNT_PAGE: 
				return getString(R.string.w_create_account);
			}
			
			return null;
		}
	}
}
