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

package com.au.planningalerts;

import android.content.Context;
import android.content.res.Resources;

import com.au.planningalerts.server.GoogleMapsServer;
import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GoogleMapsServerTest {

    @Mock
    Context mMockContext;

    @Mock
    Resources mMockResources;


    @Test
    public void testGeocodingApi() throws Exception {


        when(mMockResources.getString(R.string.google_geocoding_connecttimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.google_geocoding_readtimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.google_geocoding_url)).thenReturn("https://googlemaps");
        when(mMockContext.getResources()).thenReturn(mMockResources);


        InputStream in = this.getClass().getClassLoader().getResourceAsStream("GoogleGeocodingPayload.json");

        // Mock URL
        final URL url = mock(URL.class);
        HttpsURLConnection con = mock(HttpsURLConnection.class);
        when(url.openConnection()).thenReturn(con);
        when(con.getResponseCode()).thenReturn(200);
        when(con.getInputStream()).thenReturn(in);

        GoogleMapsServer server = new GoogleMapsServer(mMockContext.getResources()) {
            public URL openURL(String urlString) throws MalformedURLException {
                return url;
            }
        };


        LatLng ll = server.toLocation("hello");

        assertThat(ll.latitude).isEqualTo(-33.821413);
        assertThat(ll.longitude).isEqualTo(151.1978822);


    }

    @Test
    public void testGeocodingApiWithError() throws Exception {


        when(mMockResources.getString(R.string.google_geocoding_connecttimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.google_geocoding_readtimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.google_geocoding_url)).thenReturn("https://googlemaps");
        when(mMockContext.getResources()).thenReturn(mMockResources);


        InputStream in = this.getClass().getClassLoader().getResourceAsStream("GoogleGeocodingErrorPayload.json");

        // Mock URL
        final URL url = mock(URL.class);
        HttpsURLConnection con = mock(HttpsURLConnection.class);
        when(url.openConnection()).thenReturn(con);
        when(con.getResponseCode()).thenReturn(200);
        when(con.getInputStream()).thenReturn(in);

        GoogleMapsServer server = new GoogleMapsServer(mMockContext.getResources()) {
            public URL openURL(String urlString) throws MalformedURLException {
                return url;
            }
        };


        try {

            LatLng ll = server.toLocation("hello");

        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("Keyless access to Google Maps Platform is deprecated. Please use an API key with all your API calls to avoid service interruption. For further details please refer to http://g.co/dev/maps-no-account");
        }


    }


    @Test
    public void testGeocodingApiWithNoResults() throws Exception {


        when(mMockResources.getString(R.string.google_geocoding_connecttimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.google_geocoding_readtimeout)).thenReturn("1000");
        when(mMockResources.getString(R.string.google_geocoding_url)).thenReturn("https://googlemaps");
        when(mMockContext.getResources()).thenReturn(mMockResources);


        InputStream in = this.getClass().getClassLoader().getResourceAsStream("GoogleGeocodingZeroResults.json");

        // Mock URL
        final URL url = mock(URL.class);
        HttpsURLConnection con = mock(HttpsURLConnection.class);
        when(url.openConnection()).thenReturn(con);
        when(con.getResponseCode()).thenReturn(200);
        when(con.getInputStream()).thenReturn(in);

        GoogleMapsServer server = new GoogleMapsServer(mMockContext.getResources()) {
            public URL openURL(String urlString) throws MalformedURLException {
                return url;
            }
        };


        try {

            LatLng ll = server.toLocation("hello");

        } catch (Exception e) {

            assertThat(e.getMessage()).isEqualTo("Unable to geocode");
        }


    }


}
