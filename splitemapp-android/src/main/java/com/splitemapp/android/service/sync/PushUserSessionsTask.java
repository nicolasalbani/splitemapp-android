package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.UserSession;
import com.splitemapp.commons.domain.dto.UserSessionDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;

public class PushUserSessionsTask extends PushTask<UserSessionDTO, Long, PushLongResponse> {

	private static final String TAG = PushUserSessionsTask.class.getSimpleName();

	public PushUserSessionsTask(Context context) {
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
