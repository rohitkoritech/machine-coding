package com.interview.lld.circuitbreaker;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class CircuitBreakerWebClient {

    // 3 failures within 20 seconds => circuit opens
    private static final int FAILURE_THRESHOLD = 3;
    private static final Duration FAILURE_WINDOW = Duration.ofSeconds(20);

    // Circuit remains open for 10 seconds
    private static final Duration OPEN_DURATION = Duration.ofSeconds(10);

    private enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    /**
     * Maintains circuit-breaker state independently for each service.
     * <p>
     * ServiceB -> its own CircuitState
     * ServiceC -> its own CircuitState
     */
    private final Map<String, CircuitState> circuits =
            new ConcurrentHashMap<>();

    /**
     * This is the only method we need to expose for the problem.
     */
    public Response execute(String service) {

        CircuitState circuit =
                circuits.computeIfAbsent(
                        service,
                        key -> new CircuitState()
                );

        synchronized (circuit) {

            Instant now = Instant.now();

            // Remove failures outside the 20-second window.
            circuit.removeOldFailures(now);

            // --------------------------------------------------
            // 1. Check whether the circuit allows the call
            // --------------------------------------------------

            if (circuit.state == State.OPEN) {

                /*
                 * Circuit is open.
                 * Check whether the 10-second block period is over.
                 */
                if (now.isBefore(circuit.openedAt.plus(OPEN_DURATION))) {
                    return null; // Call is blocked
                }

                /*
                 * Block period has expired.
                 * Allow one trial request.
                 */
                circuit.state = State.HALF_OPEN;
            }

            // --------------------------------------------------
            // 2. Make the remote call
            // --------------------------------------------------

            Response response;

            try {
                response = call();
            } catch (Exception e) {

                // Treat remote-call exceptions as failures.
                circuit.recordFailure(now);

                return null;
            }

            // --------------------------------------------------
            // 3. Process the response
            // --------------------------------------------------

            if (response.getStatus() == 500) {

                circuit.recordFailure(now);

            } else {

                /*
                 * Successful request.
                 *
                 * A successful HALF_OPEN request closes
                 * the circuit again.
                 */
                circuit.state = State.CLOSED;
                circuit.failures.clear();
            }

            return response;
        }
    }

    /**
     * State belonging to ONE service.
     */
    private static class CircuitState {

        private State state = State.CLOSED;

        /*
         * Timestamps of recent failures.
         *
         * ArrayDeque is sufficient because failures are inserted
         * chronologically and old failures are removed from the front.
         */
        private final Deque<Instant> failures = new ArrayDeque<>();

        private Instant openedAt;

        private void removeOldFailures(Instant now) {

            Instant cutoff = now.minus(FAILURE_WINDOW);

            while (!failures.isEmpty()
                    && failures.peekFirst().isBefore(cutoff)) {

                failures.removeFirst();
            }
        }

        private void recordFailure(Instant now) {

            failures.addLast(now);

            /*
             * Three failures inside the sliding window
             * => open the circuit.
             */
            if (failures.size() >= FAILURE_THRESHOLD) {

                state = State.OPEN;
                openedAt = now;
            }
        }
    }

    // ----------------------------------------------------------
    // Simulated remote call
    // ----------------------------------------------------------

    public Response call() {

        // Existing simulation provided by the interviewer.
        return new Response();
    }

    public static class Response {
        int getStatus() {
            return 200;
        }
    }
}