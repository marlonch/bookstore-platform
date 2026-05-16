package com.hub.application.catalog.stock.port.in;

import com.hub.application.catalog.stock.port.in.command.ReserveStockCommand;
import com.hub.domain.catalog.stock.Stock;

public interface ReserveStockUseCase {
    Stock reserve(ReserveStockCommand command);
}
