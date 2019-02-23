package io.github.robertograham.nadir;

import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;
import java.time.LocalDateTime;
import java.util.Objects;

final class Token {

    private final String accessToken;
    private final String tokenType;
    private final LocalDateTime accessTokenExpiresAt;

    private Token(String accessToken, String tokenType, LocalDateTime accessTokenExpiresAt) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }

    public String accessToken() {
        return accessToken;
    }

    public String tokenType() {
        return tokenType;
    }

    public LocalDateTime accessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }

    @Override
    public String toString() {
        return "Token{" +
            "accessToken='" + accessToken + '\'' +
            ", tokenType='" + tokenType + '\'' +
            ", accessTokenExpiresAt=" + accessTokenExpiresAt +
            '}';
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object)
            return true;
        if (!(object instanceof Token))
            return false;
        final var token = (Token) object;
        return accessToken.equals(token.accessToken) &&
            tokenType.equals(token.tokenType) &&
            accessTokenExpiresAt.equals(token.accessTokenExpiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, tokenType, accessTokenExpiresAt);
    }

    enum Adapter implements JsonbAdapter<Token, JsonObject> {

        INSTANCE;

        @Override
        public JsonObject adaptToJson(final Token token) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Token adaptFromJson(final JsonObject jsonObject) {
            return new Token(
                jsonObject.getString("access_token"),
                jsonObject.getString("token_type"),
                LocalDateTime.now()
                    .plusSeconds(Long.parseLong(jsonObject.getString("expires_in")))
            );
        }
    }
}
