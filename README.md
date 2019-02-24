### nadir

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