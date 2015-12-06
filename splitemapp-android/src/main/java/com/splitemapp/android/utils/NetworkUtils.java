package com.splitemapp.android.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.splitemapp.android.constants.Constants;
import com.splitemapp.commons.rest.RestUtils;

public class NetworkUtils {
	
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
		String serviceUrl = "http://"+Constants.BACKEND_HOST+":"+Constants.BACKEND_PORT+"/"+Constants.BACKEND_PATH+servicePath;

		return RestUtils.callRestService(serviceUrl, request, responseType);
	}
}
