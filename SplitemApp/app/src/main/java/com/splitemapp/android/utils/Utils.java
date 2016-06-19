package com.splitemapp.android.utils;

import java.util.List;

import com.splitemapp.commons.domain.User;

public class Utils {

	/**
	 * Returns a long[] containing the list of User IDs in the provided list of Users
	 * @param userList
	 * @return
	 */
	static public long[] userListToIdArray(List<User> userList){
		long[] userIdArray = new long[userList.size()];
		
		for(int i=0; i<userList.size(); i++){
			userIdArray[i] = userList.get(i).getId();
		}
		
		return userIdArray;
	}
}
