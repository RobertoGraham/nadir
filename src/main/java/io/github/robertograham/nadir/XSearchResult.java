package io.github.robertograham.nadir;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.bind.adapter.JsonbAdapter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

final class XSearchResult {

    private final long totalCount;
    private final List<String> friendUserIds;

    private XSearchResult(long totalCount, List<String> friendUserIds) {
        this.totalCount = totalCount;
        this.friendUserIds = friendUserIds;
    }

    public long totalCount() {
        return totalCount;
    }

    public List<String> friendUserIds() {
        return friendUserIds;
    }

    @Override
    public String toString() {
        return "XSearchResult{" +
            "totalCount=" + totalCount +
            ", friendUserIds=" + friendUserIds +
            '}';
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object)
            return true;
        if (!(object instanceof XSearchResult))
            return false;
        final var xSearchResult = (XSearchResult) object;
        return totalCount == xSearchResult.totalCount &&
            friendUserIds.equals(xSearchResult.friendUserIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalCount, friendUserIds);
    }

    enum Adapter implements JsonbAdapter<XSearchResult, JsonObject> {

        INSTANCE;

        @Override
        public JsonObject adaptToJson(final XSearchResult xSearchResult) {
            throw new UnsupportedOperationException();
        }

        @Override
        public XSearchResult adaptFromJson(final JsonObject jsonObject) {
            return new XSearchResult(
                jsonObject.getJsonNumber("totalCount")
                    .longValueExact(),
                Optional.ofNullable(jsonObject.isNull("infoList") ?
                    null
                    : jsonObject.getJsonArray("infoList"))
                    .map((final var jsonArray) -> jsonArray.stream()
                        .filter(Objects::nonNull)
                        .filter((final var jsonValue) -> jsonValue instanceof JsonObject)
                        .map(JsonValue::asJsonObject)
                        .map((final var friendUserJsonObject) -> friendUserJsonObject.getString("friendUserId"))
                        .collect(Collectors.toUnmodifiableList()))
                    .orElseGet(Collections::emptyList)
            );
        }
    }
}
