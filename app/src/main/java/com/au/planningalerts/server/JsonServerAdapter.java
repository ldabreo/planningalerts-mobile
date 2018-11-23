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

package com.au.planningalerts.server;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Adapter class for server's that deal with JSON data. Handles the reading of the JSON feed.
 * <p>
 * Subclasses should implement the <code>fromJSON</code> method for deserialising
 * objects from a JSON string.
 *
 * @param <Result> returned Object type
 */
public abstract class JsonServerAdapter<Result> {

    /**
     * @param json data feed
     * @return Onject(s) deserialized from json data feed.
     * @throws Exception
     */
    abstract protected Result fromJSON(String json) throws Exception;

    /**
     * Utility function that onverts the contents of an InputStream to a String
     */
    public String readStream(InputStream stream)
            throws IOException, UnsupportedEncodingException {
        InputStreamReader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        BufferedReader br = new BufferedReader(reader);

        try {
            StringBuilder sb = new StringBuilder();

            String line = null;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();

        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Problem reading data", e);
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return "";
    }

}
