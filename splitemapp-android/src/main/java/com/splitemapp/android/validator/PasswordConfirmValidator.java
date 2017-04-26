package com.splitemapp.android.validator;

import android.widget.TextView;

public abstract class PasswordConfirmValidator extends TextConfirmValidator {

	static final int MIN_LENGTH = 8;
	static final int MAX_LENGTH = 15;

	public PasswordConfirmValidator(TextView textView, TextView textViewConfirm, boolean showOkColor) {
		super(textView,textViewConfirm,showOkColor);
	}
	
	@Override
	public void validate(TextView textView, TextView textViewConfirm, String text, String textConfirm) {
		boolean isValid = false;

		if(!text.isEmpty() && text.length() >= MIN_LENGTH && text.length() <= MAX_LENGTH){
			if(text.equals(textConfirm)){
				isValid = true;
			}
		}

		if(isValid){
			showValidColor(textView);
			showValidColor(textViewConfirm);
		} else {
			showInvalidColor(textView);
			showInvalidColor(textViewConfirm);
		}
		
		onValidationAction(isValid);
	}
}
