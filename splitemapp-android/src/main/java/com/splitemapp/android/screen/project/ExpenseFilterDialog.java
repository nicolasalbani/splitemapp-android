package com.splitemapp.android.screen.project;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;

public abstract class ExpenseFilterDialog {

	private Dialog dialog;
	private LinearLayout dialogLinearLayout;

	public ExpenseFilterDialog(final Activity activity){
		dialog = new Dialog(activity);
		
		// Inflating and setting the layout
		LayoutInflater inflater = activity.getLayoutInflater();
		dialogLinearLayout = (LinearLayout) inflater.inflate(getLinearLayoutView(), null);
		
		// Setting dialog style
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(dialogLinearLayout);
        
		// Setting window properties
		Window dialogWindow = dialog.getWindow();
		LayoutParams attributes = dialogWindow.getAttributes();
		attributes.gravity = Gravity.CENTER;
		dialogWindow.setAttributes(attributes);
	}

	/**
	 * Shows this dialog in the screen
	 */
	public void show(){
		dialog.show();
	}
	
	/**
	 * Hides this dialog from the screen
	 */
	public void dismiss(){
		dialog.dismiss();
	}
	
	/**
	 * Returns the required View by id
	 * @param id
	 * @return
	 */
	public View findViewById(int id){
		return dialogLinearLayout.findViewById(id);
	}
	
	/**
	 * Returns the integer which points to the selected linear layout to be used
	 * @return
	 */
	public abstract int getLinearLayoutView();
	
}
