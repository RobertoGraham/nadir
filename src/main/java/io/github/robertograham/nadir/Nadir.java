package io.github.robertograham.nadir;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.io.IOException;
import java.util.Optional;

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
     * @return {@code true} if this instance was configured to establish an
     * XMPP connection or {@code false} otherwise
     * @since 1.1.0
     */
    default boolean establishXmppConnection() {
        return false;
    }

    /**
     * @return {@code true} if this instance was configured to debug
     * XMPP traffic or {@code false} otherwise
     * @since 1.1.0
     */
    default boolean debugXmppTraffic() {
        return false;
    }

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

    /**
     * @return an {@link Optional} of {@link XMPPTCPConnection} that's non-empty if this instance was
     * configured to establish an XMPP connection
     * @since 1.1.0
     */
    default Optional<XMPPTCPConnection> xmppConnection() {
        return Optional.empty();
    }

    @Override
    void close();

    /**
     * Used to build instances of {@link Nadir}
     *
     * @since 1.0.0
     */
    interface Builder {

        /**
         * {@code false} by default
         *
         * @param establishXmppConnection {@code true} to establish an XMPP connection
         *                                or {@code false} otherwise
         * @return the {@code Builder} instance this method was called on
         * @since 1.1.0
         */
        default Builder establishXmppConnection(final boolean establishXmppConnection) {
            return this;
        }

        /**
         * {@code true} by default
         *
         * @param debugXmppTraffic {@code true} to debug XMPP traffic
         *                         or {@code false} otherwise
         * @return the {@code Builder} instance this method was called on
         * @since 1.1.0
         */
        default Builder debugXmppTraffic(final boolean debugXmppTraffic) {
            return this;
        }

        /**
         * @return An authenticated instance of {@link Nadir}
         * @throws StryderErrorException If any erroneous HTTP responses are encountered during authentication
         * @throws IOException           If any number of problems occur during authentication or when establishing
         *                               an XMPP connection
         * @since 1.0.0
         */
        Nadir build() throws IOException;
    }
}
