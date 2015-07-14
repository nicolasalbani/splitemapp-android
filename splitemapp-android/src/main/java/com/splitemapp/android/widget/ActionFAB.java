package com.splitemapp.android.widget;

import android.support.design.widget.FloatingActionButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ActionFAB {
	
	private RelativeLayout actionLayout;
	private FloatingActionButton actionFab;
	private TextView label;
	
	public ActionFAB(RelativeLayout actionLayout, FloatingActionButton actionFab, TextView label) {
		this.actionLayout = actionLayout;
		this.actionFab = actionFab;
		this.label = label;
		
		// Adding child views
		this.actionLayout.addView(actionFab);
		this.actionLayout.addView(label);
	}
	public FloatingActionButton getActionFab() {
		return actionFab;
	}
	public void setActionFab(FloatingActionButton actionFab) {
		this.actionFab = actionFab;
	}
	public TextView getLabel() {
		return label;
	}
	public void setLabel(TextView label) {
		this.label = label;
	}
	public RelativeLayout getActionLayout() {
		return actionLayout;
	}
	public void setActionLayout(RelativeLayout actionLayout) {
		this.actionLayout = actionLayout;
	}

}
