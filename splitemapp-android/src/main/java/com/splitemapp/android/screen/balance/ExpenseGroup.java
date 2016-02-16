package com.splitemapp.android.screen.balance;

import java.math.BigDecimal;

import android.graphics.drawable.Drawable;

public class ExpenseGroup {

	private Drawable drawable;
	private BigDecimal amount;

	public ExpenseGroup() {}

	public ExpenseGroup(Drawable drawable, BigDecimal amount) {
		this.drawable = drawable;
		this.amount = amount;
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
	
}
