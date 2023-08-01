/*
 * Copyright (C) 2017-2021 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.setupwizard;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import static org.lineageos.setupwizard.SetupWizardApp.LOGV;

import android.annotation.Nullable;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;

import org.lineageos.setupwizard.util.PhoneMonitor;
import org.lineageos.setupwizard.util.SetupWizardUtils;
import java.util.List;

public class SetupWizardExitActivity extends BaseSetupWizardActivity {

    private static final String TAG = SetupWizardExitActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLauncher();

        if (LOGV) {
            Log.v(TAG, "onCreate savedInstanceState=" + savedInstanceState);
        }
        if (SetupWizardUtils.isOwner()) {
            SetupWizardUtils.enableCaptivePortalDetection(this);
        }
        PhoneMonitor.onSetupFinished();
        if (!SetupWizardUtils.isManagedProfile(this)) {
            launchHome();
        }
        finish();
        applyForwardTransition(TRANSITION_ID_FADE);
        Intent i = new Intent();
        i.setClassName(getPackageName(), SetupWizardExitService.class.getName());
        startService(i);
    }

    private void launchHome() {
        startActivity(new Intent("android.intent.action.MAIN")
                .addCategory("android.intent.category.HOME")
                .addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK));
    }

    public void initLauncher (){
		String ORIGINAL_LAUNCHER_PACKAGENAME = "com.okcaros.launcher";
    	String ORIGINAL_LAUNCHER_CLASSNAME = "app.lawnchair.LawnchairLauncher";
        Intent queryIntent = new Intent();
		final PackageManager mPm = this.getPackageManager();
        queryIntent.addCategory(Intent.CATEGORY_HOME);
        queryIntent.setAction(Intent.ACTION_MAIN);

        List<ResolveInfo> homeActivities = mPm.queryIntentActivities(queryIntent, 0);
        if(homeActivities == null) {
            return;
        }

        ComponentName defaultLauncher = new ComponentName(ORIGINAL_LAUNCHER_PACKAGENAME,
                ORIGINAL_LAUNCHER_CLASSNAME);
        int activityNum = homeActivities.size();
        ComponentName[] set = new ComponentName[activityNum];
        int defaultMatch = -1;
        for(int i = 0; i < activityNum; i++){
            ResolveInfo info = homeActivities.get(i);
            set[i] = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
            if(ORIGINAL_LAUNCHER_CLASSNAME.equals(info.activityInfo.name)
                    && ORIGINAL_LAUNCHER_PACKAGENAME.equals(info.activityInfo.packageName)){
                defaultMatch = info.match;
            }
        }

        //if Launcher is not found, do not set anything
        if(defaultMatch == -1){
            return ;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        mPm.addPreferredActivity(filter, defaultMatch, set, defaultLauncher);
    }
}
