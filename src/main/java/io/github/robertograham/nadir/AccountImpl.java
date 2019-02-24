package io.github.robertograham.nadir;

import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;
import java.util.Objects;

final class AccountImpl implements Account {

    private final long userId;
    private final String username;

    private AccountImpl(long userId) {
        this.userId = userId;
        this.username = "";
    }

    @Override
    public long userId() {
        return userId;
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public String toString() {
        return "AccountImpl{" +
            "userId=" + userId +
            ", username='" + username + '\'' +
            '}';
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object)
            return true;
        if (!(object instanceof AccountImpl))
            return false;
        final var accountImpl = (AccountImpl) object;
        return userId == accountImpl.userId &&
            username.equals(accountImpl.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }

    enum Adapter implements JsonbAdapter<AccountImpl, JsonObject> {

        INSTANCE;

        @Override
        public JsonObject adaptToJson(final AccountImpl account) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AccountImpl adaptFromJson(final JsonObject jsonObject) {
            return new AccountImpl(jsonObject.getJsonObject("pid")
                .getJsonNumber("pidId")
                .longValueExact());
        }
    }
}
