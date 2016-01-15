package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.Set;

import android.content.Context;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserContactData;
import com.splitemapp.commons.domain.dto.UserContactDataDTO;
import com.splitemapp.commons.domain.dto.response.PullUserContactDataResponse;

public class PullUserContactDatasTask extends PullTask<UserContactDataDTO, PullUserContactDataResponse> {

	private static final String TAG = PullUserContactDatasTask.class.getSimpleName();

	public PullUserContactDatasTask(Context context) {
		super(context);
	}

	@Override
	protected String getTableName() {
		return TableName.USER_CONTACT_DATA;
	}

	@Override
	protected String getServicePath() {
		return ServiceConstants.PULL_USER_CONTACT_DATAS_PATH;
	}

	@Override
	protected String getLoggingTag() {
		return TAG;
	}

	@Override
	protected void processResult(PullUserContactDataResponse response) throws SQLException {
		// Updating sync status
		getHelper().updateSyncStatusPullAt(UserContactData.class, response.getSuccess(), response.getPulledAt());

		Set<UserContactDataDTO> userContactDataDTOs = response.getItemSet();
		for(UserContactDataDTO userContactDataDTO:userContactDataDTOs){
			// We obtain the required parameters for the object creation from the local database
			User user = getHelper().getUser(userContactDataDTO.getUserId().longValue());

			// We create the new entity and store it into the local database
			UserContactData userContactData = new UserContactData(user, userContactDataDTO);
			getHelper().createOrUpdateUserContactData(userContactData);
		}
	}

	@Override
	protected Class<PullUserContactDataResponse> getResponseType() {
		return PullUserContactDataResponse.class;
	}

}
