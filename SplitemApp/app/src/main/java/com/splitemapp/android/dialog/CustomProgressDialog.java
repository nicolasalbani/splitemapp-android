package com.splitemapp.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

import com.splitemapp.android.R;

public class CustomProgressDialog extends Dialog {

	public static CustomProgressDialog show(Context context) {
		return show(context, null, null, false, false, null);
	}

	public static CustomProgressDialog show(Context context, CharSequence title,
			CharSequence message) {
		return show(context, title, message, false, false, null);
	}

	public static CustomProgressDialog show(Context context, CharSequence title,
			CharSequence message, boolean indeterminate,
			boolean cancelable, OnCancelListener cancelListener) {
		CustomProgressDialog dialog = new CustomProgressDialog(context);
		dialog.setTitle(title);
		dialog.setCancelable(cancelable);
		dialog.setOnCancelListener(cancelListener);
		dialog.addContentView(new ProgressBar(context), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		dialog.show();

		return dialog;
	}

	public CustomProgressDialog(Context context) {
		super(context, R.style.TransparentDialog);
	}

}
