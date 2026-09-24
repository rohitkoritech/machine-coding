package com.interview.lld.tokenmanager;

import java.time.Clock;
import java.time.Duration;

public class Main {

    public static void main(String[] args) {

        TokenProvider provider =
                new OAuthTokenProvider();

        TokenManager tokenManager =
                new TokenManager(
                        provider,
                        Clock.systemUTC(),
                        Duration.ofMinutes(5)
                );

        String token1 = tokenManager.getToken();
        String token2 = tokenManager.getToken();

        System.out.println(token1);
        System.out.println(token2);

        // Same cached token.
        System.out.println(token1.equals(token2));
    }
}