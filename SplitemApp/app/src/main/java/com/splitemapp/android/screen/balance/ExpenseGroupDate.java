package com.splitemapp.android.screen.balance;

import java.math.BigDecimal;
import java.util.Calendar;

import android.graphics.drawable.Drawable;

public class ExpenseGroupDate extends ExpenseGroup{

	Calendar monthYear;
	
	public ExpenseGroupDate() {
		super();
	}

	public ExpenseGroupDate(Drawable drawable, BigDecimal amount, Calendar monthYear) {
		super(drawable, amount);
		this.monthYear = monthYear;
	}

	public Calendar getMonthYear() {
		return monthYear;
	}

	public void setMonthYear(Calendar monthYear) {
		this.monthYear = monthYear;
	}

}
