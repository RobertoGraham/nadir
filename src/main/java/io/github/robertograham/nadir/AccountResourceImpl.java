package io.github.robertograham.nadir;

import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

final class AccountResourceImpl implements AccountResource {

    private static final int MAX_ID_COUNT_PER_ACCOUNTS_REQUEST = 5;
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
        final var accountImplOptional = httpClient.execute(
            RequestBuilder.get("https://gateway.ea.com/proxy/identity/pids/me")
                .setHeader(AUTHORIZATION, String.format("%s %s", token.tokenType(), token.accessToken()))
                .build(),
            optionalResultResponseHandlerProvider.forClass(AccountImpl.class)
        );
        if (accountImplOptional.isPresent()) {
            final var userIdLong = accountImplOptional
                .map(Account::userId)
                .orElseThrow();
            return findAllByUserIds(List.of(String.valueOf(userIdLong))).orElseThrow()
                .stream()
                .filter((final var account) -> userIdLong == account.userId())
                .findFirst()
                .or(() -> accountImplOptional);
        }
        return accountImplOptional.map(Function.identity());
    }

    private Optional<Set<String>> findAllUserIdsBySearchTerms(final String... searchTerms) throws IOException {
        Objects.requireNonNull(searchTerms, "searchTerms cannot be null");
        for (final var nameString : searchTerms)
            Objects.requireNonNull(nameString, "searchTerms cannot contain a null value");
        final var accountIdString = findOneBySessionToken()
            .map(Account::userId)
            .map(String::valueOf)
            .orElseThrow(() -> new IOException("Couldn't get authenticated user's ID"));
        return httpClient.execute(
            RequestBuilder.get("https://api1.origin.com/xsearch/users")
                .addParameter("userId", accountIdString)
                .addParameter("searchTerm", String.join("\tOR\t", searchTerms))
                .addParameter("start", "0")
                .setHeader("authToken", tokenSupplier.get().accessToken())
                .build(),
            optionalResultResponseHandlerProvider.forClass(XSearchResult.class)
        )
            .map(XSearchResult::friendUserIds);
    }

    @Override
    public Optional<List<Account>> findAllBySearchTerms(final String... searchTerms) throws IOException {
        final var userIdStringList = findAllUserIdsBySearchTerms(searchTerms)
            .map(ArrayList::new)
            .orElseThrow(() -> new IOException("Failed to fetch user IDs from search terms"));
        final var userIdPartitionListList = IntStream.range(0, userIdStringList.size())
            .boxed()
            .collect(Collectors.groupingBy((final var integer) -> integer / MAX_ID_COUNT_PER_ACCOUNTS_REQUEST))
            .values()
            .stream()
            .map((final var integerList) -> integerList.stream()
                .map(userIdStringList::get)
                .collect(Collectors.toUnmodifiableList()))
            .collect(Collectors.toUnmodifiableList());
        final var optionalAccountListList = new ArrayList<Optional<List<Account>>>();
        for (final var userIdPartitionList : userIdPartitionListList)
            optionalAccountListList.add(findAllByUserIds(userIdPartitionList));
        return optionalAccountListList.stream()
            .reduce((final var optionalAccountList, final var optionalAccountListAccumulator) ->
                optionalAccountListAccumulator.map((final var accountList) -> Stream.concat(
                    accountList.stream(),
                    optionalAccountList.orElseGet(Collections::emptyList)
                        .stream())
                    .collect(Collectors.toUnmodifiableList()))
            )
            .orElseGet(Optional::empty);
    }

    private Optional<List<Account>> findAllByUserIds(final List<String> userIds) throws IOException {
        final var usersXmlString = httpClient.execute(
            RequestBuilder.get("https://api1.origin.com/atom/users")
                .addParameter("userIds", String.join(",", userIds))
                .setHeader("authToken", tokenSupplier.get().accessToken())
                .build(),
            optionalResultResponseHandlerProvider.forString()
        )
            .orElseThrow(() -> new IOException("Failed to fetch user XML"));
        try (final var stringReader = new StringReader(usersXmlString)) {
            return Optional.of(JAXB.unmarshal(stringReader, XmlUsers.class))
                .map(XmlUsers::users)
                .map((final var xmlUserList) -> xmlUserList.stream()
                    .map(xmlUser -> (Account) xmlUser)
                    .collect(Collectors.toUnmodifiableList()));
        }
    }
}
