package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.Set;

import android.content.Context;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.InviteStatus;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserInvite;
import com.splitemapp.commons.domain.dto.UserInviteDTO;
import com.splitemapp.commons.domain.dto.response.PullUserInviteResponse;

public class PullUserInvitesTask extends PullTask<UserInviteDTO, PullUserInviteResponse> {

	private static final String TAG = PullUserInvitesTask.class.getSimpleName();

	public PullUserInvitesTask(Context context) {
		super(context);
	}

	@Override
	protected String getTableName() {
		return TableName.USER_INVITE;
	}

	@Override
	protected String getServicePath() {
		return ServiceConstants.PULL_USER_INVITES_PATH;
	}

	@Override
	protected String getLoggingTag() {
		return TAG;
	}

	@Override
	protected void processResult(PullUserInviteResponse response) throws SQLException {
		// Updating sync status
		getHelper().updateSyncStatusPullAt(UserInvite.class, response.getSuccess(), response.getPulledAt());

		Set<UserInviteDTO> userInviteDTOs = response.getItemSet();
		for(UserInviteDTO userInviteDTO:userInviteDTOs){
			// We obtain the required parameters for the object creation from the local database
			User user = getHelper().getUser(userInviteDTO.getUserId().longValue());
			Project project = getHelper().getProject(userInviteDTO.getProjectId().longValue());
			InviteStatus inviteStatus = getHelper().getInviteStatus(userInviteDTO.getInviteStatusId().shortValue());

			// We create the new entity and store it into the local database
			UserInvite userInvite = new UserInvite(user, project, inviteStatus, userInviteDTO);
			getHelper().createOrUpdateUserInvite(userInvite);
		}
	}

	@Override
	protected Class<PullUserInviteResponse> getResponseType() {
		return PullUserInviteResponse.class;
	}

}
