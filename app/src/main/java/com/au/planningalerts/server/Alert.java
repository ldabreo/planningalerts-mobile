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

import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Development Alert/Application class/DTO.
 * <p>
 * Captures all data/fields published by the planning alerts server.
 */
public class Alert implements Serializable {


    // Date formatters for alert date fields
    protected static SimpleDateFormat formatter = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
    protected static SimpleDateFormat scrappedFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH);
    protected static SimpleDateFormat receivedFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    // Alert/Application data
    // Each field corresponds to a JSON data element.
    protected String id;
    protected String council_reference;
    protected String address;
    protected String description;
    protected String info_url;
    protected String comment_url;
    protected LatLng location;
    protected Date date_scrapped;
    protected Date date_received;
    protected String on_notice_from;
    protected String on_notice_to;
    protected long no_alerted;
    protected String bearing;
    protected String authority;

    /**
     * Deserialise an alert from a JSON data feed.
     *
     * @param app JSON data
     * @return an Alert
     * @throws Exception
     */
    public static Alert fromJSON(JSONObject app) throws Exception {
        Alert a = new Alert();

        JSONObject application = app.getJSONObject("application");

        a.id = "" + application.getLong("id");
        a.council_reference = application.getString("council_reference");
        a.address = application.getString("address");
        a.description = application.getString("description");
        a.info_url = application.getString("info_url");
        a.comment_url = application.getString("comment_url");
        double lat = application.getDouble("lat");
        double lng = application.getDouble("lng");
        LatLng ll = new LatLng(lat, lng);
        a.location = ll;
        a.date_scrapped = application.isNull("date_scraped") ? null : scrappedFormatter.parse(application.getString("date_scraped"));
        a.date_received = application.isNull("date_received") ? null : receivedFormatter.parse(application.getString("date_received"));
        a.on_notice_from = application.isNull("on_notice_from") ? null : application.getString("on_notice_from");
        a.on_notice_to = application.isNull("on_notice_to") ? null : application.getString("on_notice_to");
        a.no_alerted = application.isNull("no_alerted") ? 0 : application.getLong("no_alerted");
        a.bearing = application.isNull("bearing") ? "" : "" + application.getLong("bearing");
        JSONObject authority = application.getJSONObject("authority");
        a.authority = authority.getString("full_name");

        return a;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCouncil_reference() {
        return council_reference;
    }

    public void setCouncil_reference(String council_reference) {
        this.council_reference = council_reference;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInfo_url() {
        return info_url;
    }

    public void setInfo_url(String info_url) {
        this.info_url = info_url;
    }

    public String getComment_url() {
        return comment_url;
    }

    public void setComment_url(String commenturl) {
        this.comment_url = commenturl;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public Date getDate_scrapped() {
        return date_scrapped;
    }

    public void setDate_scrapped(Date date_scrapped) {
        this.date_scrapped = date_scrapped;
    }

    public Date getDate_received() {
        return date_received;
    }

    public void setDate_received(Date date_received) {
        this.date_received = date_received;
    }

    public String getOn_notice_from() {
        return on_notice_from;
    }

    public void setOn_notice_from(String on_notice_from) {
        this.on_notice_from = on_notice_from;
    }

    public long getNo_alerted() {
        return no_alerted;
    }

    public void setNo_alerted(long no_alerted) {
        this.no_alerted = no_alerted;
    }

    public String getBearing() {
        return bearing;
    }

    public void setBearing(String bearing) {
        this.bearing = bearing;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getOn_notice_to() {
        return on_notice_to;
    }

    public void setOn_notice_to(String on_notice_to) {
        this.on_notice_to = on_notice_to;
    }

    /**
     * Return a short-hand human readable tring that represents the alert.
     *
     * @return
     */
    public String toShortString() {
        return council_reference + " " +
                (String) ((date_received == null) ? "No date" : formatter.format(date_received))
                + "" + description;
    }

    /**
     * Retruns the date the application was submitted or last updayed. Some alerts,
     * depending on teh council have no date info attached.
     *
     * @return the date the alert was filed/received
     */
    public String getDateReceivedString() {

        Date d = date_received;

        if (d == null) {
            d = date_scrapped;
        }
        return (d == null) ? "No date" : formatter.format(d);
    }
}
