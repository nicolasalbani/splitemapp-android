package com.splitemapp.android.screen.balance;

import com.splitemapp.android.R;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ViewHolderDate extends RecyclerView.ViewHolder {

	public View mBarView;
	public TextView mMonthTextView;
	public TextView mYearTextView;
	public TextView mAmountTextView;
	public TextView mBalanceTextView;
	
	public ViewHolderDate(View itemView) {
		super(itemView);
		
		mBarView = itemView.findViewById(R.id.b_bar_view);
		mMonthTextView = (TextView)itemView.findViewById(R.id.b_month_textView);
		mYearTextView = (TextView)itemView.findViewById(R.id.b_year_textView);
		mAmountTextView = (TextView)itemView.findViewById(R.id.b_amount_textView);
		mBalanceTextView = (TextView)itemView.findViewById(R.id.b_balance_textView);
	}

}
