package com.splitemapp.android.screen.balance;

import java.math.BigDecimal;

import android.graphics.drawable.Drawable;

public class ExpenseGroupCategory extends ExpenseGroup{

	private boolean showPrimary;
	
	public ExpenseGroupCategory() {
		super();
	}

	public ExpenseGroupCategory(Drawable drawable, BigDecimal amount) {
		super(drawable, amount);
	}

	public boolean isShowPrimary() {
		return showPrimary;
	}

	public void setShowPrimary(boolean showPrimary) {
		this.showPrimary = showPrimary;
	}
}
