package com.splitemapp.android.validator;

import com.splitemapp.commons.utils.ValidatorUtils;

import android.widget.TextView;

public abstract class EmailValidator extends TextValidator {

	public EmailValidator(TextView textView) {
		super(textView);
	}

	@Override
	public void validate(TextView textView, String text) {
		boolean isValid = false;

		if(text != null && !text.isEmpty()){
			isValid = ValidatorUtils.isValidEmail(text);
		}

		if(isValid){
			showValidColor();
		} else {
			showInvalidColor();
		}
		
		onValidationAction(isValid);
	}

}
