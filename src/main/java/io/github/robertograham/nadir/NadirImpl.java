package io.github.robertograham.nadir;

import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.StreamError;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Resourcepart;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

final class NadirImpl implements Nadir {

    private final String emailAddress;
    private final String password;
    private final boolean establishXmppConnection;
    private final boolean debugXmppTraffic;
    private final CloseableHttpClient httpClient;
    private final AuthenticationResource authenticationResource;
    private final AccountResource accountResource;
    private final XMPPTCPConnection xmppTcpConnection;
    private Token sessionToken;

    NadirImpl(final NadirBuilderImpl nadirBuilderImpl) throws IOException {
        emailAddress = nadirBuilderImpl.emailAddress;
        password = nadirBuilderImpl.password;
        establishXmppConnection = nadirBuilderImpl.establishXmppConnection;
        debugXmppTraffic = nadirBuilderImpl.debugXmppTraffic;
        httpClient = HttpClientBuilder.create()
            .disableRedirectHandling()
            .setDefaultCookieStore(new BasicCookieStore())
            .build();
        authenticationResource = new AuthenticationResource(
            httpClient,
            JsonOptionalResultResponseHandlerProvider.INSTANCE
        );
        sessionToken = fetchSessionToken();
        accountResource = new AccountResourceImpl(
            httpClient,
            JsonOptionalResultResponseHandlerProvider.INSTANCE,
            this::session
        );
        if (establishXmppConnection) {
            SmackConfiguration.DEBUG = debugXmppTraffic;
            xmppTcpConnection = createXmppTcpConnection();
        } else
            xmppTcpConnection = null;
    }

    private Token fetchSessionToken() throws IOException {
        return authenticationResource.userCredentialsGrantedToken(emailAddress, password)
            .orElseThrow(() -> new IOException("Failed to login"));
    }

    private XMPPTCPConnection createXmppTcpConnection() throws IOException {
        final var domain = "chat.dm.origin.com";
        final var xmppTcpConnectionConfigurationBuilder = XMPPTCPConnectionConfiguration.builder()
            .setXmppDomain(JidCreate.domainBareFrom(Domainpart.fromOrThrowUnchecked(domain)))
            .setHost(domain)
            .setResource(Resourcepart.fromOrThrowUnchecked("origin"))
            .setPort(5222)
            .setUsernameAndPassword(
                accountResource.findOneBySessionToken()
                    .map(Account::userId)
                    .map(String::valueOf)
                    .orElseThrow(() -> new IOException("Failed to get authenticated user's UID")),
                password
            );
        var xmppTcpConnection = new XMPPTCPConnection(xmppTcpConnectionConfigurationBuilder.build());
        try {
            xmppTcpConnection.connect()
                .login();
            return xmppTcpConnection;
        } catch (final InterruptedException | SmackException | IOException exception) {
            xmppTcpConnection.disconnect();
            throw new IOException("Failed to establish XMPP connection", exception);
        } catch (final XMPPException exception) {
            xmppTcpConnection.disconnect();
            if (exception instanceof XMPPException.StreamErrorException) {
                final var streamErrorException = (XMPPException.StreamErrorException) exception;
                final var streamError = streamErrorException.getStreamError();
                if (StreamError.Condition.see_other_host.equals(streamError.getCondition())) {
                    xmppTcpConnection = new XMPPTCPConnection(xmppTcpConnectionConfigurationBuilder.setHost(streamError.getConditionText())
                        .build());
                    try {
                        xmppTcpConnection.connect()
                            .login();
                        return xmppTcpConnection;
                    } catch (final XMPPException | SmackException | InterruptedException nestedException) {
                        xmppTcpConnection.disconnect();
                        throw new IOException("Failed to establish XMPP connection", nestedException);
                    }
                }
            }
            throw new IOException("Failed to establish XMPP connection", exception);
        }
    }

    @Override
    public String emailAddress() {
        return emailAddress;
    }

    @Override
    public String password() {
        return password;
    }

    @Override
    public boolean establishXmppConnection() {
        return establishXmppConnection;
    }

    @Override
    public boolean debugXmppTraffic() {
        return debugXmppTraffic;
    }

    @Override
    public Token session() {
        if (sessionToken.accessTokenExpiresAt()
            .minusMinutes(5L)
            .isBefore(LocalDateTime.now()))
            try {
                sessionToken = fetchSessionToken();
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        return sessionToken;
    }

    @Override
    public AccountResource accounts() {
        return accountResource;
    }

    @Override
    public Optional<XMPPTCPConnection> xmppConnection() {
        return Optional.ofNullable(xmppTcpConnection);
    }

    @Override
    public void close() {
        HttpClientUtils.closeQuietly(httpClient);
        if (xmppTcpConnection != null)
            xmppTcpConnection.disconnect();
    }
}
