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

import android.content.Context;

import com.au.planningalerts.server.propertyresolver.IPropertyURLResolver;


/**
 * Async task for handling QR code to property address resolutions.
 * <p>
 * Note that most (if not all) QR codes have a URL attached. Resolution requires the underlying  URL
 * to be opened and (optionally) read from.
 * <p>
 * QR/URL operations occur asynchronously (in the background), from the  main UI loop.
 * <p>
 * QR interactions are managed by the {@link IPropertyURLResolver}  class that handles the underlying
 * network operations and QR discovery activities.
 */
public class PropertyURLResolverTask extends AsyncNetworkTaskAdapter<String, Void, String> {


    // QR resolver
    protected IPropertyURLResolver mServer;


    public PropertyURLResolverTask(Context context, ITaskListener<Void, String> listener, IPropertyURLResolver server) {
        super(context, listener);
        this.mServer = server;
    }


    /**
     * Resolve QR details in a background thread.
     */
    @Override
    protected String doInBackground(String... urls) {

        if (!isCancelled()) {
            try {
                return mServer.resolve(urls[0]);
            } catch (Exception e) {
                // capture for reporting back to server
                mError = e;
                cancel(true);
            }
        }
        return null;
    }
}
