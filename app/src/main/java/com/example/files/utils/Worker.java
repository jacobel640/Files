package com.example.files.utils;

import android.os.AsyncTask;
import java.util.ArrayList;

public class Worker {

    ArrayList<Runnable> jobs = new ArrayList<>();
    Executor executor;
    boolean isRunning;


    Worker() {
        executor = new Executor();
    }

    public void addJob(Runnable runnable) {
        jobs.add(runnable);
        if (!isRunning) executor.execute();
    }

    public void addJobs(ArrayList<Runnable> runnable) {
        jobs.addAll(runnable);
        if (!isRunning) executor.execute();
    }

    public void removeJob(Runnable runnable) {
        jobs.remove(runnable);
    }

    class Executor extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isRunning = true;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            for (Runnable runnable : jobs) {
                runnable.run();
                jobs.remove(runnable);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            isRunning = false;
        }

//        public void cancel(boolean isCanceled, String nothing) {
//            super.cancel(true);
//        }
    }
}
