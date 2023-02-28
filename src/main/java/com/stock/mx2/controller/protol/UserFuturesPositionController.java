package com.stock.mx2.controller.protol;

import com.stock.mx2.annotation.SameUrlData;
import com.stock.mx2.common.ServerResponse;
import javax.servlet.http.HttpServletRequest;

import com.stock.mx2.service.IStockFuturesService;
import com.stock.mx2.service.IUserFuturesPositionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;


@Controller
@RequestMapping({"/user/futures/position/"})
public class UserFuturesPositionController {
    private static final Logger log = LoggerFactory.getLogger(UserFuturesPositionController.class);


    @Autowired
    IUserFuturesPositionService iUserFuturesPositionService;
    @Autowired
    private IStockFuturesService iStockFuturesService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;


    //查詢所有期貨持倉/平倉信息
    @RequestMapping({"index.do"})
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "fuCode") String fuCode) {
        return ServerResponse.createBySuccess(this.iStockFuturesService.querySingleMarket( fuCode));
    }

    @RequestMapping({"stockPosition.do"})
    @ResponseBody
    public ServerResponse newListV2(HttpServletRequest request, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "fuCode", required = false) String stockCode) {
        return this.iUserFuturesPositionService.findMyPositionByCodeAndSpellV2(stockCode, state, request);
    }

    //查詢所有期貨持倉/平倉信息
    @RequestMapping({"wallet.do"})
    @ResponseBody
    public ServerResponse wallet(HttpServletRequest request) {
        return this.iUserFuturesPositionService.wallet(request);
    }

    /**
     * @param stockId
     * @param buyNum
     * @param lever
     * @param direction 1 多 2 空
     * @param request
     * @return
     */
    @RequestMapping({"buy.do"})
    @ResponseBody
    @SameUrlData
    public ServerResponse buy(@RequestParam("stockId") Integer stockId, @RequestParam("buyNum") Integer buyNum,  @RequestParam("lever") BigDecimal lever,@RequestParam("direction")Integer direction
            , HttpServletRequest request) {
        ServerResponse serverResponse = null;

        try {
            serverResponse = this.iUserFuturesPositionService.buy(stockId, buyNum, lever, request,direction);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverResponse;
    }

    //用戶平倉操作
    @RequestMapping({"sell.do"})
    @ResponseBody
    @SameUrlData
    public ServerResponse sell( @RequestParam("positionSn") String positionSn,Integer sellNumber) {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.iUserFuturesPositionService.sell(positionSn, 1,sellNumber);
        } catch (Exception e) {
            log.error("用戶平倉操作 = {}", e);
        }
        return serverResponse;
    }

}
