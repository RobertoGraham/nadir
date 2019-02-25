package io.github.robertograham.nadir;

import java.io.IOException;

/**
 * Entry-point for the {@code nadir} library
 * <p>
 * Can be instantiated via {@link Nadir#newNadir(String, String)}
 * which will use the default {@link Nadir.Builder} options
 * <p>
 * Can be created via {@link Nadir#newBuilder(String, String)} which returns an instance of {@link Nadir.Builder}
 * that lets you customise the object returned by {@link Builder#build()}
 *
 * @since 1.0.0
 */
public interface Nadir extends AutoCloseable {

    /**
     * @param emailAddress EA account email address
     * @param password     EA account password
     * @return An instance of {@link Nadir.Builder} that can be used to build an instance of {@code Nadir}
     * @throws NullPointerException If {@code emailAddress is null}
     * @throws NullPointerException If {@code password is null}
     * @since 1.0.0
     */
    static Builder newBuilder(final String emailAddress,
                              final String password) {
        return new NadirBuilderImpl(emailAddress, password);
    }

    /**
     * @param emailAddress EA account email address
     * @param password     EA account password
     * @return An authenticated instance of {@code Nadir} configured with the default {@link Nadir.Builder} options
     * @see Nadir#newBuilder(String, String)
     * @see Builder#build()
     * @since 1.0.0
     */
    static Nadir newNadir(final String emailAddress,
                          final String password) throws IOException {
        return Nadir.newBuilder(emailAddress, password)
            .build();
    }

    /**
     * @return The EA account email address this instance was configured to use
     * @since 1.0.0
     */
    String emailAddress();

    /**
     * @return The EA account password this instance was configured to use
     * @since 1.0.0
     */
    String password();

    /**
     * @return A non-expired instance of {@link Token} with at least 5 minutes of use left
     * @since 1.0.0
     */
    Token session();

    /**
     * @return an instance of {@link AccountResource} from which EA account related actions can be performed
     * @since 1.0.0
     */
    AccountResource accounts();

    @Override
    void close();

    /**
     * Used to build instances of {@link Nadir}
     *
     * @since 1.0.0
     */
    interface Builder {

        /**
         * @return An authenticated instance of {@link Nadir}
         * @throws StryderErrorException If any erroneous HTTP responses are encountered during authentication
         * @throws IOException           If any number of problems occur during authentication
         * @since 1.0.0
         */
        Nadir build() throws IOException;
    }
}
