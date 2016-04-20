package com.splitemapp.android.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

import com.splitemapp.android.constants.Constants;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.rest.RestUtils;

public class NetworkUtils {
	
	public static final int TIMEOUT = 5000;
	
	public static String getIpAddress() throws SocketException {
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
	
	/**
	 * 
	 * @param servicePath String containing the rest service name
	 * @param request <E> The request object used in the rest service call
	 * @param responseType <T> The response class that the rest service call is supposed to return
	 * @return	<T> An instance of the response type specified as a parameter
	 */
	public static <E,T> T callRestService(String servicePath, E request, Class<T> responseType){
		// We create the url based on the provider serviceName
		String serviceUrl = "http://"+ServiceConstants.BACKEND_HOST+":"+ServiceConstants.BACKEND_PORT+"/"+ServiceConstants.BACKEND_PATH+servicePath;

		return RestUtils.callRestService(serviceUrl, request, responseType);
	}
	
	/**
	 * Checks for connection to server
	 * @param url
	 * @param timeout
	 * @return
	 */
	public static boolean isConnectedToServer() {
		try{
			URL myUrl = new URL("http://"+ServiceConstants.BACKEND_HOST+":"+ServiceConstants.BACKEND_PORT);
			URLConnection connection = myUrl.openConnection();
			connection.setConnectTimeout(TIMEOUT);
			connection.connect();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
