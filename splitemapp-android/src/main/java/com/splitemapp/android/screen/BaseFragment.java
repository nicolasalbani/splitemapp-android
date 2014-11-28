package com.splitemapp.android.screen;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.splitemapp.android.constants.Constants;
import com.splitemapp.android.dao.DatabaseHelper;

public abstract class BaseFragment extends Fragment {

	protected DatabaseHelper databaseHelper = null;

	static{
		OpenHelperManager.setOpenHelperClass(DatabaseHelper.class);
	}

	/**
	 * This method is called when the fragment is destroyed, releasing the database helper object
	 */
	public void onDestroy() {
		super.onDestroy();
		if (databaseHelper != null) {
			OpenHelperManager.releaseHelper();
			databaseHelper = null;
		}
	}

	/**
	 * This method calls the OpenHelperManager getHelper static method with the proper DatabaseHelper class reference 
	 * @return DatabaseHelper object which offers DAO for every domain entity
	 */
	protected DatabaseHelper getHelper() {
		if (databaseHelper == null) {
			databaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
		}
		return databaseHelper;
	}

	/**
	 * 
	 * @param servicePath String containing the rest service name
	 * @param request <E> The request object used in the rest service call
	 * @param responseType <T> The response class that the rest service call is supposed to return
	 * @return	<T> An instance of the response type specified as a parameter
	 */
	protected <E,T> T callRestService(String servicePath, E request, Class<T> responseType){
		// We create the url based on the provider serviceName
		String url = "http://"+Constants.BACKEND_HOST+":"+Constants.BACKEND_PORT+"/"+Constants.BACKEND_PATH+servicePath;

		// We get an instance of the spring framework RestTemplate and configure wrapping the root XML element
		RestTemplate restTemplate = new RestTemplate();
		MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		mappingJackson2HttpMessageConverter.getObjectMapper().configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		mappingJackson2HttpMessageConverter.getObjectMapper().configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);

		// We use old version of request factory that uses HTTPClient instead of HttpURLConnection to avoid bugs
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());  

		// We make the POST rest service call
		T response = restTemplate.postForObject(url, request, responseType);
		return response;
	}

	protected void showToast(String message){
		Toast toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
		toast.show();
	}

	protected String getIpAddress() throws SocketException {
		Enumeration<NetworkInterface> networkInterfaceList = NetworkInterface.getNetworkInterfaces();

		while(networkInterfaceList.hasMoreElements()){
			Enumeration<InetAddress> inetAddresses = networkInterfaceList.nextElement().getInetAddresses();
			while(inetAddresses.hasMoreElements()){
				InetAddress inetAddress = inetAddresses.nextElement();
				if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress.isSiteLocalAddress()) {
					return inetAddress.getHostAddress();
				}
			}
		}

		return Constants.LOOPBACK_ADDRESS;
	}

}
