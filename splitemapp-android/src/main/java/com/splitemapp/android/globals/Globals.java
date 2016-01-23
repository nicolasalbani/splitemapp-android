package com.splitemapp.android.globals;

import java.util.List;

import com.splitemapp.android.screen.createproject.CreateProjectFragment;
import com.splitemapp.commons.domain.User;

public class Globals {
	private static List<User> createProjectActivityUserList;
	private static Long expenseActivityExpenseId;
	private static Long expenseActivityProjectId;
	private static Long createProjectActivityProjectId;
	private static CreateProjectFragment createProjectFragment;
	private static Boolean isConnectedToServer;

	public static List<User> getCreateProjectActivityUserList() {
		return createProjectActivityUserList;
	}

	public static void setCreateProjectActivityUserList(List<User> createProjectUserList) {
		Globals.createProjectActivityUserList = createProjectUserList;
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

	public static CreateProjectFragment getCreateProjectFragment() {
		return createProjectFragment;
	}

	public static void setCreateProjectFragment(CreateProjectFragment createProjectFragment) {
		Globals.createProjectFragment = createProjectFragment;
	}

	public static Long getCreateProjectActivityProjectId() {
		return createProjectActivityProjectId;
	}

	public static void setCreateProjectActivityProjectId(
			Long createProjectActivityProjectId) {
		Globals.createProjectActivityProjectId = createProjectActivityProjectId;
	}

	public static Boolean getIsConnectedToServer() {
		return isConnectedToServer;
	}

	public static void setIsConnectedToServer(Boolean isConnectedToServer) {
		Globals.isConnectedToServer = isConnectedToServer;
	}
}
