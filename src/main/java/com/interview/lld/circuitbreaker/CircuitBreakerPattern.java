package com.interview.lld.circuitbreaker;

import java.util.ArrayList;
import java.util.List;

public class CircuitBreakerPattern {

    private record Request(
            String id,
            long timestamp,
            String result
    ) {
    }

    public static List<String> processCircuitBreaker(
            List<Request> requests,
            int failureThreshold,
            int successThreshold,
            int timeout) {

        List<String> output = new ArrayList<>();

        String state = "CLOSED";

        int failureCount = 0;
        int successCount = 0;

        long openTimestamp = -1;

        for (Request request : requests) {

            long timestamp = request.timestamp();
            String result = request.result();

            // 1. Check whether OPEN timeout has expired.
            if (state.equals("OPEN")
                    && timestamp - openTimestamp >= timeout) {
                state = "HALF_OPEN";
            }

            // 2. State BEFORE applying this request.
            output.add(state);

            // 3. Apply request result.
            if (state.equals("CLOSED")) {

                if (result.equals("failure")) {

                    failureCount++;

                    if (failureCount >= failureThreshold) {
                        state = "OPEN";
                        openTimestamp = timestamp;

                        failureCount = 0;
                    }

                } else {
                    // Success resets consecutive failure count.
                    failureCount = 0;
                }

            } else if (state.equals("OPEN")) {
                // Ignore request result while OPEN.
            } else { // HALF_OPEN

                if (result.equals("success")) {

                    successCount++;

                    if (successCount >= successThreshold) {
                        state = "CLOSED";
                        successCount = 0;
                    }

                } else {

                    // Any failure immediately reopens circuit.
                    state = "OPEN";
                    openTimestamp = timestamp;
                    successCount = 0;
                }
            }
        }

        return output;
    }

    public static void main(String[] args) {

        List<Request> requests = List.of(
                new Request("req1", 100, "success"),
                new Request("req2", 200, "failure"),
                new Request("req3", 300, "failure"),
                new Request("req4", 400, "failure"),
                new Request("req5", 1500, "success"),
                new Request("req6", 1600, "success")
        );

        List<String> result = processCircuitBreaker(
                requests,
                2,      // failureThreshold
                1,      // successThreshold
                1000    // timeout
        );

        result.forEach(System.out::println);
    }
}