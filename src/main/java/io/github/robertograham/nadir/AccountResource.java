package io.github.robertograham.nadir;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface AccountResource {

    Optional<Account> findOneBySessionToken() throws IOException;

    Optional<List<Account>> findAllBySearchTerms(final String... names) throws IOException;
}
