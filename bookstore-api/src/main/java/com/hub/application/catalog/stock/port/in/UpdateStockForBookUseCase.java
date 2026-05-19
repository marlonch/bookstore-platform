package com.hub.application.catalog.stock.port.in;

import com.hub.application.catalog.stock.port.in.command.UpdateStockCommand;
import com.hub.domain.catalog.stock.Stock;

public interface UpdateStockForBookUseCase {
    Stock updateStockToBook(UpdateStockCommand stockCommand);
}
