package io.github.robertograham.nadir;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "users")
final class XmlUsers {

    @XmlElement(name = "user")
    private final List<XmlUser> users;

    private XmlUsers(final List<XmlUser> users) {
        this.users = users;
    }

    private XmlUsers() {
        this(new ArrayList<>());
    }

    private List<XmlUser> getUsers() {
        return users;
    }

    List<XmlUser> users() {
        return List.copyOf(users);
    }

    @Override
    public String toString() {
        return "XmlUsers{" +
            "users=" + users +
            '}';
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object)
            return true;
        if (!(object instanceof XmlUsers))
            return false;
        final var xmlUsers = (XmlUsers) object;
        return users.equals(xmlUsers.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users);
    }
}
