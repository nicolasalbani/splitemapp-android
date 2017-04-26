package com.splitemapp.android.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

	/**
	 * Returns a hash String of the provided input
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
     */
	static public String stringToHash(String input) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return new String(hash);
        } catch (NoSuchAlgorithmException e) {
            // Do nothing
        }

		return input;
	}
}
