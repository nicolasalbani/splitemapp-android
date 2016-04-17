package com.splitemapp.android.validator;

import android.widget.TextView;

public abstract class PasswordValidator extends TextValidator {

	static final int MIN_LENGTH = 8;
	static final int MAX_LENGTH = 15;

	public PasswordValidator(TextView textView, boolean showOkColor) {
		super(textView,showOkColor);
	}

	@Override
	public void validate(TextView textView, String text) {
		boolean isValid = false;

		if(!text.isEmpty() && text.length() >= MIN_LENGTH && text.length() <= MAX_LENGTH){
			isValid = true;
		}

		if(isValid){
			showValidColor();
		} else {
			showInvalidColor();
		}
		
		onValidationAction(isValid);
	}

}
