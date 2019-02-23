package io.github.robertograham.nadir;

import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

final class AccountResourceImpl implements AccountResource {

    private final CloseableHttpClient httpClient;
    private final OptionalResultResponseHandlerProvider optionalResultResponseHandlerProvider;
    private final Supplier<Token> tokenSupplier;

    AccountResourceImpl(final CloseableHttpClient httpClient,
                        final OptionalResultResponseHandlerProvider optionalResultResponseHandlerProvider,
                        final Supplier<Token> tokenSupplier) {
        this.httpClient = httpClient;
        this.optionalResultResponseHandlerProvider = optionalResultResponseHandlerProvider;
        this.tokenSupplier = tokenSupplier;
    }


    @Override
    public Optional<Account> findOneBySessionToken() throws IOException {
        final var token = tokenSupplier.get();
        return httpClient.execute(
            RequestBuilder.get("https://gateway.ea.com/proxy/identity/pids/me")
                .setHeader(AUTHORIZATION, String.format("%s %s", token.tokenType(), token.accessToken()))
                .build(),
            optionalResultResponseHandlerProvider.forClass(AccountImpl.class)
        )
            .map(Function.identity());
    }

    @Override
    public Optional<String> findOneByName(final String name) throws IOException {
        final var accountIdString = findOneBySessionToken()
            .map(Account::id)
            .map(String::valueOf)
            .orElseThrow(() -> new IOException("Couldn't get authenticated user's ID"));
        final var token = tokenSupplier.get();
        return httpClient.execute(
            RequestBuilder.get("https://api1.origin.com/xsearch/users")
                .addParameter("userId", accountIdString)
                .addParameter("searchTerm", name)
                .addParameter("start", "0")
                .setHeader("authToken", token.accessToken())
                .build(),
            optionalResultResponseHandlerProvider.forString()
        );
    }
}
