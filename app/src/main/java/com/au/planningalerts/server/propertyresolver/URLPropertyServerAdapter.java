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

package com.au.planningalerts.server.propertyresolver;

import android.content.res.Resources;
import android.os.Parcel;
import androidx.annotation.Nullable;

import com.au.planningalerts.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapter class that encapsulates common URL handling routines for property URL resolver subclasses, including
 * reading from a URL and following redirects e.g http to https.
 * <p>
 * Implements the {@link IPropertyURLResolver} interface.
 * <p>
 * Subclasses should implement the <code>resolve()</code> method.
 */
public abstract class URLPropertyServerAdapter implements IPropertyURLResolver {


    protected int mConnectTimeout;
    protected int mReadTimeout;
    protected String mCodeRegexp;


    public URLPropertyServerAdapter(Parcel in) {
        mConnectTimeout = in.readInt();
        mReadTimeout = in.readInt();
        mCodeRegexp = in.readString();
    }

    ;


    public URLPropertyServerAdapter(Resources resources) {
        mConnectTimeout = Integer.parseInt(resources.getString(R.string.server_connecttimeout));
        mReadTimeout = Integer.parseInt(resources.getString(R.string.server_readtimeout));
    }

    /**
     * The code is able to be interpreted if it matches the
     * RegExp expression.
     *
     * @param qrCode to be inspected
     * @return true if the qrCode can be resolved, false otherwise.
     */
    @Override
    public boolean isResolvable(String qrCode) {
        return (qrCode != null && qrCode.matches(mCodeRegexp));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(mConnectTimeout);
        dest.writeInt(mReadTimeout);
        dest.writeString(mCodeRegexp);
    }

    @Override
    public int describeContents() {
        return 0;
    }


    /**
     * Read the contents of an HTML page.
     *
     * @param con
     * @return HTML text.
     * @throws Exception
     */
    public String readURL(HttpURLConnection con) throws Exception {
        BufferedReader in = null;
        StringBuffer contents = new StringBuffer("");
        try {
            in = new BufferedReader(
                    new InputStreamReader(
                            con.getInputStream()));

            String inputLine;

            while ((inputLine = in.readLine()) != null)
                contents.append(inputLine);

        } catch (Exception e) {
            throw e;
        } finally {

            // Close always
            if (in != null) {
                in.close();
            }
        }

        return contents.toString();


    }

    /**
     * Utility function to extract a String from a String using a RegExp.
     *
     * @param src    Source String.
     * @param regexp extraction RegExp.
     * @return extracted token.
     */
    protected String extractString(String src, String regexp) {

        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(src);

        if (matcher.find()) {
            String address = matcher.group(1);
            return address;
        }
        return "";
    }

    /**
     * Made a one-liner to assist with URL mocking.
     *
     * @param urlString the URL strring
     * @return a URL
     * @throws MalformedURLException
     */
    public URL openURL(String urlString) throws MalformedURLException {
        return new URL(urlString);
    }

    /**
     * Made a one-liner to assist with URL mocking.
     *
     * @param context base
     * @param spec    tail
     * @return a URL
     * @throws MalformedURLException
     */
    public URL openUrlWithContext(URL context, String spec) throws MalformedURLException {
        return new URL(context, spec);
    }

    /**
     * Utility method to read the contents of a URL and follow redirects.
     *
     * @param urlString     target URL.
     * @param validorRegexp expression to validate the supplied URL.
     * @param readContents  if true the page details will be read. If false the contents are not read.
     * @return the result of rfollowing and eading from the URL.
     * @throws Exception
     */
    public URLResult resolveURL(final String urlString, @Nullable String validorRegexp, boolean readContents) throws Exception {
        HttpURLConnection conn = null;
        String url = urlString;
        URL resourceURL;
        URL base = null;
        URL next = null;
        String location = null;


        // Traverse all URL redirects (common with QR urls) to final URL
        try {

            while (true) {
                resourceURL = openURL(url);

                // Close previous connection
                if (conn != null) {
                    conn.disconnect();
                }
                conn = (HttpURLConnection) resourceURL.openConnection();

                conn.setConnectTimeout(mConnectTimeout);
                conn.setReadTimeout(mReadTimeout);
                conn.setInstanceFollowRedirects(false);


                switch (conn.getResponseCode()) {
                    case HttpURLConnection.HTTP_MOVED_PERM:
                    case HttpURLConnection.HTTP_MOVED_TEMP:
                        location = conn.getHeaderField("Location");
                        location = URLDecoder.decode(location, "UTF-8");
                        base = openURL(url);
                        next = openUrlWithContext(base, location);  // Deal with relative URLs
                        url = next.toExternalForm();
                        continue;
                }

                break;
            } // end while

            // check final url is valid
            if (isValidUrl(url, validorRegexp)) {
                throw new Exception("Unsupported QR code");
            }
            String contents = (readContents) ? readURL(conn) : "";

            return new URLResult(url, contents);


        } catch (Exception e) {
            //Log.e(getClass().getSimpleName(), "Problem parsing url:" + urlString, e);
            throw e;
        } finally {

            if (conn != null) {
                conn.disconnect();
            }
        }

    }

    /**
     * @param url    to test
     * @param regExp regExp to use to validate url
     * @return true if url passes RegExp, false otherwise.
     * @throws Exception
     */
    protected boolean isValidUrl(String url, String regExp) throws Exception {
        return (regExp != null && !url.matches(regExp));
    }

    // Class to capture the result of reading from a URL.
    protected class URLResult {

        public String url;
        public String contents;

        public URLResult(String url, String contents) {
            this.url = url;
            this.contents = contents;
        }
    }
}
