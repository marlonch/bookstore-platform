package com.hub.adapters.out.transaction;

import com.hub.application.shared.port.out.TransactionPort;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class SpringTransactionAdapter implements TransactionPort {

    @Override
    @Transactional
    public <T> T execute(Supplier<T> work) {
        return work.get();
    }
}
