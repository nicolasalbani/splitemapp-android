package com.splitemapp.android.validator;

import android.content.Context;
import android.widget.TextView;

import com.splitemapp.android.R;

public abstract class PasswordValidator extends TextValidator {

	static final int MIN_LENGTH = 8;
	static final int MAX_LENGTH = 15;

	public PasswordValidator(TextView textView, boolean showOkColor, Context resources) {
		super(textView,showOkColor,resources);
	}

	@Override
	public void validate(TextView textView, String text) {
		boolean isValid = false;

		if(!text.isEmpty() && text.length() >= MIN_LENGTH && text.length() <= MAX_LENGTH){
			isValid = true;
		}

		if(!isValid){
			textView.setError(getResources().getString(R.string.val_password));
		}
		
		onValidationAction(isValid);
	}

}
