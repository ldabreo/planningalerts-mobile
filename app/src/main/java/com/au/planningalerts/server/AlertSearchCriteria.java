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

package com.au.planningalerts.server;

import com.google.android.gms.maps.model.LatLng;

/**
 * Class for defining the search criteria for fetching Alerts from the planning alerts server.
 * <p>
 * See the {@link IPlanningAlertsServer} interface.
 * <p>
 * Alerts may be searched for by:
 * <ol>
 * <li>Location - defined using a Latitude/Longitude pair</li>
 * <li>Address - defined using a free form address</li>
 * <li>Area - defined using LatLongs of the bottomLeft and ropRight of the area</li>
 * </ol>
 * <p>
 * In addition the criteria can specify the radios of the search (for location or address).
 */
public class AlertSearchCriteria {

    // Max number of alerts returned by server (irrespective of radius)
    public static final int MAX_COUNT = 100;
    // co-ordinates for search
    protected LatLng location;
    protected String address;
    protected LatLng bottomLeft;
    protected LatLng topRight;
    protected int count;
    protected Radius radius;

    public AlertSearchCriteria(String address) {
        this.address = address;
        this.count = MAX_COUNT;
        this.radius = Radius.CLOSE;
    }

    public AlertSearchCriteria(LatLng location) {
        this.location = location;
        this.count = MAX_COUNT;
        this.radius = Radius.CLOSE;
    }


    public AlertSearchCriteria(LatLng bottomLeft, LatLng topRight) {
        this.bottomLeft = bottomLeft;
        this.topRight = topRight;
        this.count = MAX_COUNT;
    }

    public LatLng getLocation() {
        return location;
    }

    public AlertSearchCriteria setLocation(LatLng location) {
        this.location = location;
        return this;
    }

    public int getCount() {
        return count;
    }

    public AlertSearchCriteria setCount(int count) {
        this.count = count;
        return this;
    }

    public Radius getRadius() {
        return radius;
    }

    public AlertSearchCriteria setRadius(Radius radius) {
        this.radius = radius;
        return this;
    }

    public String toQueryParameters(int page) {
        if (location != null) {
            return "page=" + page + "&count=" + count + "&lat=" + location.latitude + "&lng=" + location.longitude + "&radius=" + radius.distance;
        } else if (bottomLeft != null && topRight != null) {
            return "page=" + page + "&count=" + count + "&bottom_left_lat=" + bottomLeft.latitude + "&bottom_left_lng=" + bottomLeft.longitude +
                    "&top_right_lat=" + topRight.latitude + "&top_right_lng=" + topRight.longitude;
        } else if (address != null) {
            return "page=" + page + "&count=" + count + "&address=" + address + "&radius=" + radius.distance;
        }
        return "";
    }

    // Enum for defining the alert search radius from a lat/long centre.
    public enum Radius {
        CLOSE(300),
        NEARBY(800),
        AREA(2000),
        NEIGHBORHOOD(4000);

        private final int distance;   //  in meters

        Radius(int distance) {
            this.distance = distance;
        }
    }
}
