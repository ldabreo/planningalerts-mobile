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

package com.au.planningalerts.task;

import android.content.Context;
import android.content.res.Resources;

import com.au.planningalerts.server.Alert;
import com.au.planningalerts.server.AlertSearchCriteria;
import com.au.planningalerts.server.IPlanningAlertsServer;
import com.au.planningalerts.server.PlanningAlertsServer;

/**
 * Async task for handling planning server interactions.
 * <p>
 * Fetches from Planning Alerts occur asynchronously (in the background), from the  main UI loop.
 * <p>
 * Interactions are managed by the {@link IPlanningAlertsServer} server that handles the underlying
 * network operations.
 */
public class PlanningAlertsTask extends AsyncNetworkTaskAdapter<AlertSearchCriteria, Void, Alert[]> {


    // Planning alerts server
    protected IPlanningAlertsServer mServer;


    public PlanningAlertsTask(Context context, ITaskListener<Void, Alert[]> listener) {
        super(context, listener);
        this.mServer = createServer(context.getResources());
    }


    /**
     * Fetch alerts from the server in the background.
     *
     * @param filters
     * @return
     */
    @Override
    protected Alert[] doInBackground(AlertSearchCriteria... filters) {

        if (!isCancelled()) {
            try {
                return mServer.findAlerts(filters[0]);
            } catch (Exception e) {
                // Capture for reporting back to caller
                mError = e;
            }
        }
        return null;
    }

    /**
     * One-liner to facilitate mocking.
     *
     * @param resource - app resources
     * @return a IPlanningAlertsServer instance.
     */
    public IPlanningAlertsServer createServer(Resources resource) {
        return new PlanningAlertsServer(resource);
    }

}
