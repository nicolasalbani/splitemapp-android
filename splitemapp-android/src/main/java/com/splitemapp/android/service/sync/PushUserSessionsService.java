package com.splitemapp.android.service.sync;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.UserSession;
import com.splitemapp.commons.domain.dto.UserSessionDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;

public class PushUserSessionsService extends PushService<UserSessionDTO, Long, PushLongResponse> {

	private static final String TAG = PushUserSessionsService.class.getSimpleName();

	public PushUserSessionsService() {
		super(TAG);
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
	protected List<UserSessionDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
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
