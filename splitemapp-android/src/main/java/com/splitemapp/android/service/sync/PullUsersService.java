package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.Set;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserStatus;
import com.splitemapp.commons.domain.dto.UserDTO;
import com.splitemapp.commons.domain.dto.response.PullUserResponse;

public class PullUsersService extends PullService<UserDTO, PullUserResponse> {

	private static final String TAG = PullUsersService.class.getSimpleName();

	public PullUsersService() {
		super(TAG);
	}

	@Override
	protected String getTableName() {
		return TableName.USER;
	}

	@Override
	protected String getServicePath() {
		return ServiceConstants.PULL_USERS_PATH;
	}

	@Override
	protected String getLoggingTag() {
		return TAG;
	}

	@Override
	protected void processResult(PullUserResponse response) throws SQLException {
		// Updating sync status
		getHelper().updateSyncStatusPullAt(User.class, response.getSuccess(), response.getPulledAt());

		Set<UserDTO> userDTOs = response.getItemSet();
		for(UserDTO userDTO:userDTOs){
			// We obtain the required parameters for the object creation from the local database
			UserStatus userStatus = getHelper().getUserStatus(userDTO.getUserStatusId().shortValue());

			// We create the new entity and store it into the local database
			User user = new User(userStatus, userDTO);
			getHelper().createOrUpdateUser(user);
		}
	}

	@Override
	protected Class<PullUserResponse> getResponseType() {
		return PullUserResponse.class;
	}
}
