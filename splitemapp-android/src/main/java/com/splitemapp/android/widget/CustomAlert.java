package com.splitemapp.android.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

public abstract class CustomAlert {

	private AlertDialog.Builder builder;
	private DialogInterface.OnClickListener dialogClickListener;

	public CustomAlert(Context context){
		// Creating the onClick listener
		dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					executeOnPositiveAnswer();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					executeOnNegativeAnswer();
					break;
				}
			}
		};

		builder = new AlertDialog.Builder(context);
		builder.setMessage(getMessage()).setPositiveButton(getPositiveButtonText(), dialogClickListener)
		.setNegativeButton(getNegativeButtonText(), dialogClickListener);
	}

	public void show(){
		builder.show();
	}

	/**
	 * Returns the message to be prompted to the user
	 * @return
	 */
	public abstract String getMessage();

	/**
	 * Returns the text to be shown in the positive button
	 * @return
	 */
	public abstract String getPositiveButtonText();

	/**
	 * Returns the text to be shown in the negative button
	 * @return
	 */
	public abstract String getNegativeButtonText();

	/**
	 * Code to be executed upon positive answer
	 */
	public abstract void executeOnPositiveAnswer();

	/**
	 * Code to be executed upon negative answer
	 */
	public abstract void executeOnNegativeAnswer();
}
