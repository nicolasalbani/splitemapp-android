package com.splitemapp.android.validator;

import android.widget.TextView;

public abstract class EmptyValidator extends TextValidator {

	public EmptyValidator(TextView textView) {
		super(textView);
	}

	@Override
	public void validate(TextView textView, String text) {
		boolean isValid = false;

		if(text != null && !text.isEmpty()){
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
