package com.splitemapp.android.widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.screen.project.ProjectFragment;
import com.splitemapp.android.utils.ViewUtils;

public class CustomFloatingActionButton {

	private static final String TAG = ProjectFragment.class.getSimpleName();

	private FloatingActionButton mainFab;
	private List<ActionFAB>  actionFabList;
	private View shade;

	public CustomFloatingActionButton(Context context, final FloatingActionButton mainFab) {
		// Creating new FAB action list
		this.actionFabList = new ArrayList<ActionFAB>();

		// Assigning the main FAB an OnClickListener to manage the list of action FABs
		this.mainFab = mainFab;
		this.mainFab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Switching shade view visibility
				switch(shade.getVisibility()){
				case View.GONE:
					shade.setVisibility(View.VISIBLE);
					break;
				case View.VISIBLE:
					shade.setVisibility(View.GONE);
					break;
				}

				// Placing all FABs to the front
				mainFab.bringToFront();

				// Switching FAB and label visibility
				for(ActionFAB actionFab:actionFabList){
					// Switching action layout visibility
					switch(actionFab.getActionLayout().getVisibility()){
					case View.VISIBLE: {
						actionFab.getActionLayout().setVisibility(View.GONE);
						break;
					}
					case View.GONE: {
						actionFab.getActionLayout().bringToFront();
						actionFab.getActionLayout().setVisibility(View.VISIBLE);
					}
					}
				}
			}
		});

		// Creating the shade view
		shade = new View(context, getAttributeSetFromResource(context, R.drawable.shade));
		shade.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Blocking user input
			}
		});
		shade.setVisibility(View.GONE);

		// Adding the shade as a child to the mainFab's parent layout
		RelativeLayout layout = (RelativeLayout)this.mainFab.getParent();
		layout.addView(shade);
	}

	public void addActionFab(Context context, String labelString, int themeResource, OnClickListener onClickListener){
		// Creating a new FAB
		FloatingActionButton actionFab = createActionFab(context, themeResource, onClickListener);

		// Creating a new label for the FAB
		TextView label = createLabel(context, labelString, actionFab.getId());

		// Creating a new Relative Layout to contain the FAB and label
		RelativeLayout actionLayout = createActionLayout(context);

		// Adding the new FAB to the list
		this.actionFabList.add(new ActionFAB(actionLayout, actionFab, label));

		// Adding the new relative layout as a child to the mainFab's parent layout
		RelativeLayout layout = (RelativeLayout)this.mainFab.getParent();
		layout.addView(actionLayout);
	}

	private RelativeLayout createActionLayout(Context context){
		// Adding the actionFab and the label as a child to the new relative layout 
		RelativeLayout.LayoutParams actionLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		if(this.actionFabList.size()==0){
			actionLayoutParams.addRule(RelativeLayout.ABOVE, this.mainFab.getId());
		} else {
			actionLayoutParams.addRule(RelativeLayout.ABOVE, this.actionFabList.get(this.actionFabList.size()-1).getActionLayout().getId());
		}

		// Creating relative layout and assigning layout parameters
		RelativeLayout actionLayout = new RelativeLayout(context);
		actionLayout.setLayoutParams(actionLayoutParams);

		// Setting default visibility to GONE
		actionLayout.setVisibility(View.GONE);

		// Setting an id to the action layout
		actionLayout.setId(ViewUtils.generateViewId());

		return actionLayout;
	}

	private TextView createLabel(Context context, String labelString, int fabId){
		// Creating a new Label
		TextView label = new TextView(context);

		// Setting an id to the FAB
		label.setId(ViewUtils.generateViewId());

		// Setting LayoutParams for the label including the relative position
		RelativeLayout.LayoutParams labelLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		labelLayoutParams.addRule(RelativeLayout.LEFT_OF, fabId);
		labelLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		label.setLayoutParams(labelLayoutParams);

		// Setting rounded shape
		label.setBackgroundResource(R.drawable.label_rounded_corners);

		// Setting the labels text
		label.setText(labelString);

		// Setting the labels padding
		label.setPadding(5, 5, 5, 5);

		// Setting the text color
		label.setTextColor(Color.BLACK);

		return label;
	}

	private FloatingActionButton createActionFab(Context context, int themeResource, OnClickListener onClickListener){
		// Creating new FAB
		FloatingActionButton actionFab = new FloatingActionButton(context, getAttributeSetFromResource(context, themeResource));

		// Setting an id to the FAB
		actionFab.setId(ViewUtils.generateViewId());

		// Setting LayoutParams for the FAB including the relative position
		RelativeLayout.LayoutParams fabLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		fabLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		fabLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		fabLayoutParams.setMargins(-15, -25, 0, -25);
		actionFab.setLayoutParams(fabLayoutParams);

		// Assigning the provided onClickListener
		actionFab.setOnClickListener(onClickListener);

		return actionFab;
	}

	private AttributeSet getAttributeSetFromResource(Context context, int themeResource){
		// Loading the action FAB attributes from XML
		XmlResourceParser parser = context.getResources().getXml(themeResource);

		// Seeking to the first tag.
		int type = 0;
		while (type != XmlPullParser.END_DOCUMENT && type != XmlPullParser.START_TAG) {
			try {
				type = parser.next();
			} catch (XmlPullParserException e) {
				Log.e(TAG, "XmlPullParserException caught!", e);
			} catch (IOException e) {
				Log.e(TAG, "IOException caught!", e);
			}
		}

		return Xml.asAttributeSet(parser);
	}

	public FloatingActionButton getMainFab() {
		return mainFab;
	}
	public void setMainFab(FloatingActionButton mainFab) {
		this.mainFab = mainFab;
	}
	public List<ActionFAB> getActionFabList() {
		return actionFabList;
	}
	public void setActionFabList(List<ActionFAB> actionFabList) {
		this.actionFabList = actionFabList;
	}

	public View getShade() {
		return shade;
	}

	public void setShade(View shade) {
		this.shade = shade;
	}

}
