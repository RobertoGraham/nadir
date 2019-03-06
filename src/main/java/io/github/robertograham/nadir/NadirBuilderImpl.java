package io.github.robertograham.nadir;

import java.io.IOException;
import java.util.Objects;

final class NadirBuilderImpl implements Nadir.Builder {

    final String emailAddress;
    final String password;
    boolean establishXmppConnection;
    boolean debugXmppTraffic;

    NadirBuilderImpl(final String emailAddress,
                     final String password) {
        this.emailAddress = Objects.requireNonNull(emailAddress);
        this.password = Objects.requireNonNull(password);
        establishXmppConnection = false;
        debugXmppTraffic = true;
    }

    @Override
    public Nadir.Builder establishXmppConnection(final boolean establishXmppConnection) {
        this.establishXmppConnection = establishXmppConnection;
        return this;
    }

    @Override
    public Nadir.Builder debugXmppTraffic(final boolean debugXmppTraffic) {
        this.debugXmppTraffic = debugXmppTraffic;
        return this;
    }

    @Override
    public Nadir build() throws IOException {
        return new NadirImpl(this);
    }
}
