package com.splitemapp.android.validator;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import com.splitemapp.android.R;

public abstract class TextValidator implements TextWatcher {
	private final TextView textView;
	private boolean showOkColor;
	private int okDrawableResource;
	private Context resources;

	public TextValidator(TextView textView, boolean showOkColor, Context resources) {
		this.textView = textView;
		this.showOkColor = showOkColor;
		this.resources = resources;
	}

	public TextValidator(TextView textView, boolean showOkColor, int okDrawableResource, Context resources) {
		this.textView = textView;
		this.showOkColor = showOkColor;
		this.okDrawableResource = okDrawableResource;
		this.resources = resources;
	}

	public abstract void validate(TextView textView, String text);

	public abstract void onValidationAction(boolean isValid);

	public Context getResources(){
		return this.resources;
	}

	@Override
	final public void afterTextChanged(Editable s) {
		String text = textView.getText().toString();
		validate(textView, text);
	}

	@Override
	final public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* Don't care */ }

	@Override
	final public void onTextChanged(CharSequence s, int start, int before, int count) { /* Don't care */ }

	public void showInvalidColor(TextView textView){
		textView.setBackgroundResource(R.drawable.shape_rectangle_warning);
	}

	public void showValidColor(TextView textView){
		if(showOkColor){
			if(okDrawableResource != 0){
				textView.setBackgroundResource(okDrawableResource);
			} else {
				textView.setBackgroundResource(R.drawable.shape_rectangle_ok);
			}
		} else {
			textView.setBackgroundResource(0);
		}
	}
}
