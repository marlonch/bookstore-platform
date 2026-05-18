package com.hub.application.catalog.stock.port.in;

import com.hub.application.catalog.stock.port.in.command.CreateStockCommand;
import com.hub.domain.catalog.stock.Stock;

public interface CreateStockForNewBookUseCase {
    Stock create(CreateStockCommand stockCommand);
}
