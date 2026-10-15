package org.hospital.workers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

/** Runs the JUnit test methods without Maven when a local build tool is unavailable. */
public final class OfflineTestRunner {
    private OfflineTestRunner() {}

    public static void main(String[] args) throws Exception {
        int passed = 0;
        int failed = 0;
        for (Class<?> testClass : new Class<?>[]{WorkerLogicSmokeTest.class, ApplicationDataRepositoryTest.class}) {
            Object instance = testClass.getDeclaredConstructor().newInstance();
            for (Method method : testClass.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(Test.class)) {
                    continue;
                }
                try {
                    method.invoke(instance);
                    System.out.println("[PASS] " + testClass.getSimpleName() + "." + method.getName());
                    passed++;
                } catch (InvocationTargetException exception) {
                    System.err.println("[FAIL] " + testClass.getSimpleName() + "." + method.getName()
                            + ": " + exception.getCause());
                    failed++;
                }
            }
        }
        System.out.println("Tests passed=" + passed + " failed=" + failed);
        if (failed != 0) {
            throw new AssertionError("Offline test run failed");
        }
    }
}
