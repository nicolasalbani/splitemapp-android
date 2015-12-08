package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.Set;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.InviteStatus;
import com.splitemapp.commons.domain.Project;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserInvite;
import com.splitemapp.commons.domain.dto.UserInviteDTO;
import com.splitemapp.commons.domain.dto.response.PullUserInviteResponse;

/**
 * Sync Task to pull user_invite table data from the remote DB
 * @author nicolas
 */
public abstract class PullUserInvitesTask extends PullTask<UserInviteDTO, PullUserInviteResponse> {
	
	public PullUserInvitesTask(DatabaseHelper databaseHelper) {
		super(databaseHelper);
	}
	
	@Override
	protected String getLoggingTag() {
		return getClass().getSimpleName();
	}

	@Override
	protected String getTableName(){
		return TableName.USER_INVITE;
	}

	@Override
	protected String getServicePath(){
		return ServiceConstants.PULL_USER_INVITES_PATH;
	}

	@Override
	protected void processResult(PullUserInviteResponse response) throws SQLException {
		// Updating sync status
		databaseHelper.updateSyncStatusPullAt(UserInvite.class, response.getSuccess());

		Set<UserInviteDTO> userInviteDTOs = response.getItemSet();
		for(UserInviteDTO userInviteDTO:userInviteDTOs){
			// We obtain the required parameters for the object creation from the local database
			User user = databaseHelper.getUser(userInviteDTO.getUserId().longValue());
			Project project = databaseHelper.getProject(userInviteDTO.getProjectId().longValue());
			InviteStatus inviteStatus = databaseHelper.getInviteStatus(userInviteDTO.getInviteStatusId().shortValue());

			// We create the new entity and store it into the local database
			UserInvite userInvite = new UserInvite(user, project, inviteStatus, userInviteDTO);
			databaseHelper.createOrUpdateUserInvite(userInvite);
		}
	}

	@Override
	protected Class<PullUserInviteResponse> getResponseType() {
		return PullUserInviteResponse.class;
	}
}
