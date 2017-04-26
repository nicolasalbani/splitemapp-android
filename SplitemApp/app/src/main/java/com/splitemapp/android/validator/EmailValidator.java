package com.splitemapp.android.validator;

import com.splitemapp.android.R;
import com.splitemapp.commons.utils.ValidatorUtils;

import android.content.Context;
import android.widget.TextView;

public abstract class EmailValidator extends TextValidator {

	public EmailValidator(TextView textView,boolean showOkColor,Context resources) {
		super(textView,showOkColor,resources);
	}
	
	public EmailValidator(TextView textView,boolean showOkColor, int okDrawableResource,Context resources) {
		super(textView,showOkColor,okDrawableResource, resources);
	}

	@Override
	public void validate(TextView textView, String text) {
		boolean isValid = false;

		if(text != null && !text.isEmpty()){
			isValid = ValidatorUtils.isValidEmail(text);
		}

		if(!isValid){
			textView.setError(getResources().getString(R.string.val_email));
		}
		
		onValidationAction(isValid);
	}

}
