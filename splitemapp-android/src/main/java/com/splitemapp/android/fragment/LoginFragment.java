package com.splitemapp.android.fragment;

import java.sql.SQLException;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.j256.ormlite.dao.Dao;
import com.splitemapp.android.R;
import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.domain.UserStatus;

public class LoginFragment extends BaseFragment {

	private Button mAddSimpleData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_login, container, false);

		mAddSimpleData = (Button) v.findViewById(R.id.add_button);
		mAddSimpleData.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// We access the DAO for SimpleData 
				Dao<UserStatus, Integer> dao;
				try {
					Log.i("LoginFragment","Before creating UserStatus");
					UserStatus userStatus = new UserStatus();
					userStatus.setCod("myCode");
					userStatus.setTitle("myTitle");
					DatabaseHelper helper = getHelper();
					helper.getWritableDatabase();
					dao = helper.getUserStatusDao();
					dao.create(userStatus);
					Log.i("LoginFragment","After creating UserStatus");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
		
		return v;
	}
}
