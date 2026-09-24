package com.interview.lld.ratelimiter;

import java.util.concurrent.ConcurrentHashMap;

public class FixedWindowRateLimiter {

    private final int maxRequests;
    private final long windowMillis;

    private final ConcurrentHashMap<String, ClientWindow> clients = new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }

    public boolean allowRequest(String clientId) {

        ClientWindow window =
                clients.computeIfAbsent(
                        clientId,
                        key -> new ClientWindow(System.currentTimeMillis())
                );

        synchronized (window) {

            long now = System.currentTimeMillis();

            // Start a new window.
            if (now - window.windowStart >= windowMillis) {
                window.windowStart = now;
                window.requestCount = 0;
            }

            // Limit reached.
            if (window.requestCount >= maxRequests) {
                return false;
            }

            // Allow request.
            window.requestCount++;

            return true;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(5, 2 * 1000);

        System.out.println(rateLimiter.allowRequest("client-1"));
        System.out.println(rateLimiter.allowRequest("client-1"));
        System.out.println(rateLimiter.allowRequest("client-1"));
        System.out.println(rateLimiter.allowRequest("client-1"));
        System.out.println(rateLimiter.allowRequest("client-1"));
        System.out.println(rateLimiter.allowRequest("client-1"));

        Thread.sleep(2 * 1000);

        System.out.println(rateLimiter.allowRequest("client-1"));
        System.out.println(rateLimiter.allowRequest("client-1"));

    }

    static class ClientWindow {
        long windowStart;
        long requestCount;

        ClientWindow(long windowStart) {
            this.windowStart = windowStart;
            this.requestCount = 0;
        }
    }
}