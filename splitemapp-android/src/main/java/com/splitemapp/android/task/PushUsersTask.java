package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.dto.UserDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;
import com.splitemapp.commons.domain.id.IdReference;
import com.splitemapp.commons.domain.id.IdUpdate;

/**
 * Sync Task to push user table data to the remote DB
 * @author nicolas
 */
public abstract class PushUsersTask extends PushTask<UserDTO, Long, PushLongResponse> {
	
	public PushUsersTask(DatabaseHelper databaseHelper) {
		super(databaseHelper);
	}
	
	@Override
	protected String getLoggingTag() {
		return getClass().getSimpleName();
	}

	@Override
	protected String getTableName(){
		return TableName.USER;
	}

	@Override
	protected String getServicePath(){
		return ServiceConstants.PUSH_USERS_PATH;
	}

	@Override
	protected Class<PushLongResponse> getResponseType() {
		return PushLongResponse.class;
	}

	@Override
	protected List<UserDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
		// We get all the project in the database
		List<User> userList = databaseHelper.getUserList();

		// We add to the project DTO list the ones which were updated after the lastPushSuccessAt date 
		ArrayList<UserDTO> userDTOList = new ArrayList<UserDTO>();
		for(User user:userList){
			if(user.getUpdatedAt().after(lastPushSuccessAt)){
				userDTOList.add(new UserDTO(user));
			}
		}
		return userDTOList;
	}

	@Override
	protected void processResult(PushLongResponse response) throws SQLException {
		// Updating sync status
		databaseHelper.updateSyncStatusPushAt(User.class, response.getSuccess());

		List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

		// We create the ID reference list to be updated
		List<IdReference> idReferenceList = new ArrayList<IdReference>();
		idReferenceList.add(new IdReference(TableName.USER, TableField.USER_ID));
		idReferenceList.add(new IdReference(TableName.USER_TO_PROJECT, TableField.USER_TO_PROJECT_USER_ID));
		idReferenceList.add(new IdReference(TableName.USER_EXPENSE, TableField.USER_EXPENSE_USER_ID));
		idReferenceList.add(new IdReference(TableName.USER_INVITE, TableField.USER_INVITE_USER_ID));
		idReferenceList.add(new IdReference(TableName.USER_CONTACT_DATA, TableField.USER_CONTACT_DATA_USER_ID));
		idReferenceList.add(new IdReference(TableName.USER_AVATAR, TableField.USER_AVATAR_USER_ID));

		//We update all references to this ID
		for(IdUpdate<Long> idUpdate:idUpdateList){
			databaseHelper.updateIdReferences(idUpdate, idReferenceList);
		}
	}
}
