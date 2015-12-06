package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.Set;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.screen.RestfulFragment;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserStatus;
import com.splitemapp.commons.domain.dto.UserDTO;
import com.splitemapp.commons.domain.dto.response.PullUserResponse;

/**
 * Sync Task to pull user table data from the remote DB
 * @author nicolas
 */
public class PullUsersTask extends PullTask<UserDTO, PullUserResponse> {
	
	public PullUsersTask(DatabaseHelper databaseHelper, RestfulFragment restfulFragment) {
		super(databaseHelper, restfulFragment);
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
		return ServiceConstants.PULL_USERS_PATH;
	}

	@Override
	protected void processResult(PullUserResponse response) throws SQLException {
		// Updating sync status
		databaseHelper.updateSyncStatusPullAt(User.class, response.getSuccess());

		Set<UserDTO> userDTOs = response.getItemSet();
		for(UserDTO userDTO:userDTOs){
			// We obtain the required parameters for the object creation from the local database
			UserStatus userStatus = databaseHelper.getUserStatus(userDTO.getUserStatusId().shortValue());

			// We create the new entity and store it into the local database
			User user = new User(userStatus, userDTO);
			databaseHelper.createOrUpdateUser(user);
		}
	}

	@Override
	protected Class<PullUserResponse> getResponseType() {
		return PullUserResponse.class;
	}
}
