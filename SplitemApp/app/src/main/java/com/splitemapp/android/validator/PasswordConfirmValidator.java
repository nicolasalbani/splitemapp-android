package com.splitemapp.android.validator;

import android.content.Context;
import android.widget.TextView;

import com.splitemapp.android.R;

public abstract class PasswordConfirmValidator extends TextConfirmValidator {

	static final int MIN_LENGTH = 8;
	static final int MAX_LENGTH = 15;

	public PasswordConfirmValidator(TextView textView, TextView textViewConfirm, boolean showOkColor,Context resources) {
		super(textView,textViewConfirm,showOkColor,resources);
	}
	
	@Override
	public void validate(TextView textView, TextView textViewConfirm, String text, String textConfirm) {
		boolean isValid = false;

		if(!text.isEmpty() && text.length() >= MIN_LENGTH && text.length() <= MAX_LENGTH){
			if(text.equals(textConfirm)){
				isValid = true;
			}
		}

		if(!isValid){
			textView.setError(getResources().getString(R.string.val_password_confirm));
		}
		
		onValidationAction(isValid);
	}
}
