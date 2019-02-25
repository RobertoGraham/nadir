package io.github.robertograham.nadir;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.adapter.JsonbAdapter;
import java.io.IOException;
import java.util.Optional;

enum JsonOptionalResultResponseHandlerProvider implements OptionalResultResponseHandlerProvider {

    INSTANCE(
        TokenImpl.Adapter.INSTANCE,
        AccountImpl.Adapter.INSTANCE,
        XSearchResult.Adapter.INSTANCE
    );

    private final ResponseHandler<Optional<String>> stringOptionalHandler;
    private final Jsonb jsonb;

    JsonOptionalResultResponseHandlerProvider(final JsonbAdapter... jsonbAdapters) {
        stringOptionalHandler = responseHandlerFromHttpEntityToOptionalResultMapper((final var httpEntity) ->
            Optional.ofNullable(EntityUtils.toString(httpEntity)));
        jsonb = JsonbBuilder.create(new JsonbConfig()
            .withAdapters(jsonbAdapters));
    }

    @Override
    public ResponseHandler<Optional<String>> forString() {
        return stringOptionalHandler;
    }

    @Override
    public <T> ResponseHandler<Optional<T>> forClass(final Class<T> tClass) {
        return responseHandlerFromHttpEntityToOptionalResultMapper((final var httpEntity) -> {
            final var inputStream = httpEntity.getContent();
            if (inputStream == null)
                return Optional.empty();
            try (inputStream) {
                return Optional.of(jsonb.fromJson(inputStream, tClass));
            }
        });
    }

    private <T> ResponseHandler<Optional<T>> responseHandlerFromHttpEntityToOptionalResultMapper(final HttpEntityToOptionalResultMapper<T> httpEntityToOptionalResultMapper) {
        return response -> {
            final var statusLine = response.getStatusLine();
            final var statusCodeInt = statusLine.getStatusCode();
            final var httpEntity = response.getEntity();
            if (statusCodeInt >= HttpStatus.SC_OK && statusCodeInt < HttpStatus.SC_MULTIPLE_CHOICES)
                return httpEntity == null ?
                    Optional.empty()
                    : httpEntityToOptionalResultMapper.mapHttpEntityToOptionalResult(httpEntity);
            final var inputStream = httpEntity != null ?
                httpEntity.getContent()
                : null;
            if (inputStream == null)
                throw new StryderErrorException(statusLine, response.getAllHeaders());
            try (inputStream;
                 final var jsonReader = Json.createReader(inputStream)) {
                throw new StryderErrorException(statusCodeInt, jsonReader.readObject());
            } catch (final JsonException ignored) {
                throw new StryderErrorException(statusLine, response.getAllHeaders());
            }
        };
    }

    private interface HttpEntityToOptionalResultMapper<T> {

        Optional<T> mapHttpEntityToOptionalResult(final HttpEntity httpEntity) throws IOException;
    }
}