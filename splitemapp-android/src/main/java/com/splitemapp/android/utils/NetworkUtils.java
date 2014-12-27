package com.splitemapp.android.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.splitemapp.android.constants.Constants;

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
}
