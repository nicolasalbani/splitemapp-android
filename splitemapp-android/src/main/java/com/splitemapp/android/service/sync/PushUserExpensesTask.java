package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.splitemapp.android.service.BaseTask;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.UserExpense;
import com.splitemapp.commons.domain.dto.UserExpenseDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;
import com.splitemapp.commons.domain.id.IdReference;
import com.splitemapp.commons.domain.id.IdUpdate;

public class PushUserExpensesTask extends PushTask<UserExpense, UserExpenseDTO, Long, PushLongResponse> {

	private static final String TAG = PushUserExpensesTask.class.getSimpleName();

	List<UserExpense> userExpenseList = null;

	public PushUserExpensesTask(Context context) {
		super(context);
	}

	@Override
	protected String getLoggingTag() {
		return TAG;
	}

	@Override
	protected String getTableName(){
		return TableName.USER_EXPENSE;
	}

	@Override
	protected String getServicePath(){
		return ServiceConstants.PUSH_USER_EXPENSES_PATH;
	}

	@Override
	protected List<UserExpenseDTO> getRequestItemList() throws SQLException {
		// We get all the project in the database
		// TODO only get the ones marked for push
		userExpenseList = getHelper().getUserExpenseList();

		// Removing items that should not be pushed
		removeNotPushable(userExpenseList);

		// We add to the DTO list the ones which were updated after the lastPushSuccessAt date
		// and that they were not updated by someone else
		ArrayList<UserExpenseDTO> userExpenseDTOList = new ArrayList<UserExpenseDTO>();
		for(UserExpense userExpense:userExpenseList){
			// Setting the user that pushes the record
			userExpense.setPushedBy(getHelper().getLoggedUser());
			// Adding item to the list
			userExpenseDTOList.add(new UserExpenseDTO(userExpense));
		}
		return userExpenseDTOList;
	}

	@Override
	protected void processResult(PushLongResponse response) throws SQLException {
		// Updating sync status
		getHelper().updateSyncStatusPushAt(UserExpense.class, response.getSuccess(), response.getPushedAt());

		// Updating pushedAt
		for(UserExpense entity:userExpenseList){
			getHelper().updatePushedAt(entity, response.getPushedAt());
		}

		List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

		// We create the ID reference list to be updated
		List<IdReference> idReferenceList = new ArrayList<IdReference>();
		idReferenceList.add(new IdReference(TableName.USER_EXPENSE, TableField.USER_EXPENSE_ID));

		//We update all references to this ID
		updateIdReferences(idUpdateList, idReferenceList);

		//We notify the update was successful
		broadcastMessage(BaseTask.EXPENSES_PUSHED, ServiceConstants.UI_MESSAGE);
	}
}
