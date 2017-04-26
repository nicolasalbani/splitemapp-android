package com.splitemapp.android.screen.expense;

import java.text.DecimalFormat;

public class ExpenseAmountFormat extends DecimalFormat {

	private static final long serialVersionUID = 4278695792059052374L;

	// Decimal number entry
	public static final int MAX_DIGITS_BEFORE_DECIMAL = 5;
	public static final int MAX_DIGITS_AFTER_DECIMAL = 2;

	public ExpenseAmountFormat(){
		super();
		setGroupingUsed(false);
		setDecimalSeparatorAlwaysShown(true);
		setMaximumFractionDigits(MAX_DIGITS_AFTER_DECIMAL);
		setMinimumFractionDigits(MAX_DIGITS_AFTER_DECIMAL);
	}
}
