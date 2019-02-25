package io.github.robertograham.nadir;

/**
 * The {@code Account} class represents an EA account
 *
 * @since 1.0.0
 */
public interface Account {

    /**
     * @return The UID (user ID) of this {@code Account}
     * @since 1.0.0
     */
    long userId();

    /**
     * @return The EAID (EA username) of this {@code Account}
     * @since 1.0.0
     */
    String username();
}
