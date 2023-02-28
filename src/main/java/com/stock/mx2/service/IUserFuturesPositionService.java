package com.stock.mx2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.UserPosition;
import com.stock.mx2.pojo.UserPositionFutures;
import com.stock.mx2.vo.position.AdminPositionVO;
import com.stock.mx2.vo.position.PositionProfitVO;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface IUserFuturesPositionService  extends IService<UserPositionFutures> {
    ServerResponse findUserFuturesPositionByCode(HttpServletRequest request, String fuCode);

    ServerResponse wallet(HttpServletRequest request);

    ServerResponse buy(Integer stockId, Integer buyNum, BigDecimal lever, HttpServletRequest request,Integer direction) throws Exception;


    ServerResponse sell(String paramString, int paramInt, Integer sellNumber) throws Exception;

     List<UserPositionFutures> findPositionByStockCodeAndTimes(int minuteTimes, String stockCode, Integer userId);

    Integer findPositionNumByTimes(int paramInt, Integer paramInteger);

    List<Integer> findDistinctUserIdListAndRz();


    List<UserPositionFutures> findPositionByUserIdAndSellIdIsNullWhereRzAndAll();


    List<PositionProfitVO> getPositionProfitVOList(List<UserPositionFutures> userPositions,BigDecimal trqPrice,BigDecimal trxPrice);

    void sellByAll(List<PositionProfitVO> positionProfitVOList1,BigDecimal trqPrice,BigDecimal trxPrice) ;

    List<UserPositionFutures> findPositionByUserIdAndSellIdIsNullWhereRz(Integer userId);

    PositionProfitVO getPositionProfitVO(UserPositionFutures position,BigDecimal nowPrice);

    ServerResponse updatePrice(String positionSn, BigDecimal buyOrderPrice, Date date);

    ServerResponse del(Integer positionId);

    ServerResponse lock(Integer positionId, Integer state, String lockMsg);

    List<AdminPositionVO> listByAdminExport(String sort, String sortColmn, Integer agentId, Integer positionType, Integer state, Integer userId, String positionSn, String beginTime, String endTime, int pageNum, int pageSize, Integer buyType);

    ServerResponse listByAdmin(Integer sum, String sort, String sortColmn, Integer agentId, Integer positionType, Integer state, Integer userId, String positionSn, String beginTime, String endTime, int pageNum, int pageSize, Integer buyType);

    ServerResponse listByAdminNb(Integer sum, String sort, String sortColmn, Integer agentId, Integer positionType, Integer state, Integer userId, String positionSn, String beginTime, String endTime, int pageNum, int pageSize, Integer buyType);

    ServerResponse findMyPositionByCodeAndSpellV2(String stockCode, Integer state, HttpServletRequest request);

}
