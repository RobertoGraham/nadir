package io.github.robertograham.nadir;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement(name = "user")
final class XmlUser implements Account {

    @XmlElement(name = "userId")
    private final long userId;
    @XmlElement(name = "EAID")
    private final String username;

    private XmlUser(final long userId, final String username) {
        this.userId = userId;
        this.username = username;
    }

    private XmlUser() {
        this(-1L, "");
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
        return "XmlUser{" +
            "userId=" + userId +
            ", username='" + username + '\'' +
            '}';
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object)
            return true;
        if (!(object instanceof XmlUser))
            return false;
        final var xmlUser = (XmlUser) object;
        return userId == xmlUser.userId &&
            username.equals(xmlUser.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }
}
