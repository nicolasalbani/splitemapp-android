package com.splitemapp.android.validator;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import com.splitemapp.android.R;

public abstract class TextValidator implements TextWatcher {
	private final TextView textView;
	private boolean showOkColor;
	private int okDrawableResource;

	public TextValidator(TextView textView, boolean showOkColor) {
		this.textView = textView;
		this.showOkColor = showOkColor;
	}

	public TextValidator(TextView textView, boolean showOkColor, int okDrawableResource) {
		this.textView = textView;
		this.showOkColor = showOkColor;
		this.okDrawableResource = okDrawableResource;
	}

	public abstract void validate(TextView textView, String text);

	public abstract void onValidationAction(boolean isValid);

	@Override
	final public void afterTextChanged(Editable s) {
		String text = textView.getText().toString();
		validate(textView, text);
	}

	@Override
	final public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* Don't care */ }

	@Override
	final public void onTextChanged(CharSequence s, int start, int before, int count) { /* Don't care */ }

	public void showInvalidColor(){
		textView.setBackgroundResource(R.drawable.shape_rectangle_warning);
	}

	public void showValidColor(){
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
