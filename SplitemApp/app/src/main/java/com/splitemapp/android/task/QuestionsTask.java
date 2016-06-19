package com.splitemapp.android.task;

import android.util.Log;

import com.splitemapp.android.dao.DatabaseHelper;
import com.splitemapp.android.utils.NetworkUtils;
import com.splitemapp.commons.constants.ServiceConstants;
import com.splitemapp.commons.domain.dto.QuestionDTO;
import com.splitemapp.commons.domain.dto.request.QuestionsRequest;
import com.splitemapp.commons.domain.dto.response.ServiceResponse;

/**
 * Questions tasks which enables the user to send a question to the remote server
 * @author nicolas
 *
 */
public abstract class QuestionsTask extends BaseAsyncTask<Void, Void, ServiceResponse> {

	String message;
	
	public QuestionsTask(DatabaseHelper databaseHelper, String message){
		super(databaseHelper);
		this.message = message;
	}

	private String getLoggingTag(){
		return getClass().getSimpleName();
	}
	
	@Override
	protected void onPreExecute() {
		executeOnStart();
	}

	@Override
	public ServiceResponse doInBackground(Void... params) {
		try {
			// Creation the QuestionDTO object
			QuestionDTO questionDTO = new QuestionDTO();
			questionDTO.setMessage(message);
			
			// Creating the login request
			QuestionsRequest request = new QuestionsRequest();
			request.setToken(databaseHelper.getCurrentUserSession().getToken());
			request.setQuestionDTO(questionDTO);

			// Calling the rest service and send back the login response
			return NetworkUtils.callRestService(ServiceConstants.QUESTIONS_PATH, request, ServiceResponse.class);
		} catch (Exception e) {
			Log.e(getLoggingTag(), e.getMessage(), e);
		}

		return null;
	}

	@Override
	public void onPostExecute(ServiceResponse response) {
		boolean success = false;

		// Validating the response
		if(response != null){
			success = response.getSuccess();
		} else {
			executeOnFail(ServiceConstants.ERROR_MESSAGE_NETWORK_ERROR);
			return;
		}

		// We show the status toast if it failed
		if(!success){
			executeOnFail(response.getMessage());
		} else {
			// Executing code on success
			executeOnSuccess();
		}
	}
}
