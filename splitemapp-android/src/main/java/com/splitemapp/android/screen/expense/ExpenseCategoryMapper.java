package com.splitemapp.android.screen.expense;

import com.splitemapp.android.R;

public enum ExpenseCategoryMapper {

	CAR				(1, R.drawable.ic_category_car_48dp, R.string.e_category_car),
	TRAVEL			(2, R.drawable.ic_category_travel_48dp, R.string.e_category_travel),
	FOOD			(3, R.drawable.ic_category_food_48dp, R.string.e_category_food),
	FAMILY			(4, R.drawable.ic_category_personal_48dp, R.string.e_category_family),
	BILLS			(5, R.drawable.ic_category_bills_48dp, R.string.e_category_bills),
	ENTERTAINMENT	(6, R.drawable.ic_category_entertainment_48dp, R.string.e_category_entertainment),
	HOME			(7, R.drawable.ic_category_home_48dp, R.string.e_category_home),
	GIFTS			(8, R.drawable.ic_category_gift_48dp, R.string.e_category_gifts),
	SHOPPING		(9, R.drawable.ic_category_shopping_48dp, R.string.e_category_shopping);

	private final int expenseCategoryId;
	private final int drawableId;
	private final int titleId;

	ExpenseCategoryMapper(int expenseCategoryId, int drawableId, int titleId){
		this.expenseCategoryId = expenseCategoryId;
		this.drawableId = drawableId;
		this.titleId = titleId;
	}

	public int getDrawableId() {
		return drawableId;
	}
	
	public int getTitleId() {
		return titleId;
	}
	
	public int getExpenseCategoryId() {
		return expenseCategoryId;
	}
}
