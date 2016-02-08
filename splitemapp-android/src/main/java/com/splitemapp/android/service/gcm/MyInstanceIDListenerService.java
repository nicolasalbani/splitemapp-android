package com.splitemapp.android.service.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;
import com.splitemapp.android.service.BaseTask;
import com.splitemapp.android.service.BaseIntentService;

public class MyInstanceIDListenerService extends InstanceIDListenerService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, BaseIntentService.class);
		intent.putExtra(BaseTask.TASK_NAME, GcmRegistrationTask.class.getSimpleName());
		startService(intent);
    }
}