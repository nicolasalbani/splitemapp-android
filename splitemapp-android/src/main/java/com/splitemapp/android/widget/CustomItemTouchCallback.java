package com.splitemapp.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.splitemapp.android.R;

public class CustomItemTouchCallback extends ItemTouchHelper.Callback{

	private static final int MAX_SWIPE_RIGHT = 120;
	private static final int MAX_SWIPE_LEFT = -120;
	private static final int TEXT_SIZE = 25;

	Context context;
	int leftColorResource;
	int leftStringResource;
	int rightColorResource;
	int rightStringResource;

	public CustomItemTouchCallback(Context context, boolean isLeftToRigth, int colorResource, int stringResource){
		this.context = context;
		if(isLeftToRigth){
			this.leftColorResource = colorResource;
			this.leftStringResource = stringResource;
			this.rightColorResource = 0;
			this.rightStringResource = 0;
		} else {
			this.leftColorResource = 0;
			this.leftStringResource = 0;
			this.rightColorResource = colorResource;
			this.rightStringResource = stringResource;
		}
	}

	public CustomItemTouchCallback(Context context, int leftColorResource, int leftStringResource, int rightColorResource, int rightStringResource){
		this.context = context;
		this.leftColorResource = leftColorResource;
		this.leftStringResource = leftStringResource;
		this.rightColorResource = rightColorResource;
		this.rightStringResource = rightStringResource;
	}

	@Override
	public void onChildDraw(Canvas canvas, RecyclerView recyclerView, ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
		
		if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
			
			View itemView = viewHolder.itemView;

			// Drawing the colored rectangle up to MAX_SWIPE 
			if(isSwipeRightToLeft(dX)){
				if(dX > MAX_SWIPE_LEFT){
					drawRectWithText(canvas, dX, (float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
					super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
				} else {
					drawRectWithText(canvas, dX, (float) itemView.getRight() + MAX_SWIPE_LEFT, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
					super.onChildDraw(canvas, recyclerView, viewHolder, MAX_SWIPE_LEFT, dY, actionState, isCurrentlyActive);
				}
			} else {
				if(dX < MAX_SWIPE_RIGHT){
					drawRectWithText(canvas, dX, (float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
					super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
				} else {
					drawRectWithText(canvas, dX, (float) itemView.getLeft(), (float) itemView.getTop(), MAX_SWIPE_RIGHT, (float) itemView.getBottom());
					super.onChildDraw(canvas, recyclerView, viewHolder, MAX_SWIPE_RIGHT, dY, actionState, isCurrentlyActive);
				}
			}
		}
		
	}
	

	/**
	 * Returns a boolean indicating whether this is a right to left or left to right swipe
	 * @param dX
	 * @return
	 */
	private boolean isSwipeRightToLeft(float dX){
		if(dX <= 0){
			return true;
		} else {
			return false;
		}
	} 

	/**
	 * Returns boolean indicating whether left to right swipe is enabled
	 * @return
	 */
	private boolean isLeftToRightEnabled(){
		if(this.leftColorResource != 0 && this.leftStringResource != 0){
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns boolean indicating whether right to left swipe is enabled
	 * @return
	 */
	private boolean isRightToLeftEnabled(){
		if(this.leftColorResource != 0 && this.leftStringResource != 0){
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public float getSwipeThreshold(ViewHolder viewHolder) {
		return 1f;
	}

	/**
	 * Draws a rectangle in the specified position with the specified background color and text
	 * @param canvas
	 * @param dX
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	private void drawRectWithText(Canvas canvas, float dX, float left, float top, float right, float bottom){
		int backgroundColor = 0;
		String text = null;
		
		if(isSwipeRightToLeft(dX)){
			backgroundColor = ContextCompat.getColor(context, this.rightColorResource);
			text = context.getResources().getString(this.rightStringResource);
		} else {
			backgroundColor = ContextCompat.getColor(context, this.leftColorResource);
			text = context.getResources().getString(this.leftStringResource);
		}
		
		// Drawing rectangle
		Paint paint = new Paint();
		paint.setColor(backgroundColor);
		canvas.drawRect(left, top, right, bottom, paint);

		// Drawing text
		paint.setColor(ContextCompat.getColor(context, R.color.white));
		paint.setTextSize(TEXT_SIZE);
		paint.setTextAlign(Align.CENTER);
		paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
		canvas.drawText(text, (right + left) / 2, ((top + bottom) / 2) + (TEXT_SIZE / 3), paint);
	}

	@Override
	public boolean onMove(RecyclerView arg0, ViewHolder arg1, ViewHolder arg2) {
		return false;
	}

	@Override
	public int getMovementFlags(RecyclerView arg0, ViewHolder arg1) {
		int movementFlags = 0;

		// Including LEFT flags if left to right swipe is enabled
		if(isLeftToRightEnabled()){
			movementFlags = movementFlags | ItemTouchHelper.LEFT;
		}

		// Including RIGHT flags if right to left swipe is enabled
		if(isRightToLeftEnabled()){
			movementFlags = movementFlags | ItemTouchHelper.RIGHT;
		}

		return makeMovementFlags(0, movementFlags);
	}

	@Override
	public void onSwiped(ViewHolder arg0, int arg1) {
		// Do nothing
	}

}
