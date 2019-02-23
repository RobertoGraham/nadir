package io.github.robertograham.nadir;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

final class AuthenticationResource {

    private final CloseableHttpClient httpClient;
    private final OptionalResultResponseHandlerProvider optionalResultResponseHandlerProvider;

    AuthenticationResource(final CloseableHttpClient httpClient,
                           final OptionalResultResponseHandlerProvider optionalResultResponseHandlerProvider) {
        this.httpClient = httpClient;
        this.optionalResultResponseHandlerProvider = optionalResultResponseHandlerProvider;
    }

    private Optional<String> firstLocationUrl() throws IOException {
        try (final var httpResponse = httpClient.execute(RequestBuilder.get("https://accounts.ea.com/connect/auth")
            .addParameters(
                new BasicNameValuePair("response_type", "code"),
                new BasicNameValuePair("client_id", "ORIGIN_SPA_ID"),
                new BasicNameValuePair("display", "originXWeb/login"),
                new BasicNameValuePair("locale", "en_US"),
                new BasicNameValuePair("release_type", "prod"),
                new BasicNameValuePair("redirect_uri", "https://www.origin.com/views/login.html")
            )
            .build())) {
            return extractOptionalLocationFromHttpResponse(httpResponse, "Location");
        }
    }

    private Optional<String> secondLocationPathFromFirstLocationUrl(final String firstLocationUrl) throws IOException {
        try (final var httpResponse = httpClient.execute(RequestBuilder.get(firstLocationUrl)
            .build())) {
            return extractOptionalLocationFromHttpResponse(httpResponse, "Location");
        }
    }

    private Optional<String> thirdLocationUrlFromSecondLocationPath(final String secondLocationPath) throws IOException {
        try (final var httpResponse = httpClient.execute(RequestBuilder.get(String.format(
            "%s%s",
            "https://signin.ea.com",
            secondLocationPath
        ))
            .build())) {
            return extractOptionalLocationFromHttpResponse(httpResponse, "SelfLocation");
        }
    }

    private Optional<String> windowLocationFromThirdLocationUrl(final String emailAddress,
                                                                final String password,
                                                                final String thirdLocationUrl) throws IOException {
        final var htmlString = httpClient.execute(
            RequestBuilder.post(thirdLocationUrl)
                .setEntity(EntityBuilder.create()
                    .setContentType(ContentType.APPLICATION_FORM_URLENCODED)
                    .setParameters(
                        new BasicNameValuePair("email", emailAddress),
                        new BasicNameValuePair("password", password),
                        new BasicNameValuePair("_eventId", "submit"),
                        new BasicNameValuePair("cid", UUID.randomUUID().toString().replace("-", "")),
                        new BasicNameValuePair("showAgeUp", "true"),
                        new BasicNameValuePair("googleCaptchaResponse", ""),
                        new BasicNameValuePair("_rememberMe", "on")
                    )
                    .build())
                .build(),
            optionalResultResponseHandlerProvider.forString()
        )
            .orElseThrow(() -> new IllegalStateException("Couldn't find html"));
        try (final var scanner = new Scanner(htmlString)) {
            return scanner.findAll(Pattern.compile("\\R+\\s+window.location\\s*=\\s*\"(.*)\";\\R+"))
                .filter(Objects::nonNull)
                .filter((final var matchResult) -> matchResult.groupCount() == 1)
                .map((final var matchResult) -> matchResult.group(1))
                .findFirst();
        }
    }

    private Optional<String> fourthLocationUrlFromWindowLocation(final String windowLocation) throws IOException {
        try (final var httpResponse = httpClient.execute(RequestBuilder.get(windowLocation)
            .build())) {
            return extractOptionalLocationFromHttpResponse(httpResponse, "Location");
        }
    }

    private void requestFourthLocationUrl(final String fourthLocationUrl) throws IOException {
        try (final var httpResponse = httpClient.execute(RequestBuilder.get(fourthLocationUrl)
            .build())) {
        }
    }

    private Optional<Token> fetchToken() throws IOException {
        return httpClient.execute(
            RequestBuilder.get("https://accounts.ea.com/connect/auth?client_id=ORIGIN_JS_SDK&response_type=token&redirect_uri=nucleus:rest&prompt=none&release_type=prod")
                .build(),
            optionalResultResponseHandlerProvider.forClass(Token.class)
        );
    }

    Optional<Token> userCredentialsGrantedToken(final String emailAddress,
                                                final String password) throws IOException {
        final var firstLocationUrlString = firstLocationUrl()
            .orElseThrow(() -> new IllegalStateException("Couldn't find first Location header value"));
        final var fid = extractFidParameterValueFromUrl(firstLocationUrlString)
            .orElseThrow(() -> new IllegalStateException("Couldn't find fid query parameter value"));
        final var secondLocationPathString = secondLocationPathFromFirstLocationUrl(firstLocationUrlString)
            .orElseThrow(() -> new IllegalStateException("Couldn't find second Location header value"));
        final var thirdLocationUrlString = thirdLocationUrlFromSecondLocationPath(secondLocationPathString)
            .orElseThrow(() -> new IllegalStateException("Couldn't find third Location header value"));
        final var windowLocationString = windowLocationFromThirdLocationUrl(emailAddress, password, thirdLocationUrlString)
            .orElseThrow(() -> new IllegalStateException("Couldn't find window.location"));
        final var fourthLocationUrlString = fourthLocationUrlFromWindowLocation(windowLocationString)
            .orElseThrow(() -> new IllegalStateException("Couldn't find fourth Location header value"));
        requestFourthLocationUrl(fourthLocationUrlString);
        return fetchToken();
    }

    private Optional<String> extractFidParameterValueFromUrl(final String url) {
        try {
            return URLEncodedUtils.parse(new URI(url), StandardCharsets.UTF_8).stream()
                .filter(Objects::nonNull)
                .filter((final var nameValuePair) -> "fid".equals(nameValuePair.getName()))
                .map(NameValuePair::getValue)
                .findFirst()
                .filter((final var fidValue) -> !fidValue.isBlank());
        } catch (final URISyntaxException ignored) {
            return Optional.empty();
        }
    }

    private Optional<String> extractOptionalLocationFromHttpResponse(final HttpResponse httpResponse, final String headerName) {
        return Optional.ofNullable(httpResponse.getAllHeaders())
            .flatMap((final var headerArray) -> Arrays.stream(headerArray)
                .filter(Objects::nonNull)
                .filter((final var header) -> headerName.equals(header.getName()))
                .map(Header::getValue)
                .filter(Objects::nonNull)
                .findFirst()
                .filter((final var locationHeaderValue) -> !locationHeaderValue.isBlank()));
    }
}