package com.splitemapp.android.fragment;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.splitemapp.android.R;
import com.splitemapp.android.domain.dto.response.LoginResponse;

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
				new LoginRequestTask().execute();
			}
			
//			@Override
//			public void onClick(View v) {
//				// We access the DAO for SimpleData 
//				Dao<UserStatus, Integer> dao;
//				try {
//					Log.i("LoginFragment","Before creating UserStatus");
//					UserStatus userStatus = new UserStatus();
//					userStatus.setCod("myCode");
//					userStatus.setTitle("myTitle");
//					DatabaseHelper helper = getHelper();
//					helper.getWritableDatabase();
//					dao = helper.getUserStatusDao();
//					dao.create(userStatus);
//					Log.i("LoginFragment","After creating UserStatus");
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
//			}
		});
		
		return v;
	}
	
	private class LoginRequestTask extends AsyncTask<Void, Void, LoginResponse> {
	    @Override
	    protected LoginResponse doInBackground(Void... params) {
	        try {
	            final String url = "http://192.168.0.100:8080/splitemapp-service-backend-rest/login";
	            RestTemplate restTemplate = new RestTemplate();
	            MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
	            mappingJackson2HttpMessageConverter.getObjectMapper().configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
	            mappingJackson2HttpMessageConverter.getObjectMapper().configure(SerializationFeature.WRAP_ROOT_VALUE, true);
	            restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);
	            LoginResponse loginResponse = restTemplate.getForObject(url, LoginResponse.class);
	            return loginResponse;
	        } catch (Exception e) {
	            Log.e("MainActivity", e.getMessage(), e);
	        }

	        return null;
	    }

	    @Override
	    protected void onPostExecute(LoginResponse loginResponse) {
	        TextView greetingIdText = (TextView) getActivity().findViewById(R.id.user_name);
	        TextView greetingContentText = (TextView) getActivity().findViewById(R.id.session_token);
	        greetingIdText.setText(loginResponse.getFirstName() +" "+ loginResponse.getLastName());
	        greetingContentText.setText(loginResponse.getSessionToken());
	    }

	}
}
