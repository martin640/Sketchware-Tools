package io.ready.tools;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

public class ThreadManager {

    private Activity activity;

    public ThreadManager() {
        throw new UnsupportedOperationException("Invalid arguments for ThreadManager");
    }

    public ThreadManager(@NonNull Activity activity) {
        this.activity = activity;
    }

    public void runOnUiThread(@NonNull final Runnable runnable) {
        if (activity == null) {
            throw new NullPointerException("Activity cannot be null");
        }

        activity.runOnUiThread(new java.lang.Runnable() {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }

    public void runOnSeparateThread(@NonNull final AsynchronousRunnable runnable) {
        new task().execute(runnable);
    }

    public interface Runnable {
        void run();
    }

    public interface AsynchronousRunnable {
        void run(ThreadManager threadManager);

        void onComplete(ThreadManager threadManager);
    }

    private class task extends AsyncTask<AsynchronousRunnable, AsynchronousRunnable, AsynchronousRunnable> {

        private AsynchronousRunnable runnable;

        @Override
        protected AsynchronousRunnable doInBackground(AsynchronousRunnable... runnables) {
            runnable = runnables[0];
            runnable.run(ThreadManager.this);
            return null;
        }

        @Override
        protected void onPostExecute(AsynchronousRunnable r) {
            super.onPostExecute(runnable);
            runnable.onComplete(ThreadManager.this);
        }
    }
}
