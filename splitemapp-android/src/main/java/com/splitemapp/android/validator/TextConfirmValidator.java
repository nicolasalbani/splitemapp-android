package com.splitemapp.android.validator;

import android.widget.TextView;

public abstract class TextConfirmValidator extends TextValidator {
	
	private final TextView textViewConfirm;

	public TextConfirmValidator(TextView textView, TextView textViewConfirm, boolean showOkColor) {
		super(textView, showOkColor);
		this.textViewConfirm = textViewConfirm;
	}
	
	public TextConfirmValidator(TextView textView, TextView textViewConfirm, boolean showOkColor, int okDrawableResource) {
		super(textView, showOkColor, okDrawableResource);
		this.textViewConfirm = textViewConfirm;
	}

	@Override
	public final void validate(TextView textView, String text) {
		validate(textView, textViewConfirm, text, textViewConfirm.getText().toString());
	}
	
	public abstract void validate(TextView textView, TextView textViewConfirm, String text, String textConfirm);

}
