package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.Set;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.ExpenseCategory;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserExpense;
import com.splitemapp.commons.domain.dto.UserExpenseDTO;
import com.splitemapp.commons.domain.dto.response.PullUserExpenseResponse;

/**
 * Sync Task to pull user_expense table data from the remote DB
 * @author nicolas
 */
public abstract class PullUserExpensesTask extends PullTask<UserExpenseDTO, PullUserExpenseResponse> {
	
	public PullUserExpensesTask(DatabaseHelper databaseHelper) {
		super(databaseHelper);
	}
	
	@Override
	protected String getLoggingTag() {
		return getClass().getSimpleName();
	}

	@Override
	protected String getTableName(){
		return TableName.USER_EXPENSE;
	}

	@Override
	protected String getServicePath(){
		return ServiceConstants.PULL_USER_EXPENSES_PATH;
	}

	@Override
	protected void processResult(PullUserExpenseResponse response) throws SQLException {
		// Updating sync status
		databaseHelper.updateSyncStatusPullAt(UserExpense.class, response.getSuccess(), response.getPulledAt());

		Set<UserExpenseDTO> userExpenseDTOs = response.getItemSet();
		for(UserExpenseDTO userExpenseDTO:userExpenseDTOs){
			// We obtain the required parameters for the object creation from the local database
			User user = databaseHelper.getUser(userExpenseDTO.getUserId().longValue());
			Project project = databaseHelper.getProject(userExpenseDTO.getProjectId().longValue());
			ExpenseCategory expenseCategory = databaseHelper.getExpenseCategory(userExpenseDTO.getExpenseCategoryId().shortValue());

			// We create the new entity and store it into the local database
			UserExpense userExpense = new UserExpense(user, project, expenseCategory, userExpenseDTO);
			databaseHelper.createOrUpdateUserExpense(userExpense);
		}
	}

	@Override
	protected Class<PullUserExpenseResponse> getResponseType() {
		return PullUserExpenseResponse.class;
	}
}