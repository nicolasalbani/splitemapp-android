package com.splitemapp.android.widget;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

public abstract class ListAlertDialog {

	private AlertDialog.Builder builder;
	private View dialogView;

	public ListAlertDialog(Activity activity){
		builder = new AlertDialog.Builder(activity);
		
		// Inflating the layout
		LayoutInflater inflater = activity.getLayoutInflater();
		dialogView = inflater.inflate(getLayoutView(), null);
		builder.setView(dialogView);
	}

	/**
	 * Shows this dialog in the screen
	 */
	public void show(){
		builder.show();
	}
	
	/**
	 * Returns the required View by id
	 * @param id
	 * @return
	 */
	public View findViewById(int id){
		return dialogView.findViewById(id);
	}
	
	/**
	 * Returns the integer which points to the selected view to be used
	 * @return
	 */
	public abstract int getLayoutView();
	
}
