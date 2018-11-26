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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.au.planningalerts.qr.BarcodeCaptureActivity;
import com.au.planningalerts.server.Alert;
import com.au.planningalerts.server.AlertSearchCriteria;
import com.au.planningalerts.server.propertyresolver.DomainServer;
import com.au.planningalerts.server.propertyresolver.McGrathServer;
import com.au.planningalerts.server.propertyresolver.PropertyResolverChain;
import com.au.planningalerts.task.GeocodingTask;
import com.au.planningalerts.task.ITaskListener;
import com.au.planningalerts.task.NetworkHeadlessFragment;
import com.au.planningalerts.task.PlanningAlertsTask;
import com.au.planningalerts.task.PropertyURLResolverTask;
import com.au.planningalerts.ui.AlertWindowAdapter;
import com.au.planningalerts.ui.LoadingAnimator;
import com.au.planningalerts.ui.MapMarkerGenerator;
import com.au.planningalerts.ui.SearchSpinnerAdapter;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.Formatter;
import java.util.Locale;

/**
 * Main Activity for showing development application alerts on a Google Map.
 * <p>
 * This app enlists the services of the {@link https://www.planningalerts.org.au/} server , which provides
 * information about development applications (DAs) near you. PlanningAlerts
 * aggregates DA information from Australian local councils and and makes it
 * available over an HTTP/S API.
 * </p>
 * In this app, the user can search for development applications aka alerts by:
 *
 * <ol>
 * <li>Searching at the current location</li>
 * <li>Searching by entering a specific address</li>
 * <li>Scanning a property QR code from a domain property magazine using the device's camera</li>
 * </ol>
 * Alerts are displayed on the map, colour coded according to their date of receipt. The more
 * recent alerts are shown in red and the older ones in fading shades of orange and yellow.
 * Alerts 3 years or older are shown in pale yellow. This allows the user to easily distinguish
 * between fresh/newer applications vs already completed ones. Unfortunately the PlanningAlerts
 * server does not provide information on the application status.
 * </p>
 * This app, although on the surface very simple, co-ordinates several backend network
 * interactions to ensure a consistent and smooth user experience.
 * </p>
 * Backend servers include:
 * <ol>
 * <li>The Google maps server (managed by the Google maps fragment)</li>
 * <li>The Google places API (managed by the google autocomplete fragment)</li>
 * <li>The Google geocoding API for deriving a Latitude/Longitude (LatLng) from an address
 * scanned off a QR code</li>
 * <li>Various property websites, for servicing QR codes</li>
 * <li>The Planning Alerts server itself - for finding DAs</li>
 * </ol>
 * All server URL's are configured under the ../res/values/ folder.
 * <p>
 * Both the planning Alerts server and the Google APIs require keys that must be requested from
 * PlanningAlerts and Google respectively by you. These keys are not stored with the project source
 * code but rather supplied by the build user's gradle.properties file at build time. See the
 * _api xml files and the README for instructions on how to incorporate the server keys into the build.
 * <p>
 * Note that the geo-coding service requires billing to be activated against the key's Google account.
 * </p>
 * No key means your map will be blank.
 * </p>
 * No key means you will not be able to fetch alerts from the PlanningAlerts server.
 * </p>
 * The goal of the server interaction model is to:
 * <ol>
 * <li>Ensure a smooth user experience. No UI freezing.</li>
 * <li>Avoid flooding the planning alerts server with requests. This is achieved by asking
 * the user to explicitly re-issue the search if they move or zoom the map. This is in
 * preference to automatically issuing alert fetch requests in the background whenever the
 * map is moved or zoomed.</li>
 * <li>Minimise the data fetched from PlanningAlerts by capping the number of
 * alerts at 1 page of 100 most recent alerts(the default limit).
 * The user can always fetch more by zooming in.</li>
 * </ol>
 * In practice this means that all server interactions are handled via async tasks, not
 * on the main UI thread (mandated by Android anyway) and only the 100 (most recent) alerts
 * are fetched at a time. The code prevents multiple alert fetches from operating concurrently
 * and only allows the user to issue a new search once the existing search has completed and the
 * map has stopped moving. This allows the map to zoom smoothly to completion.
 * </p>
 * See the <code>server</code> package for backend servers and the <code>task</code> package for
 * the corresponding async tasks. All ongoing tasks are managed via the headless fragment
 * {@link NetworkHeadlessFragment}.
 * <p>
 * Other UI 'features':
 * <ul>
 * <li>The main search dropdown is an amalgamation of a drop-down spinner with custom search icons
 * and the Google auto complete places fragment. The background drawables have been set to give
 * the appearance of a single search widget.
 * </li>
 * <li>The QR reader is a lift from the Android vision samples project with the graphic overlay
 * modified to show a cross-hairs icon to assist with Camera focusing.</li>
 * </ul>
 * </p>
 * A note about QR codes:
 * The app supports searching for alerts near a property by scanning the property's QR code
 * from a magazine. Once scanned, the QR code can be used to derive a URL which eventually
 * points to the address details. Once the address is known it can be geo-coded to derive a location
 * which can then be passed to PlanningAlerts to find the nearby DAs.
 * </p>
 * The catch - a property magazine may contain many different types of QR codes - some used by
 * realestate agents and others by the main property website www.domain.com.au.  It is not possible
 * to know upfront which QR code is being scanned. For this reason the QR server has been
 * implemented as a discovery chain of QR code resolvers, where each resolver is dedicated to resolving
 * a particular QR code type. When a QR code is scanned the code is passed to each resolver in turn
 * until one is found that can handle it. Currently 2 QR types are supported:
 * <ul>
 * <li>Domain QR codes - which map to a domain property or project listing</li>
 * <li>McGrath QR codes - which map to a McGrath property listing or Youtube</li>
 * </ul>
 * Other resolvers may be added as required.
 *
 * </p>
 *
 * @author ldabreo 2018
 *
 * </ol>
 */
