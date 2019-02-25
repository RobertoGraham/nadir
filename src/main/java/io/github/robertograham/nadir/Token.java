package io.github.robertograham.nadir;

import java.time.LocalDateTime;

/**
 * Representation of a user's authentication session
 *
 * @since 1.0.0
 */
public interface Token {

    /**
     * @return An access token that can be used to authenticate against API endpoints
     * @since 1.0.0
     */
    String accessToken();

    /**
     * @return The type of token this, I have only seen {@code Bearer} so far
     * @since 1.0.0
     */
    String tokenType();

    /**
     * @return The time when this session will be invalid. A valid {@code Token} can be obtained using
     * {@link Nadir#session()}
     * @since 1.0.0
     */
    LocalDateTime accessTokenExpiresAt();
}
