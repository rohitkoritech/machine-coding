package com.interview.lld.tokenmanager;

public interface TokenProvider {

    /**
     * Refreshes the access token.
     *
     * @param refreshToken the refresh token associated
     *                     with the current access token
     */
    Token refresh(String refreshToken);
}