@SuppressWarnings({"deprecation", "ResourceType"})
public class MainActivity extends AppCompatActivity {

    // Camera bar code Intent id
    protected static final int RC_BARCODE_CAPTURE = 9001;

    // Headless fragment for managing Async tasks
    protected NetworkHeadlessFragment mNetworkFragment;

    // Main search selection dropdown
    protected Spinner mSearchSpinner;

    // Google auto complete fragment for capturing an address
    protected PlaceAutocompleteFragment mAddressAutoComplete;
    protected EditText mAddressText;

    // Google map for showing alerts
    protected GoogleMap mMap;
    protected MapFragment mMapFragment;

    // animates an in-progress spinning icon during alert fetches
    protected LoadingAnimator mAnimator;

    // Android location provider
    protected FusedLocationProviderClient mFusedLocationClient;

    // Alert info panel
    protected LinearLayout mInfoPanel;

    // Search button on map
    protected Button mSearchHereButton;

    /// QR code scanner activity
    protected CameraActivityListener mMapActivity;
    protected BottomNavigationView mNavBar;
    protected ViewFlipper mNavBarFlipper;
    // QR code resolver chain
    protected PropertyResolverChain mResolverChain;
    // Current location (derived from device)
    protected Location mCurrentLocation = null;
    protected Marker mSelectedMarker = null;
    protected TranslateAnimation mInfoPanelAnimator = null;
    // Set to True if an alert fetch is in progress - used to prevent flooding the server
    // Only one fetch allowed at a time.
    protected boolean loadAlertsPending = false;
    // Callback. On receipt of alerts from server, map them on the map
    protected ITaskListener<Void, Alert[]> mAlertsListener
            = new ITaskListener<Void, Alert[]>() {


        @Override
        public void onPreExecute() {
            mAnimator.startAnimation();
        }

        @Override
        public void onPostExecute(Alert[] result, @Nullable Exception error) {


            // If there is a problem quit silently
            if (error != null) {
                onCancelled(result, error);
                return;
            }
            // Check there are alerts to display...
            if (result.length == 0) {
                mAnimator.endAnimation();
                showInfo("No alerts found");
                return;
            }

            // Place alert markers on the map, once for each alert
            try {
                Alert[] alerts = result;

                for (Alert a : alerts) {
                    MarkerOptions o = MapMarkerGenerator.createMarkerOptions(a);
                    Marker m = mMap.addMarker(o);
                    // Add alert to marker
                    m.setTag(a);
                }

            } catch (Exception e) {
                //Log.e(getClass().getSimpleName(), "Problem reading alerts", e);

                //notify user
            } finally {
                mAnimator.endAnimation();
            }
        }

        @Override
        public void onProgressUpdate(Void... values) {
            // No partial progress supported by server....
        }

        @Override
        public void onCancelled(Alert[] alerts, @Nullable Exception error) {
            mAnimator.endAnimation();
            showError("Unable to load alerts", error);
        }
    };
    // Callback. On receipt of a successful geo-coding response
    // load alerts for the LatLong
    protected ITaskListener<Void, LatLng> mGeocodingListener
            = new ITaskListener<Void, LatLng>() {


        @Override
        public void onPreExecute() {
        }

        @Override
        public void onPostExecute(LatLng result, @Nullable Exception error) {
            if (error != null) {
                onCancelled(result, error);
                return;
            }
            loadAlertsForLocation(result);
        }

        @Override
        public void onProgressUpdate(Void... values) {
        }

        @Override
        public void onCancelled(LatLng latLng, @Nullable Exception error) {
            showError("Unable to geocode address", error);
        }
    };
    // Callback. On receipt of QR code scan issue a geocoding request for the address.
    protected ITaskListener<Void, String> mPropertyResolverListener
            = new ITaskListener<Void, String>() {


        @Override
        public void onPreExecute() {
        }

        @Override
        public void onPostExecute(String address, @Nullable Exception error) {

            if (error != null) {
                onCancelled(address, error);
                return;
            }

            mAddressAutoComplete.setText(address);
            GeocodingTask task = new GeocodingTask(getApplicationContext(), mGeocodingListener);
            mNetworkFragment.register(task);
            task.execute(address);
        }

        @Override
        public void onProgressUpdate(Void... values) {

        }

        @Override
        public void onCancelled(String s, @Nullable Exception error) {
            showError("Unable to resolve QR code", error);
        }
    };

