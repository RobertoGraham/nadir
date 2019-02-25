package io.github.robertograham.nadir;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * An object from which actions related to EA accounts can be performed
 *
 * @since 1.0.0
 */
public interface AccountResource {

    /**
     * @return An {@link Optional} of {@link Account} that's non-empty if the HTTP response contained an account
     * @throws StryderErrorException If there's an HTTP error response
     * @throws IOException           If an {@link java.io.InputStream} cannot be created from the HTTP response
     * @since 1.0.0
     */
    Optional<Account> findOneBySessionToken() throws IOException;

    /**
     * @param searchTerms The terms that will be used to search for EA accounts
     * @return An {@link Optional} of {@link List} of {@link Account} that's non-empty if the HTTP response contained any accounts
     * @throws StryderErrorException If there's an HTTP error response
     * @throws IOException           If an {@link java.io.InputStream} cannot be created from the HTTP response
     * @since 1.0.0
     */
    Optional<List<Account>> findAllBySearchTerms(final String... searchTerms) throws IOException;
}
