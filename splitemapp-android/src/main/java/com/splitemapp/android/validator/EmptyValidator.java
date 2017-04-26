package com.splitemapp.android.validator;

import android.widget.TextView;

public abstract class EmptyValidator extends TextValidator {

	public EmptyValidator(TextView textView, boolean showOkColor) {
		super(textView,showOkColor);
	}
	
	public EmptyValidator(TextView textView, boolean showOkColor, int okDrawableResource) {
		super(textView,showOkColor,okDrawableResource);
	}

	@Override
	public void validate(TextView textView, String text) {
		boolean isValid = false;

		if(text != null && !text.isEmpty()){
			isValid = true;
		}

		if(isValid){
			showValidColor(textView);
		} else {
			showInvalidColor(textView);
		}
		
		onValidationAction(isValid);
	}

}
