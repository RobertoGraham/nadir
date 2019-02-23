package io.github.robertograham.nadir;

import org.apache.http.client.ResponseHandler;

import java.util.Optional;

interface OptionalResultResponseHandlerProvider {

    ResponseHandler<Optional<String>> forString();

    <T> ResponseHandler<Optional<T>> forClass(Class<T> tClass);
}
