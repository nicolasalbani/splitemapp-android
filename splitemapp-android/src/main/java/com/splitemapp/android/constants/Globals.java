package com.splitemapp.android.constants;

import java.util.List;

import com.splitemapp.commons.domain.User;

public class Globals {
	private static List<User> createListActivityUserList;
	private static Long expenseActivityExpenseId;
	private static Long expenseActivityProjectId;

	public static List<User> getCreateListActivityUserList() {
		return createListActivityUserList;
	}

	public static void setCreateListActivityUserList(List<User> createListUserList) {
		Globals.createListActivityUserList = createListUserList;
	}

	public static Long getExpenseActivityProjectId() {
		return expenseActivityProjectId;
	}

	public static void setExpenseActivityProjectId(
			Long expenseActivityProjectId) {
		Globals.expenseActivityProjectId = expenseActivityProjectId;
	}

	public static Long getExpenseActivityExpenseId() {
		return expenseActivityExpenseId;
	}

	public static void setExpenseActivityExpenseId(
			Long expenseActivityExpenseId) {
		Globals.expenseActivityExpenseId = expenseActivityExpenseId;
	}
}
