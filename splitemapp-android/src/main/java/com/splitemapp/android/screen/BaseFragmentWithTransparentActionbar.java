package com.splitemapp.android.screen;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.splitemapp.android.R;

public abstract class BaseFragmentWithTransparentActionbar extends BaseFragment {

	protected Toolbar actionBar;
	protected ImageView mBack;
	protected ImageView mMenu;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View v = inflater.inflate(getFragmentResourceId(), container, false);

		// Setting the action bar
		actionBar = (Toolbar) v.findViewById(R.id.actionBar);
		((AppCompatActivity) getActivity()).setSupportActionBar(actionBar);
		((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
		
		// Setting the cancel and done buttons
		mBack = (ImageView) v.findViewById(R.id.back_action_imageView);
		mBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});
		
		mMenu = (ImageView) v.findViewById(R.id.menu_action_imageView);
		mMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				menuAction();
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
	 * Action to execute when pressing the DONE button
	 */
	protected abstract void menuAction();
}
