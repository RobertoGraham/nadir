package io.github.robertograham.nadir;

import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;
import java.time.LocalDateTime;
import java.util.Objects;

final class TokenImpl implements Token {

    private final String accessToken;
    private final String tokenType;
    private final LocalDateTime accessTokenExpiresAt;

    private TokenImpl(String accessToken, String tokenType, LocalDateTime accessTokenExpiresAt) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }

    @Override
    public String accessToken() {
        return accessToken;
    }

    @Override
    public String tokenType() {
        return tokenType;
    }

    @Override
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
        if (!(object instanceof TokenImpl))
            return false;
        final var token = (TokenImpl) object;
        return accessToken.equals(token.accessToken) &&
            tokenType.equals(token.tokenType) &&
            accessTokenExpiresAt.equals(token.accessTokenExpiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, tokenType, accessTokenExpiresAt);
    }

    enum Adapter implements JsonbAdapter<TokenImpl, JsonObject> {

        INSTANCE;

        @Override
        public JsonObject adaptToJson(final TokenImpl token) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TokenImpl adaptFromJson(final JsonObject jsonObject) {
            return new TokenImpl(
                jsonObject.getString("access_token"),
                jsonObject.getString("token_type"),
                LocalDateTime.now()
                    .plusSeconds(Long.parseLong(jsonObject.getString("expires_in")))
            );
        }
    }
}
