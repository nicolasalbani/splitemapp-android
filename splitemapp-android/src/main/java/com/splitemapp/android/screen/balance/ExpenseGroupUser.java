package com.splitemapp.android.screen.balance;

import java.math.BigDecimal;

import android.graphics.drawable.Drawable;

import com.splitemapp.commons.domain.UserToProject;

public class ExpenseGroupUser extends ExpenseGroup{

	private boolean showPrimary;
	private UserToProject userToProject;

	public ExpenseGroupUser() {
		super();
	}

	public ExpenseGroupUser(Drawable drawable, BigDecimal amount, UserToProject userToProject, boolean showPrimary) {
		super(drawable, amount);
		this.userToProject = userToProject;
	}

	public UserToProject getUserToProject() {
		return userToProject;
	}
	public void setUserToProject(UserToProject userToProject) {
		this.userToProject = userToProject;
	}

	public boolean isShowPrimary() {
		return showPrimary;
	}

	public void setShowPrimary(boolean showPrimary) {
		this.showPrimary = showPrimary;
	}
}
