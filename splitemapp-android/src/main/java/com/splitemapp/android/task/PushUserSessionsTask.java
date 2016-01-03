package com.splitemapp.android.task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.constants.TableName;
import com.splitemapp.commons.domain.UserSession;
import com.splitemapp.commons.domain.dto.UserSessionDTO;
import com.splitemapp.commons.domain.dto.response.PushLongResponse;

/**
 * Sync Task to push user session table data to the remote DB
 * @author nicolas
 */
public abstract class PushUserSessionsTask extends PushTask<UserSessionDTO, Long, PushLongResponse> {

	public PushUserSessionsTask(DatabaseHelper databaseHelper) {
		super(databaseHelper);
	}

	@Override
	protected String getLoggingTag() {
		return getClass().getSimpleName();
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
	protected Class<PushLongResponse> getResponseType() {
		return PushLongResponse.class;
	}
	
	@Override
	protected boolean useLastPushSuccessAt() {
		return false;
	}

	@Override
	protected List<UserSessionDTO> getRequestItemList(Date lastPushSuccessAt) throws SQLException {
		// We get the current user session
		UserSession userSession = databaseHelper.getCurrentUserSession();

		// We add to the user session DTO list the one matching the current session 
		List<UserSessionDTO> userSessionDTOList = new ArrayList<UserSessionDTO>();
		userSessionDTOList.add(new UserSessionDTO(userSession));

		return userSessionDTOList;
	}

	@Override
	protected void processResult(PushLongResponse response) throws SQLException {
		// No need to track UserSession push status
	}
}
