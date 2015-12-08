package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.Set;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserAvatar;
import com.splitemapp.commons.domain.dto.UserAvatarDTO;
import com.splitemapp.commons.domain.dto.response.PullUserAvatarResponse;

/**
 * Sync Task to pull user_contact_data table data from the remote DB
 * @author nicolas
 */
public abstract class PullUserAvatarsTask extends PullTask<UserAvatarDTO, PullUserAvatarResponse> {
	
	public PullUserAvatarsTask(DatabaseHelper databaseHelper) {
		super(databaseHelper);
	}
	
	@Override
	protected String getLoggingTag() {
		return getClass().getSimpleName();
	}

	@Override
	protected String getTableName(){
		return TableName.USER_AVATAR;
	}

	@Override
	protected String getServicePath(){
		return ServiceConstants.PULL_USER_AVATARS_PATH;
	}

	@Override
	protected void processResult(PullUserAvatarResponse response) throws SQLException {
		// Updating sync status
		databaseHelper.updateSyncStatusPullAt(UserAvatar.class, response.getSuccess());

		Set<UserAvatarDTO> userAvatarDTOs = response.getItemSet();
		for(UserAvatarDTO userAvatarDTO:userAvatarDTOs){
			// We obtain the required parameters for the object creation from the local database
			User user = databaseHelper.getUser(userAvatarDTO.getUserId().longValue());

			// We create the new entity and store it into the local database
			UserAvatar userAvatar = new UserAvatar(user, userAvatarDTO);
			databaseHelper.createOrUpdateUserAvatar(userAvatar);
		}
	}

	@Override
	protected Class<PullUserAvatarResponse> getResponseType() {
		return PullUserAvatarResponse.class;
	}
}