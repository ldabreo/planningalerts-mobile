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

package com.au.planningalerts.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

/**
 * Adapter class for handling common/boiler plate Async task network operations and task listener
 * {@link ITaskListener} notifications.
 * <p>
 * See {@link AsyncTask}
 *
 * @param <Params>
 * @param <Progress>
 * @param <Result>
 */
@SuppressLint("StaticFieldLeak")
public abstract class AsyncNetworkTaskAdapter<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected Context mContext;
    protected ITaskListener<Progress, Result> mListener;
    protected Exception mError = null;  // Network error


    public AsyncNetworkTaskAdapter(Context context, ITaskListener<Progress, Result> listener) {
        this.mContext = context;
        this.mListener = listener;
    }


    @Override
    protected void onPostExecute(Result result) {
        mListener.onPostExecute(result, mError);
        mContext = null;
        mListener = null;
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        mListener.onProgressUpdate(values);
    }


    @Override
    protected void onPreExecute() {
        if (checkConnectivity()) {
            mListener.onPreExecute();
            super.onPreExecute();
        } else {
            cancel(true);
        }
    }

    protected boolean checkConnectivity() {
        //check network connectivity
        ConnectivityManager connectivityManager =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            mError = new Exception("No network access");
            return false;
        }
        return true;
    }

    @Override
    protected void onCancelled(Result result) {
        super.onCancelled(result);
        mListener.onCancelled(result, mError);
        mContext = null;
        mListener = null;
    }
}
