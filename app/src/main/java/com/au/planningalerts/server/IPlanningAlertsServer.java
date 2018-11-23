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

package com.au.planningalerts.server;

import com.google.android.gms.maps.model.LatLng;

/**
 * Interface that defines the Planning Alerts Server API.
 * <p>
 * To search use the {@link AlertSearchCriteria} class.
 */
public interface IPlanningAlertsServer {


    /**
     * Fetch all alerts for the supplied search parameters.
     *
     * @param filter criteria for alert search
     * @return an array of Alerts found.
     * @throws Exception
     */
    Alert[] findAlerts(AlertSearchCriteria filter) throws Exception;

    /**
     * Fetch all alerts for a given location defined by a latlong pair.
     *
     * @param location LatLong
     * @return an array of alerts around/nearby the location
     * @throws Exception
     */
    Alert[] findAlerts(LatLng location) throws Exception;
}
