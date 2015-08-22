package com.splitemapp.android.screen;

import com.splitemapp.android.R;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public abstract class BaseFragmentWithBlueActionbar extends BaseFragment {

	protected Toolbar actionBar;
	protected TextView mCancel;
	protected TextView mDone;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View v = inflater.inflate(getFragmentResourceId(), container, false);

		// Setting the action bar
		actionBar = (Toolbar) v.findViewById(R.id.actionBar);
		((AppCompatActivity) getActivity()).setSupportActionBar(actionBar);
		((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
		
		// Setting the cancel and done buttons
		mCancel = (TextView) v.findViewById(R.id.cancel_action);
		mCancel.setText(getTitleResourceId());
		mCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});
		
		mDone = (TextView) v.findViewById(R.id.done_action);
		mDone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doneAction();
			}
		});
		
		return v;
	}

	/**
	 * Gets the resource id for the fragment
	 * @return
	 */
	protected abstract int getFragmentResourceId();
	
	/**
	 * Gets the resource id for the title
	 * @return
	 */
	protected abstract int getTitleResourceId();
	
	/**
	 * Action to execute when pressing the DONE button
	 */
	protected abstract void doneAction();
}
