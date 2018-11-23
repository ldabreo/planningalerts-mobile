/*
 * Copyright 2018 L.D'Abreo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.au.planningalerts.task;

import android.content.Context;
import android.content.res.Resources;

import com.au.planningalerts.server.GoogleMapsServer;
import com.au.planningalerts.server.IGoogleMapsServer;
import com.google.android.gms.maps.model.LatLng;

/**
 * Async task for handling Google geocoding functions.
 * <p>
 * Geocoding operations occur asynchronously (in the background), from the  main UI loop.
 * <p>
 * Interactions are managed by the {@link IGoogleMapsServer} server that handles the underlying
 * network operations.
 */
public class GeocodingTask extends AsyncNetworkTaskAdapter<String, Void, LatLng> {

    // Google geocoding server
    protected IGoogleMapsServer mServer;

    public GeocodingTask(Context context, ITaskListener<Void, LatLng> listener) {
        super(context, listener);
        this.mServer = createServer(context.getResources());
        this.mListener = listener;
    }

    /**
     * One-liner to facilitate mocking.
     *
     * @param resource - app resources
     * @return a IGoogleMapsServer instance.
     */
    public IGoogleMapsServer createServer(Resources resource) {
        return new GoogleMapsServer(resource);
    }

    /**
     * Issue a geocoding request to google in the background.
     *
     * @param addresses
     * @return
     */
    @Override
    protected LatLng doInBackground(String... addresses) {

        if (!isCancelled()) {
            try {
                return mServer.toLocation(addresses[0]);
            } catch (Exception e) {
                // Capture error so it can be reported back to caller...
                mError = e;
            }
        }
        return null;
    }

}
