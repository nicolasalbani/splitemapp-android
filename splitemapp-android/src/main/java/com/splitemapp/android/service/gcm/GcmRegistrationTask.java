package com.splitemapp.android.service.gcm;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.splitemapp.android.service.sync.PushTask;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.User;
import com.splitemapp.commons.domain.UserSession;
import com.splitemapp.commons.domain.dto.UserSessionDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;

public class GcmRegistrationTask extends PushTask<User, UserSessionDTO, Long, PushLongResponse> {

	private static final String TAG = GcmRegistrationTask.class.getSimpleName();

	public GcmRegistrationTask(Context context) {
		super(context);
	}

	@Override
	protected String getLoggingTag() {
		return TAG;
	}

	@Override
	protected String getTableName(){
		return TableName.USER_SESSION;
	}

	@Override
	protected String getServicePath(){
		return ServiceConstants.PUSH_USER_SESSIONS_PATH;
	}

	@Override
	protected List<UserSessionDTO> getRequestItemList() throws SQLException {
		// Obtaining gcm token
		InstanceID instanceID = InstanceID.getInstance(getContext());
		try {
			String gcmToken = instanceID.getToken(ServiceConstants.GCM_DEFAULT_SENDER_ID,
					GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

			Log.i(TAG, "GCM Registration Token: " + gcmToken);

			// Saving the GCM token into the database
			getHelper().setGcmToken(gcmToken);
		} catch (IOException e) {
			Log.e(TAG, "IOException while obtaining GCM token!", e);
		}

		// We get the current user session
		UserSession userSession = getHelper().getCurrentUserSession();

		// We add to the user session DTO list the one matching the current session 
		List<UserSessionDTO> userSessionDTOList = new ArrayList<UserSessionDTO>();
		userSessionDTOList.add(new UserSessionDTO(userSession));

		return userSessionDTOList;
	}

	@Override
	protected void processResult(PushLongResponse response) throws SQLException {
		// No need to track UserSession push status since we only send updates, no new records
	}
}
