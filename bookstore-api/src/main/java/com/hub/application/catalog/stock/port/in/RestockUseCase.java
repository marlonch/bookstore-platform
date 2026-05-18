package com.hub.application.catalog.stock.port.in;

import com.hub.application.catalog.stock.port.in.command.RestockCommand;
import com.hub.domain.catalog.stock.Stock;

public interface RestockUseCase {
    Stock restock(RestockCommand command);
}
