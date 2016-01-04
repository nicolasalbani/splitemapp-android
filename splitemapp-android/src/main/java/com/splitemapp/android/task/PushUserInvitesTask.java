package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.UserInvite;
import com.splitemapp.commons.domain.dto.UserInviteDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;
import com.splitemapp.commons.domain.id.IdReference;
import com.splitemapp.commons.domain.id.IdUpdate;

/**
 * Sync Task to push user_invite table data to the remote DB
 * @author nicolas
 */
public abstract class PushUserInvitesTask extends PushTask<UserInviteDTO, Long, PushLongResponse> {
	
	private List<UserInvite> userInviteList = null;
	
	public PushUserInvitesTask(DatabaseHelper databaseHelper) {
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
		return ServiceConstants.PUSH_USER_INVITES_PATH;
	}

	@Override
	protected List<UserInviteDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
		// We get all the project in the database
		// TODO only get the ones marked for push
		userInviteList = databaseHelper.getUserInviteList();

		// We add to the user_invite DTO list the ones which were updated after the lastPushSuccessAt date 
		ArrayList<UserInviteDTO> userInviteDTOList = new ArrayList<UserInviteDTO>();
		for(UserInvite userInvite:userInviteList){
			if(userInvite.getUpdatedAt().after(lastPushSuccessAt)){
				// Adding item to the list
				userInviteDTOList.add(new UserInviteDTO(userInvite));
			}
		}
		return userInviteDTOList;
	}

	@Override
	protected void processResult(PushLongResponse response) throws SQLException {
		// Updating sync status
		databaseHelper.updateSyncStatusPushAt(UserInvite.class, response.getSuccess(), response.getPushedAt());
		
		// Updating pushedAt
		for(UserInvite entity:userInviteList){
			databaseHelper.updatePushedAt(entity, response.getPushedAt());
		}

		List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

		// We create the ID reference list to be updated
		List<IdReference> idReferenceList = new ArrayList<IdReference>();
		idReferenceList.add(new IdReference(TableName.USER_INVITE, TableField.USER_INVITE_ID));

		//We update all references to this ID
		for(IdUpdate<Long> idUpdate:idUpdateList){
			databaseHelper.updateIdReferences(idUpdate, idReferenceList);
		}
	}
}
