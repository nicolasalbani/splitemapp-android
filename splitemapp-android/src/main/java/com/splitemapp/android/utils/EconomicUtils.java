package com.splitemapp.android.utils;

import java.math.BigDecimal;

public class EconomicUtils {
	
	public static final int MAX_SHARE = 100;

	public static BigDecimal calulateShare(int peopleAmount){
		return new BigDecimal(MAX_SHARE/peopleAmount);
	}
}
