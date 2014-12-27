package com.splitemapp.android.screen.project;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.j256.ormlite.dao.Dao;
import com.splitemapp.android.R;
import com.splitemapp.android.screen.BaseFragment;
import com.splitemapp.android.screen.expense.ExpenseActivity;
import com.splitemapp.android.screen.expense.ExpenseFragment;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.domain.ExpenseCategory;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserExpense;

public class ProjectFragment extends BaseFragment {

	public static final String EXTRA_PROJECT_ID = "com.splitemapp.android.project_id";
	public static final String EXTRA_USER_ID = "com.splitemapp.android.user_id";

	private static final String TAG = ProjectFragment.class.getSimpleName();

	private List<UserExpense> mUserExpenses;
	private User mCurrentUser;
	private Project mCurrentProject;

	private TextView mProjectTitle;
	private ListView mUserExpensesList;
	private Button mAddNewExpense;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle arguments = getActivity().getIntent().getExtras();

		// We get the current user and project instances
		Long userId = (Long)arguments.getSerializable(EXTRA_USER_ID);
		mCurrentUser = getCurrentUser(userId);
		Long projectId = (Long)arguments.getSerializable(EXTRA_PROJECT_ID);
		mCurrentProject = getCurrentProject(projectId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_project, container, false);

		// We populate the project title
		mProjectTitle = (TextView) v.findViewById(R.id.p_project_title_textView);
		mProjectTitle.setText(mCurrentProject.getTitle());

		// We get the list of existing expenses and create the expense list adapter
		try {
			mUserExpenses = getHelper().getUserExpensesDao().queryForEq(TableField.USER_EXPENSE_PROJECT_ID, mCurrentProject.getId());
		} catch (SQLException e) {
			Log.e(TAG, "SQLException caught!", e);
		}
		UserExpenseAdapter projectAdapter = new UserExpenseAdapter(mUserExpenses);

		// We populate the list of projects for this user
		mUserExpensesList = (ListView) v.findViewById(R.id.p_expense_list_listView);
		mUserExpensesList.setAdapter(projectAdapter);
		mUserExpensesList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// We create an intent to the ProjectActivity sending the information from the clicked project
				Intent intent = new Intent(getActivity(), ExpenseActivity.class);
				intent.putExtra(ExpenseFragment.EXTRA_USER_ID, mCurrentUser.getId());
				intent.putExtra(ExpenseFragment.EXTRA_PROJECT_ID, mCurrentProject.getId());
				intent.putExtra(ExpenseFragment.EXTRA_EXPENSE_ID, mUserExpenses.get(position).getId());
				startActivity(intent);
			}
		});

		// We get the reference to the add new list button and implement a OnClickListener
		mAddNewExpense = (Button) v.findViewById(R.id.p_add_expense_button);
		mAddNewExpense.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// We move to the project creation screen
				Intent intent = new Intent(getActivity(), ExpenseActivity.class);
				intent.putExtra(ExpenseFragment.EXTRA_USER_ID, mCurrentUser.getId());
				intent.putExtra(ExpenseFragment.EXTRA_PROJECT_ID, mCurrentProject.getId());
				startActivity(intent);
			}
		});

		return v;
	}
	
	private Project getCurrentProject(Long projectId){
		Project project = null;
		try {
			Dao<Project,Integer> projectDao = getHelper().getProjectDao();
			for(Project u:projectDao){
				if(projectId.equals(u.getId())){
					project = u;
				}
			}
		} catch (SQLException e) {
			Log.e(getLoggingTag(), "SQLException caught!", e);
		}
		return project;
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
				expenseCategory = getHelper().getExpenseCategoryDao().queryForId(expenseCategoryId.intValue());
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
	public String getLoggingTag() {
		return TAG;
	}

}
