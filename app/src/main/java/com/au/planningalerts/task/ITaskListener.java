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

package com.au.planningalerts.task;

import android.support.annotation.Nullable;

/**
 * Async Task Listener callback interface. Separates the task itself from callback interactions.
 * <p>
 * Implement this interface for classes listening for the results of Async task operations
 * on the main UI thread.
 * <p>
 * See {@link android.os.AsyncTask} interface.
 *
 * @param <Progress>
 * @param <Result>
 */
public interface ITaskListener<Progress, Result> {

    /**
     * Task is about to be executed.
     */
    void onPreExecute();

    /**
     * Task has been executed.
     *
     * @param result
     * @param error
     */
    void onPostExecute(@Nullable Result result, @Nullable Exception error);

    /**
     * Task is mid-stream.
     *
     * @param values
     */
    void onProgressUpdate(Progress... values);

    /**
     * Task has been cancelled.
     *
     * @param result
     * @param error
     */
    void onCancelled(Result result, @Nullable Exception error);

}
