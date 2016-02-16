package com.splitemapp.android.screen.balance;

import com.splitemapp.android.R;

public enum MonthMapper {

	JANUARY			(0, R.string.b_month_january),
	FEBRUARY		(1, R.string.b_month_february),
	MARCH			(2, R.string.b_month_march),
	APRIL			(3, R.string.b_month_april),
	MAY				(4, R.string.b_month_may),
	JUNE			(5, R.string.b_month_june),
	JULY			(6, R.string.b_month_july),
	AUGUST			(7, R.string.b_month_august),
	SEPTEMBER		(8, R.string.b_month_september),
	OCTOBER			(9, R.string.b_month_october),
	NOVEMBER		(10, R.string.b_month_november),
	DECEMBER		(11, R.string.b_month_december);

	private final int monthId;
	private final int stringId;

	MonthMapper(int expenseCategoryId, int titleId){
		this.monthId = expenseCategoryId;
		this.stringId = titleId;
	}

	public int getStringId() {
		return stringId;
	}
	
	public int getMonthId() {
		return monthId;
	}
}
