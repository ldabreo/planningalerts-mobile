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

package com.au.planningalerts.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * Headless fragment for managing {@link AsyncTask} operations and ensuring that they are properly
 * handled when the user exits the app.
 * <p>
 * This class is task agnostic. Async tasks register with the fragment on creation and the
 * fragment tracks them for the duration of the task.
 * <p>
 * This is as per Android's recommended approach for handling network operations within a mobile
 * application.
 * <p>
 * See {@link https://developer.android.com/training/basics/network-ops/connecting}
 */
public class NetworkHeadlessFragment extends Fragment {

    // Task being tracked
    private AsyncTask mCurrentTask = null;

    public static NetworkHeadlessFragment getInstance(FragmentManager fragmentManager) {

        NetworkHeadlessFragment networkFragment = (NetworkHeadlessFragment) fragmentManager
                .findFragmentByTag(NetworkHeadlessFragment.class.getSimpleName());

        if (networkFragment == null) {
            networkFragment = new NetworkHeadlessFragment();
            fragmentManager.beginTransaction().add(networkFragment, NetworkHeadlessFragment.class.getSimpleName()).commit();
        }
        return networkFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCurrentTask = null;
    }

    @Override
    public void onDestroy() {
        // Cancel task when Fragment is destroyed.
        cancel();
        super.onDestroy();
    }

    public void register(AsyncTask task) {
        // Cancel any previous task before tracking a new one
        cancel();
        mCurrentTask = task;
    }


    public void cancel() {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
    }

}