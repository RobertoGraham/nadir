package io.github.robertograham.nadir;

import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;

import javax.json.JsonObject;
import java.util.Optional;

/**
 * An exception thrown due to erroneous HTTP responses
 *
 * @since 1.0.0
 */
public final class StryderErrorException extends HttpResponseException {

    private final JsonObject jsonObject;

    StryderErrorException(final int statusCode, final JsonObject jsonObject) {
        super(statusCode, jsonObject.toString());
        this.jsonObject = jsonObject;
    }

    StryderErrorException(final StatusLine statusLine, final Header[] headers) {
        super(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        jsonObject = null;
    }

    /**
     * @return an {@link Optional} of {@link JsonObject} that's non-empty if a JSON object
     * was included in the erroneous HTTP response
     * @since 1.0.0
     */
    public Optional<JsonObject> jsonObject() {
        return Optional.ofNullable(jsonObject);
    }
}
