/*
 *
 *  * Copyright (C) 2018 L.D'Abreo
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.au.planningalerts.ui;

import android.app.Activity;


/**
 * Animator for a animating a spinning progress icon to indicate a background task is occurring.
 * <p>
 * Overlays over parent activity. Progress bar is shown using a dia//Log.
 * See {@link LoadingProgressBar} dia//Log.
 */
public class LoadingAnimator {


    // parent actiivity
    protected Activity mActivity;

    // Graphic spinning overlay
    protected LoadingProgressBar mDialog;

    public LoadingAnimator(Activity activity) {
        mActivity = activity;
        mDialog = new LoadingProgressBar(mActivity, null);
    }

    public void startAnimation() {
        mDialog.show(true, null);
    }

    public void endAnimation() {

        if (!mActivity.isFinishing() && mDialog.getDialog().isShowing()) {
            mDialog.getDialog().dismiss();
        }
    }
}