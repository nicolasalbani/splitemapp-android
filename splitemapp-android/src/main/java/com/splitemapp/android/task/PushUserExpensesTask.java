package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.UserExpense;
import com.splitemapp.commons.domain.dto.UserExpenseDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;
import com.splitemapp.commons.domain.id.IdReference;
import com.splitemapp.commons.domain.id.IdUpdate;

/**
 * Sync Task to push user_expense table data to the remote DB
 * @author nicolas
 */
public abstract class PushUserExpensesTask extends PushTask<UserExpenseDTO, Long, PushLongResponse> {
	
	List<UserExpense> userExpenseList = null;

	public PushUserExpensesTask(DatabaseHelper databaseHelper) {
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
		return ServiceConstants.PUSH_USER_EXPENSES_PATH;
	}

	@Override
	protected Class<PushLongResponse> getResponseType() {
		return PushLongResponse.class;
	}

	@Override
	protected List<UserExpenseDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
		// We get all the project in the database
		// TODO only get the ones marked for push
		userExpenseList = databaseHelper.getUserExpenseList();

		// We add to the DTO list the ones which were updated after the lastPushSuccessAt date 
		ArrayList<UserExpenseDTO> userExpenseDTOList = new ArrayList<UserExpenseDTO>();
		for(UserExpense userExpense:userExpenseList){
			if(userExpense.getUpdatedAt().after(lastPushSuccessAt)){
				// Adding item to the list
				userExpenseDTOList.add(new UserExpenseDTO(userExpense));
			}
		}
		return userExpenseDTOList;
	}

	@Override
	protected void processResult(PushLongResponse response) throws SQLException {
		// Updating sync status
		databaseHelper.updateSyncStatusPushAt(UserExpense.class, response.getSuccess());
		
		// Updating pushedAt
		for(UserExpense entity:userExpenseList){
			databaseHelper.updatePushedAt(entity, response.getPushedAt());
		}

		List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

		// We create the ID reference list to be updated
		List<IdReference> idReferenceList = new ArrayList<IdReference>();
		idReferenceList.add(new IdReference(TableName.USER_EXPENSE, TableField.USER_EXPENSE_ID));

		//We update all references to this ID
		for(IdUpdate<Long> idUpdate:idUpdateList){
			databaseHelper.updateIdReferences(idUpdate, idReferenceList);
		}
	}
}
