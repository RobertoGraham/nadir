package io.github.robertograham.nadir;

import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;

import javax.json.JsonObject;
import java.util.Optional;

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

    public Optional<JsonObject> jsonObject() {
        return Optional.ofNullable(jsonObject);
    }
}
