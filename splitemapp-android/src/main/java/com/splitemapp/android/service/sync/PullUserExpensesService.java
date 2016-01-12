package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.Set;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.ExpenseCategory;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserExpense;
import com.splitemapp.commons.domain.dto.UserExpenseDTO;
import com.splitemapp.commons.domain.dto.response.PullUserExpenseResponse;

public class PullUserExpensesService extends PullService<UserExpenseDTO, PullUserExpenseResponse> {

	private static final String TAG = PullUserExpensesService.class.getSimpleName();

	public PullUserExpensesService() {
		super(TAG);
	}

	@Override
	protected String getTableName() {
		return TableName.USER_EXPENSE;
	}

	@Override
	protected String getServicePath() {
		return ServiceConstants.PULL_USER_EXPENSES_PATH;
	}

	@Override
	protected String getLoggingTag() {
		return TAG;
	}

	@Override
	protected void processResult(PullUserExpenseResponse response) throws SQLException {
		// Updating sync status
		getHelper().updateSyncStatusPullAt(UserExpense.class, response.getSuccess(), response.getPulledAt());

		Set<UserExpenseDTO> userExpenseDTOs = response.getItemSet();
		for(UserExpenseDTO userExpenseDTO:userExpenseDTOs){
			// We obtain the required parameters for the object creation from the local database
			User user = getHelper().getUser(userExpenseDTO.getUserId().longValue());
			Project project = getHelper().getProject(userExpenseDTO.getProjectId().longValue());
			ExpenseCategory expenseCategory = getHelper().getExpenseCategory(userExpenseDTO.getExpenseCategoryId().shortValue());

			// We create the new entity and store it into the local database
			UserExpense userExpense = new UserExpense(user, project, expenseCategory, userExpenseDTO);
			getHelper().createOrUpdateUserExpense(userExpense);
		}
	}

	@Override
	protected Class<PullUserExpenseResponse> getResponseType() {
		return PullUserExpenseResponse.class;
	}

}
