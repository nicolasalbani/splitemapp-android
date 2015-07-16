package com.splitemapp.android.screen.project;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.splitemapp.android.R;
import com.splitemapp.android.constants.Globals;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.screen.expense.ExpenseActivity;
import com.splitemapp.android.utils.ImageUtils;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.domain.ExpenseCategory;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.ProjectCoverImage;
import com.splitemapp.commons.domain.UserExpense;

public class ProjectFragment extends BaseFragment {

	private static final String TAG = ProjectFragment.class.getSimpleName();

	private List<UserExpense> mUserExpenses;
	private Project mCurrentProject;

	private TextView mProjectTitle;
	private ImageView mProjectCoverImage;
	private ListView mUserExpensesList;
	private Button mAddNewExpense;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// We get the current user and project instances
		try {
			mCurrentProject = getHelper().getProjectById(Globals.getExpenseActivityProjectId());
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_project, container, false);

		// Populating the project title
		mProjectTitle = (TextView) v.findViewById(R.id.p_project_title_textView);
		mProjectTitle.setText(mCurrentProject.getTitle());
		
		// Populating the project cover image
		mProjectCoverImage = (ImageView) v.findViewById(R.id.p_project_cover_image_imageView);
		mProjectCoverImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// in onCreate or any event where your want the user to select a file
				openImageSelector(mProjectCoverImage.getWidth(), mProjectCoverImage.getHeight());
			}
		});
		setProjectCoverImage(mProjectCoverImage, mCurrentProject, 100);

		// Getting the list of existing expenses and create the expense list adapter
		try {
			mUserExpenses = getHelper().getUserExpenseDao().queryForEq(TableField.USER_EXPENSE_PROJECT_ID, mCurrentProject.getId());
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
		UserExpenseAdapter projectAdapter = new UserExpenseAdapter(mUserExpenses);

		// Populating the list of projects for this user
		mUserExpensesList = (ListView) v.findViewById(R.id.p_expense_list_listView);
		mUserExpensesList.setAdapter(projectAdapter);
		mUserExpensesList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Creating an intent to the ProjectActivity sending the information from the clicked project
				Intent intent = new Intent(getActivity(), ExpenseActivity.class);
				Globals.setExpenseActivityProjectId(mCurrentProject.getId());
				Globals.setExpenseActivityExpenseId(mUserExpenses.get(position).getId());
				startActivity(intent);
			}
		});

		// Getting the reference to the add new list button and implement a OnClickListener
		mAddNewExpense = (Button) v.findViewById(R.id.p_add_expense_button);
		mAddNewExpense.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Moving to the project creation screen
				Intent intent = new Intent(getActivity(), ExpenseActivity.class);
				Globals.setExpenseActivityProjectId(mCurrentProject.getId());
				startActivity(intent);
			}
		});

		return v;
	}

	private class UserExpenseAdapter extends ArrayAdapter<UserExpense>{

		public UserExpenseAdapter(List<UserExpense> userExpenses){
			super(getActivity(), 0, userExpenses);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//If we weren't given a view, inflate one
			if (convertView == null){
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_user_expense, parent, false);
			}

			//Configure the view for this user expense
			UserExpense userExpense = getItem(position);

			TextView expenseCategoryTextView = (TextView)convertView.findViewById(R.id.ue_category_textView);
			Short expenseCategoryId = userExpense.getExpenseCategory().getId();
			ExpenseCategory expenseCategory = null;
			try {
				expenseCategory = getHelper().getExpenseCategoryDao().queryForId(expenseCategoryId.shortValue());
			} catch (SQLException e) {
				Log.e(getLoggingTag(), "SQLException caught!", e);
			}
			expenseCategoryTextView.setText(expenseCategory.getTitle());

			TextView expenseDateTextView = (TextView)convertView.findViewById(R.id.ue_date_textView);
			DateFormat dateFormat = SimpleDateFormat.getDateInstance();
			String date = dateFormat.format(userExpense.getExpenseDate());
			expenseDateTextView.setText(date);

			TextView expenseAmountTextView = (TextView)convertView.findViewById(R.id.ue_amount_textView);
			expenseAmountTextView.setText(userExpense.getExpense().toString());

			return convertView;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();

		// Refreshing member list when coming back from the Add People fragment
		((BaseAdapter) mUserExpensesList.getAdapter()).notifyDataSetChanged(); 
	}

	@Override
	public void executeOnImageSelection(Bitmap selectedBitmap) {
		// Updating project image on screen
		mProjectCoverImage.setImageBitmap(selectedBitmap);
		
		// Persisting selected image to database
		try {
			ProjectCoverImage projectCoverImage = getHelper().getProjectCoverImageDao().queryForEq(TableField.PROJECT_COVER_IMAGE_PROJECT_ID, mCurrentProject.getId()).get(0);
			projectCoverImage.setAvatarData(ImageUtils.bitmapToByteArray(selectedBitmap,ImageUtils.IMAGE_QUALITY_MAX));
			getHelper().getProjectCoverImageDao().createOrUpdate(projectCoverImage);
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}
	}

	@Override
	public String getLoggingTag() {
		return TAG;
	}

}
