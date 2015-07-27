package com.splitemapp.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

public class CustomItemTouchCallback extends ItemTouchHelper.Callback{

	private static final int MAX_SWIPE = -150;
	
	Context context;
	int colorResource;
	
	public CustomItemTouchCallback(Context context, int colorResource){
		this.context = context;
		this.colorResource = colorResource;
	}
	
	@Override
	public void onChildDraw(Canvas c, RecyclerView recyclerView,
			ViewHolder viewHolder, float dX, float dY, int actionState,
			boolean isCurrentlyActive) {

		View itemView = viewHolder.itemView;
		Paint paint = new Paint();
		paint.setColor(context.getResources().getColor(this.colorResource));

		// Drawing the colored rectangle up to MAX_SWIPE 
		if(dX <= 0){
			if(dX > MAX_SWIPE){
				c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), paint);
				super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
			} else {
				c.drawRect((float) itemView.getRight() + MAX_SWIPE, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), paint);
				super.onChildDraw(c, recyclerView, viewHolder, MAX_SWIPE, dY, actionState, isCurrentlyActive);
			}
		}
	}
	@Override
	public boolean onMove(RecyclerView arg0, ViewHolder arg1, ViewHolder arg2) {
		return false;
	}
	@Override
	public int getMovementFlags(RecyclerView arg0, ViewHolder arg1) {
		return makeMovementFlags(0, ItemTouchHelper.LEFT);
	}

	@Override
	public void onSwiped(ViewHolder arg0, int arg1) {
		// Do nothing
	}

}
