package com.hub.application.shared.port.out;

import java.util.function.Supplier;

public interface TransactionPort {
    <T> T execute(Supplier<T> work);

}
