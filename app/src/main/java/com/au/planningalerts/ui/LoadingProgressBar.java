/*
 *
 *  * Copyright (C) 2018 Planning Alerts
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

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.au.planningalerts.R;

/**
 * Transparent progress bar dialog for indicating that background tasks are in progress.
 */
public final class LoadingProgressBar {

    private Dialog mDialog;
    private Context mContext;

    public LoadingProgressBar(Context context, String title) {
        this.mContext = context;
        LayoutInflater inflator = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        @SuppressLint("InflateParams") final View view = inflator.inflate(R.layout.progress_bar, null);
        if (title != null) {
            final TextView tv = (TextView) view.findViewById(R.id.id_title);
            tv.setText(title);
        }

        mDialog = new Dialog(context, R.style.NewDialog);
        mDialog.setContentView(view);
    }


    public Dialog show(boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        mDialog.setCancelable(cancelable);
        mDialog.setOnCancelListener(cancelListener);
        mDialog.show();
        return mDialog;
    }

    public Dialog getDialog() {
        return mDialog;
    }

}