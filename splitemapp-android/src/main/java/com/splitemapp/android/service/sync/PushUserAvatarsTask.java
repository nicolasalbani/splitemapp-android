package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.UserAvatar;
import com.splitemapp.commons.domain.dto.UserAvatarDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;
import com.splitemapp.commons.domain.id.IdReference;
import com.splitemapp.commons.domain.id.IdUpdate;

public class PushUserAvatarsTask extends PushTask<UserAvatarDTO, Long, PushLongResponse> {

	private static final String TAG = PushUserAvatarsTask.class.getSimpleName();

	List<UserAvatar> userAvatarList = null;

	public PushUserAvatarsTask(Context context) {
		super(context);
	}
	
	@Override
	protected String getLoggingTag() {
		return TAG;
	}

	@Override
	protected String getTableName(){
		return TableName.USER_AVATAR;
	}

	@Override
	protected String getServicePath(){
		return ServiceConstants.PUSH_USER_AVATARS_PATH;
	}

	@Override
	protected List<UserAvatarDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
		// We get all the project in the database
		// TODO only get the ones marked for push
		userAvatarList = getHelper().getUserAvatarList();
		Long loggedUserId = getHelper().getLoggedUserId();

		// We add to the user_contact_data DTO list the ones which were updated after the lastPushSuccessAt date
		// and that they were not updated by someone else
		ArrayList<UserAvatarDTO> userAvatarDTOList = new ArrayList<UserAvatarDTO>();
		for(UserAvatar userAvatar:userAvatarList){
			if(userAvatar.getUpdatedAt().after(lastPushSuccessAt) && 
					userAvatar.getUser().getId().equals(loggedUserId)  && 
					(userAvatar.getPushedAt() == null || userAvatar.getPushedAt().before(lastPushSuccessAt))){
				// Adding item to the list
				userAvatarDTOList.add(new UserAvatarDTO(userAvatar));
			}
		}
		return userAvatarDTOList;
	}

	@Override
	protected void processResult(PushLongResponse response) throws SQLException {
		// Updating sync status
		getHelper().updateSyncStatusPushAt(UserAvatar.class, response.getSuccess(), response.getPushedAt());
		
		// Updating pushedAt
		for(UserAvatar entity:userAvatarList){
			getHelper().updatePushedAt(entity, response.getPushedAt());
		}

		List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

		// We create the ID reference list to be updated
		List<IdReference> idReferenceList = new ArrayList<IdReference>();
		idReferenceList.add(new IdReference(TableName.USER_CONTACT_DATA, TableField.USER_CONTACT_DATA_ID));

		//We update all references to this ID
		for(IdUpdate<Long> idUpdate:idUpdateList){
			getHelper().updateIdReferences(idUpdate, idReferenceList);
		}
	}
}
