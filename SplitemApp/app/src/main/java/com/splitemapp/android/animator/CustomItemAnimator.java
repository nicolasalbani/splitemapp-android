package com.splitemapp.android.animator;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class CustomItemAnimator extends DefaultItemAnimator {

	@Override
	public boolean animateAdd(ViewHolder holder) {
		Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), android.R.anim.slide_in_left);
		holder.itemView.setAnimation(animation);
		return super.animateAdd(holder);
	}
}
