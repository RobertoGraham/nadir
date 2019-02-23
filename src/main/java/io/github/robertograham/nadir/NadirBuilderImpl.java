package io.github.robertograham.nadir;

import java.io.IOException;
import java.util.Objects;

final class NadirBuilderImpl implements Nadir.Builder {

    final String emailAddress;
    final String password;

    NadirBuilderImpl(final String emailAddress,
                     final String password) {
        this.emailAddress = Objects.requireNonNull(emailAddress);
        this.password = Objects.requireNonNull(password);
    }

    @Override
    public Nadir build() throws IOException {
        return new NadirImpl(this);
    }
}
