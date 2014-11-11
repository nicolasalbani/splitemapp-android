package com.splitemapp.android.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.splitemapp.android.R;
import com.splitemapp.android.activity.LoginActivity;

@Config(emulateSdk = 18) 
@RunWith(RobolectricTestRunner.class)
public class LoginActivityTest {

	private LoginActivity activity;

	@Before
	public void setup()  {
		activity = Robolectric.buildActivity(LoginActivity.class).create().get();
	}

	@Test
	public void checkActivityNotNull() throws Exception {
		assertNotNull(activity);
	}

	@Test
	public void shouldHaveHappySmiles() throws Exception {
		String hello = new LoginActivity().getResources().getString(R.string.app_name);
		assertThat(hello, equalTo("SplitemApp"));
	}

} 