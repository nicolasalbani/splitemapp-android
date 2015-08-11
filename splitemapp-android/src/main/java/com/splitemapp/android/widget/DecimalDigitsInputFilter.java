package com.splitemapp.android.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;

public class DecimalDigitsInputFilter implements InputFilter {

	private Pattern mPattern;

	public DecimalDigitsInputFilter(int digitsBeforeDecimal,int digitsAfterDecimal) {
		mPattern=Pattern.compile("[0-9]{0," + digitsBeforeDecimal + "}+((\\.[0-9]{0," + digitsAfterDecimal + "})?)||(\\.)?");
	}

	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
		CharSequence match = TextUtils.concat(dest.subSequence(0, dstart), source.subSequence(start, end), dest.subSequence(dend, dest.length()));
		Matcher matcher=mPattern.matcher(match);       
		if(!matcher.matches())
			return "";
		return null;
	}

}