    // Callback. Search Drop down/Spinner listener
    protected AdapterView.OnItemSelectedListener mOnSpinnerItemSelectedListener
            = new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            // View can be null on phone re-orientation
            if (view == null) {
                return;
            }
            //get image view to disable spinner text from being shown in top bar
            TextView tv = (TextView) view.findViewById(R.id.searchOptionText);
            tv.setVisibility(View.GONE);

            if (pos == 0) { // User entered address
                // Do nothing address selection is handled by address auto complete fragment
            } else if (pos == 1) { // current location
                // Check for the location permission before accessing the current location.  If the
                // permission is not granted yet, request permission.
                int rc = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                if (rc != PackageManager.PERMISSION_GRANTED) {
                    requestLocationPermission();
                } else {
                    // Load alerts for the current location
                    mAddressAutoComplete.setText("");
                    loadAlertsForLocation(getCurrentLocation());
                }
            } else if (pos == 2) { // Scan domain bar code
                // Initiate a camera activity to scan a QR code.
                mAddressAutoComplete.setText("");
                scanDomainQRCode();
            }
            mSearchSpinner.setSelection(0); // reset every time.
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            //Log.i("dropdown", "nothing selected");
        }
    };
    // Callback. Location services listener.  Set current location from device and load nearby alerts.
    protected OnSuccessListener<Location> mLocationListener = new OnSuccessListener<Location>() {
        @Override
        public void onSuccess(Location location) {
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                mCurrentLocation = location;
                if (loadAlertsPending) {
                    loadAlertsPending = false;
                    loadAlertsForLocation(mCurrentLocation);
                }
            }
        }
    };
    // Callback. Open Planning Alerts home/About/Donate page. URL is attached as data on the View.
    View.OnClickListener mAboutPlanningAlertsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse((String) v.getTag()));
            startActivity(i);
        }
    };
    // Callback. Respond to clicks on a map marker info window (not the marker itself).
    // Open a browser on the Council Website that owns the alert.
    GoogleMap.OnInfoWindowClickListener mInfoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {

        @Override
        public void onInfoWindowClick(Marker marker) {

            if (marker.getTag() == null) {
                return;
            }

            Alert a = (Alert) marker.getTag();
            if (a.getInfo_url() == null) {
                return;
            }
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(a.getInfo_url()));
            startActivity(i);
        }
    };
    // Callback. Flip main view according to NavBar selections.
    BottomNavigationView.OnNavigationItemSelectedListener mNavBarListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            switch (menuItem.getItemId()) {
                case R.id.nav_search:
                    mNavBarFlipper.setDisplayedChild(0);
                    return true;
                case R.id.nav_about:
                    mNavBarFlipper.setDisplayedChild(1);
                    return true;
                case R.id.nav_raisealert:
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(getResources().getString(R.string.planningalerts_alert_url)));
                    startActivity(i);
                    return true;
            }
            return false;

        }
    };

    ;
    // Callback. Respond to a Map marker click to show Info Window and Alert information on
    // a panel
    GoogleMap.OnMarkerClickListener mOnClickMarkerListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            showInfoPanel(marker);
            return true;
        }
    };
    // Callback. Respond to a search button click to issue a new alert search
    View.OnClickListener mSearchHereListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            final LatLngBounds curScreen = mMap.getProjection().getVisibleRegion().latLngBounds;
            mAddressAutoComplete.setText("");
            loadAlertsForArea(curScreen.southwest, curScreen.northeast);
        }
    };
    // Callback. Respond to autocomplete fragment address completions. Load alerts
    // around the selected address
    PlaceSelectionListener mAddressListener = new PlaceSelectionListener() {
        @Override
        public void onPlaceSelected(Place place) {

            if (place.getAddress() == null) {
                return;
            }
            LatLng ll = place.getLatLng();
            loadAlertsForLocation(ll);
        }

        @Override
        public void onError(Status status) {

        }
    };

    /**
     * Clear the display.
     */
    protected void resetDisplay() {
        mSearchSpinner.setSelection(0); // reset
        mSearchHereButton.setVisibility(View.INVISIBLE); // make search here button invivible
        mMap.clear(); // clear map
        closeInfoPanel();
        mSelectedMarker = null;
    }

    /**
     * Load alerts for an area defined by LatLong co-ordinates.
     *
     * @param leftBotton
     * @param rightTop
     */
    protected void loadAlertsForArea(LatLng leftBotton, LatLng rightTop) {
        resetDisplay();

        AlertSearchCriteria filter = new AlertSearchCriteria(leftBotton, rightTop);
        PlanningAlertsTask task = new PlanningAlertsTask(getApplicationContext(), mAlertsListener);
        mNetworkFragment.register(task);
        task.execute(filter);
    }

    /**
     * Load alerts for a specific location.
     *
     * @param l the location.
     */
    protected void loadAlertsForLocation(Location l) {
        if (l == null) {
            return;
        }

        final LatLng ll = new LatLng(l.getLatitude(), l.getLongitude());
        loadAlertsForLocation(ll);
    }

    /**
     * Load alerts for a specific LatLong location.
     *
     * @param ll
     */
    protected void loadAlertsForLocation(final LatLng ll) {

        // Clear the display ready for the search...
        resetDisplay();

        // Execute load alerts after camera is idle to allow zoom to complete smoothly
        // Setup runnable
        Handler handler = new Handler();
        Runnable r = new Runnable() {
            public void run() {
                AlertSearchCriteria filter = new AlertSearchCriteria(ll);
                PlanningAlertsTask task = new PlanningAlertsTask(getApplicationContext(), mAlertsListener);
                // Sleep for one sec to allow zoom to complete
                mNetworkFragment.register(task);
                task.execute(filter);
            }
        };
        // Notify map that a fetch is pending...
        mMapActivity.setLoadAlerts(r);

        //zoom to specified location
        zoomMapToLocation(ll, null);

        // place location maker
        MarkerOptions o = new MarkerOptions();
        o = o.position(ll);
        o = o.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        Marker m = mMap.addMarker(o);
        m.setZIndex(1.0f);

        // Search will be issued once the map has completed zooming (see map listener)
    }

    /**
     * Standard error handler. Shows in snackbar.
     *
     * @param message
     * @param e
     */
    protected void showError(String message, Exception e) {
        String text = message + "." + e.getMessage();
        View v = findViewById(android.R.id.content);


        Snackbar.make(v, text, Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                })
                .show();

    }


    /**
     * Standard info message handler. Short duration.
     *
     * @param message the message to show the user.
     */
    protected void showInfo(String message) {
        View v = findViewById(android.R.id.content);


        Snackbar.make(v, message, Snackbar.LENGTH_SHORT)
                .show();

    }

    /**
     * Standard info message handler. Long duration.
     *
     * @param message the message to show the user.
     */
    protected void showInfoLong(String message) {
        View v = findViewById(android.R.id.content);


        Snackbar.make(v, message, Snackbar.LENGTH_LONG)
                .show();

    }


    /**
     * Launch the camera to scan a QR code. Pass the QR resolvers in so that the barcode activity
     * can vett the QR codes as they are scanned.
     */
    protected void scanDomainQRCode() {

        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
        intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
        intent.putExtra(BarcodeCaptureActivity.Resolver, mResolverChain);

        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }


    /**
     * Camera or browser activity has completed. If it is the camera, then resolve the
     * QR code into an actual property address.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    final Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    PropertyURLResolverTask task = new PropertyURLResolverTask(getApplicationContext(), mPropertyResolverListener, mResolverChain);
                    mNetworkFragment.register(task);
                    task.execute(barcode.displayValue);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * The current location (as set by the device). Requires location services to be enabled.
     *
     * @return the current location
     */
    protected Location getCurrentLocation() {
        return mCurrentLocation;

    }

    /**
     * Zoom map to the specified location.
     *
     * @param latlng
     * @param zoomLevel
     */
    protected void zoomMapToLocation(LatLng latlng, @Nullable Float zoomLevel) {

        CameraUpdate center =
                CameraUpdateFactory.newLatLng(latlng);
        if (zoomLevel == null) {
            zoomLevel = 17.0f; //This goes up to 21
        }
        // with animated effects
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoomLevel));
    }

    /**
     * Show the alert marker panel. Populate panel with alert details.
     *
     * @param marker
     */
    protected void showInfoPanel(Marker marker) {

        if (marker.getTag() == null) {
            return;
        }
        final Alert a = (Alert) marker.getTag();

        selectMarker(marker, a);

        // Populate fields

        // Set alert address
        TextView address = (TextView) mInfoPanel.findViewById(R.id.alert_address);
        address.setText(a.getAddress());


        // Set description of development
        TextView desc = (TextView) mInfoPanel.findViewById(R.id.alert_description);
        desc.setText(a.getDescription());

        // Set date filed
        TextView date = (TextView) mInfoPanel.findViewById(R.id.alert_date);
        date.setText(a.getDateReceivedString());

        // Set council authority details
        TextView authority = (TextView) mInfoPanel.findViewById(R.id.alert_authority);
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.ENGLISH);
        formatter.format(getResources().getString(R.string.alert_ref), a.getAuthority(), a.getCouncil_reference());
        authority.setText(sb.toString());


        showInfoPanel();


    }

    /**
     * Show the info window for map marker.
     *
     * @param marker
     * @param a
     */
    protected void selectMarker(Marker marker, Alert a) {
        clearSelectedMarker();
        // Highlight marker
        mSelectedMarker = marker;
        mSelectedMarker.showInfoWindow();
    }

    /**
     * Clear selected marker. Close the info window.
     */
    protected void clearSelectedMarker() {
        if (mSelectedMarker != null) {
            mSelectedMarker.hideInfoWindow();
        }
        mSelectedMarker = null;
    }

    /**
     * Show alert details panel
     */
    protected void showInfoPanel() {

        if (mInfoPanel.getVisibility() != View.VISIBLE) {

            final float yDelta = mInfoPanel.getHeight();


            mInfoPanel.setVisibility(View.VISIBLE);

            // Animate slide-up
            mInfoPanelAnimator = new TranslateAnimation(
                    0,                 // fromXDelta
                    0,                 // toXDelta
                    0,  // fromYDelta
                    -yDelta
            );                // toYDelta
            mInfoPanelAnimator.setDuration(500);
            mInfoPanelAnimator.setFillAfter(true);


            mInfoPanel.startAnimation(mInfoPanelAnimator);
        }

    }

    /**
     * Close alert details panel
     */
    protected void closeInfoPanel() {

        if (mInfoPanel.getVisibility() == View.VISIBLE) {

            final float yDelta = mInfoPanel.getHeight();

            // Animate slide-down
            mInfoPanelAnimator = new TranslateAnimation(
                    0,                 // fromXDelta
                    0,                 // toXDelta
                    -yDelta,  // fromYDelta
                    0
            );                // toYDelta
            mInfoPanelAnimator.setDuration(500);
            mInfoPanelAnimator.setFillAfter(false);

            mInfoPanel.startAnimation(mInfoPanelAnimator);
            mInfoPanel.setVisibility(View.INVISIBLE);
        }


    }

    /**
     * Handles the requesting of the location permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    protected void requestLocationPermission() {
        //Log.w(this.getClass().getSimpleName(), "Location permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions,
                2);
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     *
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != 2) {
            //Log.d(getClass().getSimpleName(), "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Log.d(getClass().getSimpleName(), "Location permission granted -load alerts");
            loadAlertsPending = true;
            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, mLocationListener);
            return;
        }

        //Log.e(getClass().getSimpleName(), "Permission not granted: results len = " + grantResults.length +
        // " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
    }

    /**
     * The main create method for the Activity. Initialises all UI views and components.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mNetworkFragment = NetworkHeadlessFragment.getInstance(getSupportFragmentManager());

        mAddressAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        mAddressText = ((EditText) mAddressAutoComplete.getView().findViewById(R.id.place_autocomplete_search_input));
        mAddressText.setTextSize(14.0f);
        mAddressText.setBackgroundResource(R.drawable.rounded_recktangle);
        View v = mAddressAutoComplete.getView().findViewById(R.id.place_autocomplete_clear_button);
        v.setBackgroundResource(R.drawable.rounded_recktangle);
        mAddressAutoComplete.setHint(getResources().getString(R.string.instruction_hint));

        ((View) mAddressAutoComplete.getView().findViewById(R.id.place_autocomplete_search_button)).setVisibility(View.GONE);


        // Narrow search to AUS only
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setCountry("AU")
                .build();

        mAddressAutoComplete.setFilter(typeFilter);
        mAddressAutoComplete.setOnPlaceSelectedListener(mAddressListener);


        mSearchSpinner = (Spinner) findViewById(R.id.searchSpinner);
        mSearchSpinner.setAdapter(new SearchSpinnerAdapter(this, R.layout.search_spinner_row));
        mSearchSpinner.setOnItemSelectedListener(mOnSpinnerItemSelectedListener);


        // listen to map
        mMapActivity = new CameraActivityListener();
        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(mMapActivity);
        mMapFragment.setRetainInstance(true);


        //progress bar
        mAnimator = new LoadingAnimator(this);

        // Current location
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, mLocationListener);


        TextView tv = findViewById(R.id.about_title);
        tv.setTag(getResources().getString(R.string.openfoundation_url));
        tv.setOnClickListener(mAboutPlanningAlertsClickListener);
        tv = findViewById(R.id.about_text);
        tv.setTag(getResources().getString(R.string.openfoundation_url));
        tv.setOnClickListener(mAboutPlanningAlertsClickListener);

        tv = findViewById(R.id.donate_title);
        tv.setTag(getResources().getString(R.string.planningalerts_donate_url));
        tv.setOnClickListener(mAboutPlanningAlertsClickListener);
        tv = findViewById(R.id.donate_text);
        tv.setTag(getResources().getString(R.string.planningalerts_donate_url));
        tv.setOnClickListener(mAboutPlanningAlertsClickListener);


        mNavBar = (BottomNavigationView) findViewById(R.id.navigation);
        mNavBar.setOnNavigationItemSelectedListener(mNavBarListener);

        mNavBarFlipper = (ViewFlipper) findViewById(R.id.myViewFlipper);
        mNavBarFlipper.setInAnimation(this, android.R.anim.slide_in_left);
        mNavBarFlipper.setOutAnimation(this, android.R.anim.slide_out_right);

        mSearchHereButton = (Button) findViewById(R.id.searchHere);
        mSearchHereButton.setOnClickListener(mSearchHereListener);
        mSearchHereButton.setVisibility(View.INVISIBLE);

        mInfoPanel = (LinearLayout) findViewById(R.id.infoPanel);
        mInfoPanel.setVisibility(View.INVISIBLE);

        TextView version = findViewById(R.id.versionDetailsNumber);
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version.setText(pInfo.versionName);
        } catch (Exception e) {
        }

        // Create Property resolver chain
        mResolverChain = new PropertyResolverChain()
                .addResolver(new DomainServer(getResources()))
                .addResolver(new McGrathServer(getResources()));


    }

    // Callback. Key class for responding to user map interactions.
    protected class CameraActivityListener extends FragmentActivity implements
            GoogleMap.OnCameraMoveStartedListener,
            GoogleMap.OnCameraMoveListener,
            GoogleMap.OnCameraMoveCanceledListener,
            GoogleMap.OnCameraIdleListener,
            OnMapReadyCallback {

        protected boolean showButton = false;
        protected Runnable loadAlerts;

        Handler handler = new Handler();

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mMap.setOnCameraIdleListener(this);
            mMap.setOnCameraMoveStartedListener(this);
            mMap.setOnCameraMoveListener(this);
            mMap.setOnCameraMoveCanceledListener(this);
            mMap.setOnMarkerClickListener(mOnClickMarkerListener);
            mMap.setInfoWindowAdapter(new AlertWindowAdapter(MainActivity.this));
            mMap.setOnInfoWindowClickListener(mInfoWindowClickListener);
        }


        public Runnable getLoadAlerts() {
            return loadAlerts;
        }

        public void setLoadAlerts(Runnable loadAlerts) {
            this.loadAlerts = loadAlerts;
        }

        @Override
        public void onCameraIdle() {
            // User has moved map, show loadAlerts button...
            if (showButton) {
                mSearchHereButton.setVisibility(View.VISIBLE);

                clearSelectedMarker();
                closeInfoPanel();
            }
            ;

            // Remove any current handler and restart button fade timer (stops button flickering as events
            // cascade on map movement.
            // Show button for 4 seconds before fading
            handler.removeCallbacksAndMessages(null);
            handler = new Handler();

            Runnable r = new Runnable() {
                public void run() {
                    mSearchHereButton.setVisibility(View.INVISIBLE);
                }
            };

            handler.postDelayed(r, 4000);

            // Camera is now still  and should be centred at the user's selected location.
            // Is there an alerts load outstanding? If so execute it
            if (loadAlerts != null) {
                Handler h = new Handler();
                h.postDelayed(loadAlerts, 100);
                loadAlerts = null;
            }
        }

        @Override
        public void onCameraMoveCanceled() {
        }

        @Override
        public void onCameraMove() {
        }

        @Override
        public void onCameraMoveStarted(int reason) {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                showButton = true;
            } else if (reason == GoogleMap.OnCameraMoveStartedListener
                    .REASON_API_ANIMATION) {
                showButton = false;
            } else if (reason == GoogleMap.OnCameraMoveStartedListener
                    .REASON_DEVELOPER_ANIMATION) {
                showButton = false;
            }
        }
    }


}
