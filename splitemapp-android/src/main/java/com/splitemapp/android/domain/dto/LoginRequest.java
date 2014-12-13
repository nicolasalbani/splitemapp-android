package com.splitemapp.android.domain.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.splitemapp.commons.constants.ServiceConstants;

@JsonRootName(value = ServiceConstants.LOGIN_REQUEST_ROOT)
public class LoginRequest extends com.splitemapp.commons.domain.dto.request.LoginRequest{
	
}
