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

import android.os.Parcelable;


/**
 * Interface for classes able to interpret QR codes and resolve them into property addresses.
 * A QR code has a URL attached that can be used to find the underlying property address details.
 * <p>
 * Implements the {@Link Parcelable} interface to allow it to be serialized into
 * an Intent and passed to the barcode reader activity.
 */
public interface IPropertyURLResolver extends Parcelable {


    /**
     * Return whether the supplied qrCode can be understood by this class.
     *
     * @param qrCode to be inspected
     * @return true if this class can interpret this particular code, false otherwise
     */
    boolean isResolvable(final String qrCode);


    /**
     * Resolve/extract the underlying address details from the supplied qrCode.
     *
     * @param qrCode property QR code (raw data) scanned by the camera
     * @return an address
     * @throws Exception
     */
    String resolve(final String qrCode) throws Exception;
}
