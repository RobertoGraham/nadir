package io.github.robertograham.nadir;

import java.io.IOException;
import java.util.Optional;

public interface AccountResource {

    Optional<Account> findOneBySessionToken() throws IOException;

    Optional<String> findOneByName(final String name) throws IOException;
}
