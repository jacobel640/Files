package com.example.files.utils;

import static java.util.concurrent.Executors.*;

import java.util.concurrent.ExecutorService;

public class JFileExecutor {
    private static final ExecutorService EXECUTOR =
            newFixedThreadPool(2);

    public static void execute(Runnable r) {
        EXECUTOR.execute(r);
    }
}
