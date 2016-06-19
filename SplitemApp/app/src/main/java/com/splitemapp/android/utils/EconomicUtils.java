package com.splitemapp.android.utils;


public class EconomicUtils {
	
	public static final int MAX_SHARE = 100;

	public static Float calulateShare(int peopleAmount){
		if(peopleAmount<=0){
			peopleAmount = 1;
		}
		return Float.valueOf(MAX_SHARE/peopleAmount);
	}
}
