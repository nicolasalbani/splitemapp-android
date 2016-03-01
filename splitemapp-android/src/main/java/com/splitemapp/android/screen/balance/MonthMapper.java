package com.splitemapp.android.screen.balance;

import com.splitemapp.android.R;

public enum MonthMapper {

	JANUARY			(0, R.string.b_month_january, R.string.b_short_month_january),
	FEBRUARY		(1, R.string.b_month_february, R.string.b_short_month_february),
	MARCH			(2, R.string.b_month_march, R.string.b_short_month_march),
	APRIL			(3, R.string.b_month_april, R.string.b_short_month_april),
	MAY				(4, R.string.b_month_may, R.string.b_short_month_may),
	JUNE			(5, R.string.b_month_june, R.string.b_short_month_june),
	JULY			(6, R.string.b_month_july, R.string.b_short_month_july),
	AUGUST			(7, R.string.b_month_august, R.string.b_short_month_august),
	SEPTEMBER		(8, R.string.b_month_september, R.string.b_short_month_september),
	OCTOBER			(9, R.string.b_month_october, R.string.b_short_month_october),
	NOVEMBER		(10, R.string.b_month_november, R.string.b_short_month_november),
	DECEMBER		(11, R.string.b_month_december, R.string.b_short_month_december);

	private final int monthId;
	private final int stringId;
	private final int shortStringId;

	MonthMapper(int expenseCategoryId, int stringId, int shortStringId){
		this.monthId = expenseCategoryId;
		this.stringId = stringId;
		this.shortStringId = shortStringId;
	}

	public int getStringId() {
		return stringId;
	}
	
	public int getMonthId() {
		return monthId;
	}

	public int getShortStringId() {
		return shortStringId;
	}
}
