/*
 * This file is derived from ZXing project ( https://github.com/zxing/zxing )
 * and is modified for android-lib-ZXingCaptureActivity project.
 *
 * Copyright (C) 2012 ZXing authors
 * Copyright (C) 2014 NOBUOKA Yu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.vividcode.android.zxing.camera;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

final class AutoFocusManager implements Camera.AutoFocusCallback {

  private static final String TAG = AutoFocusManager.class.getSimpleName();

  private static final long AUTO_FOCUS_INTERVAL_MS = 2000L;
  private static final Collection<String> FOCUS_MODES_CALLING_AF;
  static {
    FOCUS_MODES_CALLING_AF = new ArrayList<String>(2);
    FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
    FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
  }

  private boolean active;
  private final boolean useAutoFocus;
  private final Camera camera;
  private AsyncTask<?,?,?> outstandingTask;

  AutoFocusManager(Camera camera) {
    this.camera = camera;
    String currentFocusMode = camera.getParameters().getFocusMode();
    useAutoFocus = FOCUS_MODES_CALLING_AF.contains(currentFocusMode);
    Log.i(TAG, "Current focus mode '" + currentFocusMode + "'; use auto focus? " + useAutoFocus);
    start();
  }

  @TargetApi(11)
  @Override
  public synchronized void onAutoFocus(boolean success, Camera theCamera) {
    if (active) {
      outstandingTask = new AutoFocusTask();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
          outstandingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else {
          outstandingTask.execute();
        }
    }
  }

  synchronized void start() {
    if (useAutoFocus) {
      active = true;
      try {
        camera.autoFocus(this);
      } catch (RuntimeException re) {
        // Have heard RuntimeException reported in Android 4.0.x+; continue?
        Log.w(TAG, "Unexpected exception while focusing", re);
      }
    }
  }

  synchronized void stop() {
    if (useAutoFocus) {
      try {
        camera.cancelAutoFocus();
      } catch (RuntimeException re) {
        // Have heard RuntimeException reported in Android 4.0.x+; continue?
        Log.w(TAG, "Unexpected exception while cancelling focusing", re);
      }
    }
    if (outstandingTask != null) {
      outstandingTask.cancel(true);
      outstandingTask = null;
    }
    active = false;
  }

  private final class AutoFocusTask extends AsyncTask<Object,Object,Object> {
    @Override
    protected Object doInBackground(Object... voids) {
      try {
        Thread.sleep(AUTO_FOCUS_INTERVAL_MS);
      } catch (InterruptedException e) {
        // continue
      }
      synchronized (AutoFocusManager.this) {
        if (active) {
          start();
        }
      }
      return null;
    }
  }

}
