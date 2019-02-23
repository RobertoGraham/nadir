package io.github.robertograham.nadir;

import org.apache.http.client.CookieStore;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.time.LocalDateTime;

final class NadirImpl implements Nadir {

    private final String emailAddress;
    private final String password;
    private final CookieStore cookieStore;
    private final CloseableHttpClient httpClient;
    private final AuthenticationResource authenticationResource;
    private final AccountResource accountResource;
    private Token sessionToken;

    NadirImpl(final NadirBuilderImpl nadirBuilderImpl) throws IOException {
        this.emailAddress = nadirBuilderImpl.emailAddress;
        this.password = nadirBuilderImpl.password;
        cookieStore = new BasicCookieStore();
        httpClient = HttpClientBuilder.create()
            .disableRedirectHandling()
            .setDefaultCookieStore(cookieStore)
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
    }

    private Token fetchSessionToken() throws IOException {
        return authenticationResource.userCredentialsGrantedToken(emailAddress, password)
            .orElseThrow(() -> new IOException("Failed to login"));
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
    public void close() {
        HttpClientUtils.closeQuietly(httpClient);
    }
}
