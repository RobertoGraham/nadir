package io.github.robertograham.nadir;

import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;
import java.util.Objects;

final class AccountImpl implements Account {

    private final long id;

    private AccountImpl(long id) {
        this.id = id;
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public String toString() {
        return "AccountImpl{" +
            "id=" + id +
            '}';
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object)
            return true;
        if (!(object instanceof AccountImpl))
            return false;
        final var account = (AccountImpl) object;
        return id == account.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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
