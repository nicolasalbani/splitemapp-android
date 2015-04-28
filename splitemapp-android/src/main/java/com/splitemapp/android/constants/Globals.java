package com.splitemapp.android.constants;

import java.util.List;

import com.splitemapp.commons.domain.User;

public class Globals {
	private static List<User> createListUserList;

	public static List<User> getCreateListUserList() {
		return createListUserList;
	}

	public static void setCreateListUserList(List<User> createListUserList) {
		Globals.createListUserList = createListUserList;
	}
}
