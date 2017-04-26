package com.splitemapp.android.validator;

import android.content.Context;
import android.widget.TextView;

import com.splitemapp.android.R;

public abstract class EmptyValidator extends TextValidator {

	public EmptyValidator(TextView textView, boolean showOkColor, Context resources) {
		super(textView,showOkColor,resources);
	}
	
	public EmptyValidator(TextView textView, boolean showOkColor, int okDrawableResource,Context resources) {
		super(textView,showOkColor,okDrawableResource,resources);
	}

	@Override
	public void validate(TextView textView, String text) {
		boolean isValid = false;

		if(text != null && !text.isEmpty()){
			isValid = true;
		}

		if(!isValid){
			textView.setError(getResources().getString(R.string.val_empty));
		}

		onValidationAction(isValid);
	}

}
