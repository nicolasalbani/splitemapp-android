package com.splitemapp.android.validator;

import com.splitemapp.commons.utils.ValidatorUtils;

import android.widget.TextView;

public abstract class EmailValidator extends TextValidator {

	public EmailValidator(TextView textView,boolean showOkColor) {
		super(textView,showOkColor);
	}
	
	public EmailValidator(TextView textView,boolean showOkColor, int okDrawableResource) {
		super(textView,showOkColor,okDrawableResource);
	}

	@Override
	public void validate(TextView textView, String text) {
		boolean isValid = false;

		if(text != null && !text.isEmpty()){
			isValid = ValidatorUtils.isValidEmail(text);
		}

		if(isValid){
			showValidColor(textView);
		} else {
			showInvalidColor(textView);
		}
		
		onValidationAction(isValid);
	}

}
