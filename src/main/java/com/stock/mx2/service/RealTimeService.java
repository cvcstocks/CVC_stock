package com.stock.mx2.service;

import com.stock.mx2.common.ServerResponse;

public interface RealTimeService {

    ServerResponse deleteRealTime();

    ServerResponse deleteFuturesRealTime();

    ServerResponse findStock(String paramString);

}
