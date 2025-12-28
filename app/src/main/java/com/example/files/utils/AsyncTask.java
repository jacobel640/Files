package com.example.files.utils;

import android.annotation.SuppressLint;

import java.util.concurrent.Executor;

public class AsyncTask {

    public static final Executor THREAD_POOL_EXECUTOR = android.os.AsyncTask.THREAD_POOL_EXECUTOR;
    android.os.AsyncTask<Void, Void, Void> task;

    @SuppressLint("StaticFieldLeak")
    public AsyncTask(Runnable runnable) {
        task = new android.os.AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {

                runnable.run();

                return null;
            }

        };
    }

    @SuppressLint("StaticFieldLeak")
    public AsyncTask(Runnable runnable, Runnable postExecute) {
        task = new android.os.AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {

                runnable.run();

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                postExecute.run();
            }

        };
    }

    @SuppressLint("StaticFieldLeak")
    public AsyncTask(Runnable preExecute, Runnable runnable, Runnable postExecute) {
        task = new android.os.AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                preExecute.run();
            }

            @Override
            protected Void doInBackground(Void... arg0) {

                runnable.run();

                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
                // TODO progressUpdate.run();
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                postExecute.run();
            }

        };
    }

    public android.os.AsyncTask<Void, Void, Void> execute() {
        return task.execute();
    }

    public android.os.AsyncTask<Void, Void, Void> executeOnExecutor(Executor exec, Void... params) {
        return task.executeOnExecutor(exec, params);
    }

    public android.os.AsyncTask.Status getStatus() {
        return task.getStatus();
    }
}
