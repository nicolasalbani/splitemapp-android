package com.splitemapp.android.screen.balance;

import java.math.BigDecimal;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.expense.ExpenseAmountFormat;

public class ViewHolderUserSecondary extends RecyclerView.ViewHolder implements OnClickListener{

	private static BigDecimal mTotalExpenseValue;
	private static ExpenseAmountFormat mExpenseAmountFormat;

	// Each data item is a project
	public boolean mInitializedSeekBar;
	public ImageView mIconImageView;
	public TextView mAmountTextView;
	public BigDecimal mAmount;
	public TextView mShareTextView;
	public TextView mShareBalanceTextView;
	public SeekBar mSeekBar;
	public IExpenseGroupClickListener mClickListener;

	public ViewHolderUserSecondary(View view, BigDecimal totalExpenseValue, ExpenseAmountFormat expenseAmountFormat, IExpenseGroupClickListener clickListener) {
		super(view);
		mTotalExpenseValue = totalExpenseValue;
		mExpenseAmountFormat = expenseAmountFormat;
		mIconImageView = (ImageView)view.findViewById(R.id.b_icon_imageView);
		mAmountTextView = (TextView)view.findViewById(R.id.b_amount_textView);

		mInitializedSeekBar = false;
		mShareTextView = (TextView)view.findViewById(R.id.b_share_textView);
		mShareBalanceTextView = (TextView)view.findViewById(R.id.b_share_balance_textView);
		mSeekBar = (SeekBar)view.findViewById(R.id.b_seekBar);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mShareTextView.setVisibility(View.INVISIBLE);
				mIconImageView.setVisibility(View.VISIBLE);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				mIconImageView.setVisibility(View.INVISIBLE);
				mShareTextView.setVisibility(View.VISIBLE);
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mShareTextView.setText(seekBar.getProgress() + "%");
				updateShareBalance(seekBar, mAmount, mShareBalanceTextView);
			}
		});

		mClickListener = clickListener;
		view.setOnClickListener(this);
	}

	/**
	 * Updates the textView with the appropriate share balance
	 * @param seekBar
	 * @param amount
	 * @param textView
	 */
	public void updateShareBalance(SeekBar seekBar, BigDecimal amount, TextView textView){
		BigDecimal balance = mTotalExpenseValue.multiply(new BigDecimal((float)seekBar.getProgress()/100)).subtract(amount);
		textView.setText("$"+ mExpenseAmountFormat.format(balance.abs()));
		if(balance.signum()>0){
			textView.setTextColor(Color.RED);
		} else {
			textView.setTextColor(Color.GREEN);
		}
	}

	@Override
	public void onClick(View view) {
		// Calling the custom on click listener
		mClickListener.onItemClick(view, getAdapterPosition());
	}
}
