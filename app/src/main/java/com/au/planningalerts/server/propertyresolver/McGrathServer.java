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

package com.au.planningalerts.server.propertyresolver;

import android.content.res.Resources;
import android.os.Parcel;

import com.au.planningalerts.R;


/**
 * Class for interpreting McGrath realestate magazine URLs attached to QR codes
 * on Mcgrath property photos.
 * <p>
 * Some URLS point to a McGrath property listing whereas others redirect to youtube.
 * <p>
 * Implements the {@Link Parcelable} interface to allow it to be serialized into
 * an Intent and passed to the barcode reader activity.
 * <p>
 * See {@link IPropertyURLResolver} for the URL resolver contract.
 */
public class McGrathServer extends URLPropertyServerAdapter {


    public static final Creator<McGrathServer> CREATOR
            = new Creator<McGrathServer>() {
        public McGrathServer createFromParcel(Parcel in) {
            return new McGrathServer(in);
        }

        public McGrathServer[] newArray(int size) {
            return new McGrathServer[size];
        }
    };

    // Regexp for checking the redirected URL is one this class can understand
    protected String mRedirectedUrlRegexp;

    // RegExp for extracting/scrapping an address from a McGrath HTML page.
    protected String mAddressRegExp;


    public McGrathServer(Parcel in) {
        super(in);
        mRedirectedUrlRegexp = in.readString();
        mAddressRegExp = in.readString();
    }

    public McGrathServer(Resources resources) {
        super(resources);
        mCodeRegexp = resources.getString(R.string.mcgrath_valid_qr_code);
        mRedirectedUrlRegexp = resources.getString(R.string.mcgrath_valid_url);
        mAddressRegExp = resources.getString(R.string.mcgrath_address_regexp);
    }

    /**
     * Inspect the McGrath URL and either extract the underlying property from the HTML page.
     *
     * @param qrMcgrathCode from a domain mag
     * @return an address reverse engineered from the domain URL
     * @throws Exception
     */
    @Override
    public String resolve(final String qrMcgrathCode) throws Exception {


        URLResult result = resolveURL(qrMcgrathCode, mRedirectedUrlRegexp, true);

        String address = extractString(result.contents, mAddressRegExp);
        return address;

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mRedirectedUrlRegexp);
        dest.writeString(mAddressRegExp);
    }

}
