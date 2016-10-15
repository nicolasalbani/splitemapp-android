package com.splitemapp.android.validator;

import android.content.Context;
import android.widget.TextView;

public abstract class TextConfirmValidator extends TextValidator {
	
	private final TextView textViewConfirm;

	public TextConfirmValidator(TextView textView, TextView textViewConfirm, boolean showOkColor, Context resources) {
		super(textView, showOkColor, resources);
		this.textViewConfirm = textViewConfirm;
	}
	
	public TextConfirmValidator(TextView textView, TextView textViewConfirm, boolean showOkColor, int okDrawableResource, Context resources) {
		super(textView, showOkColor, okDrawableResource, resources);
		this.textViewConfirm = textViewConfirm;
	}

	@Override
	public final void validate(TextView textView, String text) {
		validate(textView, textViewConfirm, text, textViewConfirm.getText().toString());
	}
	
	public abstract void validate(TextView textView, TextView textViewConfirm, String text, String textConfirm);

}
