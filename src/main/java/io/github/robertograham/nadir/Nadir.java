package io.github.robertograham.nadir;

import java.io.IOException;

public interface Nadir extends AutoCloseable {

    static Builder newBuilder(final String emailAddress,
                              final String password) {
        return new NadirBuilderImpl(emailAddress, password);
    }

    static Nadir newNadir(final String emailAddress,
                          final String password) throws IOException {
        return Nadir.newBuilder(emailAddress, password)
            .build();
    }

    String emailAddress();

    String password();

    Token session();

    AccountResource accounts();

    @Override
    void close();

    interface Builder {

        Nadir build() throws IOException;
    }
}
