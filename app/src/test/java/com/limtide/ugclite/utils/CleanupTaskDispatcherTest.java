package com.limtide.ugclite.utils;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CleanupTaskDispatcherTest {

    @Test
    public void shutdownExecutorRejectsWithoutThrowing() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.shutdown();

        assertFalse(CleanupTaskDispatcher.tryExecute(executor, () -> { }));
    }

    @Test
    public void rejectionRaceIsConvertedToFalse() {
        assertFalse(CleanupTaskDispatcher.tryExecute(
                new RejectingExecutor(),
                () -> { }));
    }

    @Test
    public void acceptedTaskRunsNormally() {
        AtomicBoolean ran = new AtomicBoolean();

        assertTrue(CleanupTaskDispatcher.tryExecute(
                new DirectExecutor(),
                () -> ran.set(true)));
        assertTrue(ran.get());
    }

    private static class DirectExecutor extends AbstractExecutorService {
        private boolean shutdown;

        @Override
        public void shutdown() {
            shutdown = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdown = true;
            return Collections.emptyList();
        }

        @Override
        public boolean isShutdown() {
            return shutdown;
        }

        @Override
        public boolean isTerminated() {
            return shutdown;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return shutdown;
        }

        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

    private static final class RejectingExecutor extends DirectExecutor {
        @Override
        public void execute(Runnable command) {
            throw new RejectedExecutionException("simulated shutdown race");
        }
    }
}
