package com.limtide.ugclite.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

final class CleanupTaskDispatcher {
    private CleanupTaskDispatcher() {
    }

    static boolean tryExecute(ExecutorService executor, Runnable task) {
        if (executor.isShutdown()) {
            return false;
        }
        try {
            executor.execute(task);
            return true;
        } catch (RejectedExecutionException exception) {
            return false;
        }
    }
}
