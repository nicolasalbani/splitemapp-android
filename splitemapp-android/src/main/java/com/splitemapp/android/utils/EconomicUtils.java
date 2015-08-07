package com.splitemapp.android.utils;

import java.math.BigDecimal;

public class EconomicUtils {
	
	public static final int MAX_SHARE = 100;

	public static BigDecimal calulateShare(int peopleAmount){
		if(peopleAmount<=0){
			peopleAmount = 1;
		}
		return new BigDecimal(MAX_SHARE/peopleAmount);
	}
}
