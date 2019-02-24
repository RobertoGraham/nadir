[![Maven Central](https://img.shields.io/maven-central/v/io.github.robertograham/nadir.svg?label=Maven%20Central&style=flat-square)](https://search.maven.org/search?q=g:%22io.github.robertograham%22%20AND%20a:%22nadir%22)

# nadir

## Features

* Get UID of authenticated account
* Get UIDs of other accounts by their usernames

## Usage

### Get UID of authenticated account

```java
import io.github.robertograham.nadir.Account;
import io.github.robertograham.nadir.Nadir;

import java.io.IOException;

public final class Main {

    public static void main(final String[] args) {
        try (final var nadir = Nadir.newNadir("emailAddress", "password")) {
            nadir.accounts()
                .findOneBySessionToken()
                .map(Account::userId)
                .ifPresent(System.out::println);
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }
}
```

### Get UID of other account by its username

```java
import io.github.robertograham.nadir.Account;
import io.github.robertograham.nadir.Nadir;

import java.io.IOException;

public final class Main {

    public static void main(final String[] args) {
        try (final var nadir = Nadir.newNadir("emailAddress", "password")) {
            final var usernameString = "DiegosaursTTV";
            nadir.accounts()
                .findAllBySearchTerms(usernameString)
                .flatMap((final var accountList) -> accountList.stream()
                    .filter((final var account) -> usernameString.equalsIgnoreCase(account.username()))
                    .findFirst()
                    .map(Account::userId))
                .ifPresent(System.out::println);
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }
}
```