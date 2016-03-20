package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableField;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.dto.UserContactDataDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;
import com.splitemapp.commons.domain.id.IdReference;
import com.splitemapp.commons.domain.id.IdUpdate;

public class PushUserContactDatasTask extends PushTask<UserContactData, UserContactDataDTO, Long, PushLongResponse> {

	private static final String TAG = PushUserContactDatasTask.class.getSimpleName();

	List<UserContactData> userContactDataList = null;

	public PushUserContactDatasTask(Context context) {
		super(context);
	}

	@Override
	protected String getLoggingTag() {
		return TAG;
	}

	@Override
	protected String getTableName(){
		return TableName.USER_CONTACT_DATA;
	}

	@Override
	protected String getServicePath(){
		return ServiceConstants.PUSH_USER_CONTACT_DATAS_PATH;
	}

	@Override
	protected List<UserContactDataDTO> getRequestItemList() throws SQLException {
		// We get all the project in the database
		// TODO only get the ones marked for push
		userContactDataList = getHelper().getUserContactDataList();

		// We add to the user_contact_data DTO list the ones which were updated after the lastPushSuccessAt date
		// and that they were not updated by someone else
		ArrayList<UserContactDataDTO> userContactDataDTOList = new ArrayList<UserContactDataDTO>();
		for(UserContactData userContactData:userContactDataList){
			if((userContactData.getPushedAt() == null) || userContactData.getUpdatedAt().after(userContactData.getPushedAt())){
				// Adding item to the list
				userContactDataDTOList.add(new UserContactDataDTO(userContactData));
			}
		}
		return userContactDataDTOList;
	}

	@Override
	protected void processResult(PushLongResponse response) throws SQLException {
		// Updating sync status
		getHelper().updateSyncStatusPushAt(UserContactData.class, response.getSuccess(), response.getPushedAt());

		// Updating pushedAt
		for(UserContactData entity:userContactDataList){
			getHelper().updatePushedAt(entity, response.getPushedAt());
		}

		List<IdUpdate<Long>> idUpdateList = response.getIdUpdateList();

		// We create the ID reference list to be updated
		List<IdReference> idReferenceList = new ArrayList<IdReference>();
		idReferenceList.add(new IdReference(TableName.USER_CONTACT_DATA, TableField.USER_CONTACT_DATA_ID));

		//We update all references to this ID
		updateIdReferences(idUpdateList, idReferenceList);
	}
}
