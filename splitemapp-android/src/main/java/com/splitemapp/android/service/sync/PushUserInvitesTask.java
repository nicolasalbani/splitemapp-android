package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.UserInvite;
import com.splitemapp.commons.domain.dto.UserInviteDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;
import com.splitemapp.commons.domain.id.IdReference;
import com.splitemapp.commons.domain.id.IdUpdate;

public class PushUserInvitesTask extends PushTask<UserInvite, UserInviteDTO, Long, PushLongResponse> {

	private static final String TAG = PushUserInvitesTask.class.getSimpleName();

	private List<UserInvite> userInviteList = null;

	public PushUserInvitesTask(Context context) {
		super(context);
	}

	@Override
	protected String getLoggingTag() {
		return TAG;
	}

	@Override
	protected String getTableName(){
		return TableName.USER_INVITE;
	}

	@Override
	protected String getServicePath(){
		return ServiceConstants.PUSH_USER_INVITES_PATH;
	}

	@Override
	protected List<UserInviteDTO> getRequestItemList() throws SQLException {
		// We get all the project in the database
		// TODO only get the ones marked for push
		userInviteList = getHelper().getUserInviteList();

		// We add to the user_invite DTO list the ones which were updated after the lastPushSuccessAt date
		// and that they were not updated by someone else
		ArrayList<UserInviteDTO> userInviteDTOList = new ArrayList<UserInviteDTO>();
		for(UserInvite userInvite:userInviteList){
			if(shouldPushEntity(userInvite)){
				// Setting the user that pushes the record
				userInvite.setPushedBy(getHelper().getLoggedUser());
				// Adding item to the list
				userInviteDTOList.add(new UserInviteDTO(userInvite));
			}
		}
		return userInviteDTOList;
	}

	@Override
	protected void processResult(PushLongResponse response) throws SQLException {
		// Updating sync status
		getHelper().updateSyncStatusPushAt(UserInvite.class, response.getSuccess(), response.getPushedAt());
		
		// Updating pushedAt
		for(UserInvite entity:userInviteList){
			getHelper().updatePushedAt(entity, response.getPushedAt());
		}

		List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

		// We create the ID reference list to be updated
		List<IdReference> idReferenceList = new ArrayList<IdReference>();
		idReferenceList.add(new IdReference(TableName.USER_INVITE, TableField.USER_INVITE_ID));

		//We update all references to this ID
		updateIdReferences(idUpdateList, idReferenceList);
	}
}
