package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.dto.UserDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;
import com.splitemapp.commons.domain.id.IdReference;
import com.splitemapp.commons.domain.id.IdUpdate;

public class PushUsersTask extends PushTask<User, UserDTO, Long, PushLongResponse> {

	private static final String TAG = PushUsersTask.class.getSimpleName();

	List<User> userList = null;

	public PushUsersTask(Context context) {
		super(context);
	}

	@Override
	protected String getLoggingTag() {
		return TAG;
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
	protected List<UserDTO> getRequestItemList() throws SQLException {
		// We get all the project in the database
		// TODO only get the ones marked for push
		userList = getHelper().getUserList();

		// We add to the project DTO list the ones which were updated after the lastPushSuccessAt date
		// and that they were not updated by someone else
		ArrayList<UserDTO> userDTOList = new ArrayList<UserDTO>();
		for(User user:userList){
			if((user.getPushedAt() == null) || user.getUpdatedAt().after(user.getPushedAt())){
				// Adding item to the list
				userDTOList.add(new UserDTO(user));
			}
		}
		return userDTOList;
	}

	@Override
	protected void processResult(PushLongResponse response) throws SQLException {
		// Updating sync status
		getHelper().updateSyncStatusPushAt(User.class, response.getSuccess(), response.getPushedAt());
		
		// Updating pushedAt
		for(User entity:userList){
			getHelper().updatePushedAt(entity, response.getPushedAt());
		}

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
		updateIdReferences(idUpdateList, idReferenceList);
	}
}
