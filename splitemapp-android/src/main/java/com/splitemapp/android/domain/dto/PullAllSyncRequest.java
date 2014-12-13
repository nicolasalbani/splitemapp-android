package com.splitemapp.android.domain.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.splitemapp.commons.constants.ServiceConstants;

@JsonRootName(value = ServiceConstants.PULL_ALL_SYNC_REQUEST_ROOT)
public class PullAllSyncRequest extends com.splitemapp.commons.domain.dto.request.PullAllSyncRequest{
	
}
