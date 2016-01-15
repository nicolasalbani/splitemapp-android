package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.Set;

import android.content.Context;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserAvatar;
import com.splitemapp.commons.domain.dto.UserAvatarDTO;
import com.splitemapp.commons.domain.dto.response.PullUserAvatarResponse;

public class PullUserAvatarsTask extends PullTask<UserAvatarDTO, PullUserAvatarResponse> {

	private static final String TAG = PullUserAvatarsTask.class.getSimpleName();

	public PullUserAvatarsTask(Context context) {
		super(context);
	}

	@Override
	protected String getTableName() {
		return TableName.USER_AVATAR;
	}

	@Override
	protected String getServicePath() {
		return ServiceConstants.PULL_USER_AVATARS_PATH;
	}

	@Override
	protected String getLoggingTag() {
		return TAG;
	}

	@Override
	protected void processResult(PullUserAvatarResponse response) throws SQLException {
		// Updating sync status
		getHelper().updateSyncStatusPullAt(UserAvatar.class, response.getSuccess(), response.getPulledAt());

		Set<UserAvatarDTO> userAvatarDTOs = response.getItemSet();
		for(UserAvatarDTO userAvatarDTO:userAvatarDTOs){
			// We obtain the required parameters for the object creation from the local database
			User user = getHelper().getUser(userAvatarDTO.getUserId().longValue());

			// We create the new entity and store it into the local database
			UserAvatar userAvatar = new UserAvatar(user, userAvatarDTO);
			getHelper().createOrUpdateUserAvatar(userAvatar);
		}
	}

	@Override
	protected Class<PullUserAvatarResponse> getResponseType() {
		return PullUserAvatarResponse.class;
	}

}
