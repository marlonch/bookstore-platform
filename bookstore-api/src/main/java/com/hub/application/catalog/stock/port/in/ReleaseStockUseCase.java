package com.hub.application.catalog.stock.port.in;

import com.hub.application.catalog.stock.port.in.command.ReleaseStockCommand;
import com.hub.domain.catalog.stock.Stock;

public interface ReleaseStockUseCase {
    Stock release(ReleaseStockCommand command);
}
