package com.terheyden.event;

/**
 * Extends {@link Runnable} and allows for checked exceptions.
 */
@FunctionalInterface
public interface CheckedRunnable extends Runnable {

    /**
     * Static method to create a {@link CheckedRunnable} from a lambda.
     */
    static CheckedRunnable of(CheckedRunnable runnable) {
        return runnable;
    }

    /**
     * The same as {@link Runnable#run()}, but allows throwing checked exceptions.
     */
    void runChecked() throws Throwable;

    /**
     * Unchecked version of {@link Runnable#run()}.
     * Use this just like you would use {@link Runnable#run()}.
     * Any exceptions thrown will be rethrown as unchecked automatically.
     */
    @Override
    default void run() {
        try {
            runChecked();
        } catch (Throwable t) {
            CheckedRunnableInternal.throwUnchecked(t);
        }
    }
}

/**
 * Defines a self-contained unchecked throw method.
 */
interface CheckedRunnableInternal {
    @SuppressWarnings("unchecked")
    static <T extends Throwable, R> R throwUnchecked(Throwable t) throws T {
        throw (T) t;
    }
}
