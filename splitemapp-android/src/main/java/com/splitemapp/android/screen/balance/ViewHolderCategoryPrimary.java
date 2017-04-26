package com.splitemapp.android.screen.balance;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.splitemapp.android.R;

public class ViewHolderCategoryPrimary extends RecyclerView.ViewHolder implements OnClickListener{

	// Each data item is a project
	public ImageView mIconImageView;
	public View mBarView;
	public TextView mAmountTextView;
	public IExpenseGroupClickListener mClickListener;

	public ViewHolderCategoryPrimary(View view, IExpenseGroupClickListener clickListener) {
		super(view);
		mIconImageView = (ImageView)view.findViewById(R.id.b_icon_imageView);
		mAmountTextView = (TextView)view.findViewById(R.id.b_amount_textView);
		mBarView = view.findViewById(R.id.b_bar_view);

		mClickListener = clickListener;
		view.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		// Calling the custom on click listener
		mClickListener.onItemClick(view, getAdapterPosition());
	}

}
