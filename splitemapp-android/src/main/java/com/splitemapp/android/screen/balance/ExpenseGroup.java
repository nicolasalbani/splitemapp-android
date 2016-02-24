package com.splitemapp.android.screen.balance;

import java.math.BigDecimal;

import android.graphics.drawable.Drawable;

import com.splitemapp.commons.domain.UserToProject;

public class ExpenseGroup {

	private Drawable drawable;
	private BigDecimal amount;
	private UserToProject userToProject;

	public ExpenseGroup() {}

	public ExpenseGroup(Drawable drawable, BigDecimal amount, UserToProject userToProject) {
		this.drawable = drawable;
		this.amount = amount;
		this.setUserToProject(userToProject);
	}
	
	public Drawable getDrawable() {
		return drawable;
	}
	public void setDrawable(Drawable drawable) {
		this.drawable = drawable;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public UserToProject getUserToProject() {
		return userToProject;
	}
	public void setUserToProject(UserToProject userToProject) {
		this.userToProject = userToProject;
	}
	
}
