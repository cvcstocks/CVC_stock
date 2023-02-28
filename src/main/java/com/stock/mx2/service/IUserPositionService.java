package com.stock.mx2.service;

import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.Stock;
import com.stock.mx2.pojo.UserPosition;
import com.stock.mx2.vo.position.AdminPositionVO;
import com.stock.mx2.vo.position.PositionProfitVO;
import com.stock.mx2.vo.position.PositionVO;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public interface IUserPositionService {
    ServerResponse buy(Integer stockId, Integer buyNum, Integer buyType, BigDecimal lever, HttpServletRequest request, @RequestParam("nowPrice") BigDecimal nowPrice) throws Exception;

    ServerResponse sell(String paramString, int paramInt, Integer sellNumber, @RequestParam("nowPrice") BigDecimal nowPrice) throws Exception;

    ServerResponse lock(Integer paramInteger1, Integer paramInteger2, String paramString);

    ServerResponse del(Integer paramInteger);

    /**
     * 此方法棄用
     */
    ServerResponse<PageInfo> findMyPositionByCodeAndSpellV1(String paramString1, String paramString2, Integer paramInteger, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);

    PositionVO findUserPositionAllProfitAndLose(Integer paramInteger);

    List<UserPosition> findPositionByUserIdAndSellIdIsNull(Integer paramInteger);

    List<UserPosition> findPositionByStockCodeAndTimes(int paramInt, String paramString, Integer paramInteger);

    Integer findPositionNumByTimes(int paramInt, Integer paramInteger);

    ServerResponse listByAgent(Integer paramInteger1, Integer paramInteger2, Integer paramInteger3, Integer paramInteger4, String paramString1, String paramString2, String paramString3, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2, Integer buyType);

    ServerResponse getIncome(Integer paramInteger1, Integer paramInteger2, String paramString1, String paramString2, Integer buyType);

    ServerResponse listByAdmin(Integer sum,String sort, String sortColmn, Integer paramInteger1, Integer paramInteger2, Integer paramInteger3, Integer paramInteger4, String paramString1, String paramString2, String paramString3, int paramInt1, int paramInt2, Integer buyType);

    ServerResponse listByAdminNb(Integer sum,String sort, String sortColmn, Integer paramInteger1, Integer paramInteger2, Integer paramInteger3, Integer paramInteger4, String paramString1, String paramString2, String paramString3, int paramInt1, int paramInt2, Integer buyType);


    int CountPositionNum(Integer paramInteger1, Integer paramInteger2);

    BigDecimal CountPositionProfitAndLose();

    BigDecimal CountPositionAllProfitAndLose();

    ServerResponse create(Integer paramInteger1, String paramString1, String paramString2, String paramString3, Integer paramInteger2, Integer paramInteger3, BigDecimal paramInteger4);

    int deleteByUserId(Integer paramInteger);

    void doClosingStayTask();
//  void expireStayUnwindTask();

    ServerResponse closingStayTask(UserPosition paramUserPosition, Integer paramInteger) throws Exception;

    List<Integer> findDistinctUserIdList();

    ServerResponse<PageInfo> findPositionTopList(Integer pageSize);

    ServerResponse findUserPositionByCode(HttpServletRequest paramHttpServletRequest, String stockCode);

    ServerResponse addmargin(String paramString, int paramInt, BigDecimal marginAdd) throws Exception;

    PositionProfitVO getPositionProfitVO(UserPosition position);
    List<PositionProfitVO> getPositionProfitVOList(List<UserPosition> userPositions);


    ServerResponse sumByAdmin(Integer agentId, Integer positionType, Integer state, Integer userId, String positionSn, String beginTime, String endTime, Integer buyType);

    ServerResponse findMyPositionByCodeAndSpellSum(HttpServletRequest request);

    ServerResponse findMyPositionByCodeAndSpellV2(String stockCode, String stockSpell, Integer state, HttpServletRequest request, int pageNum, int pageSize);

    List<AdminPositionVO> listByAdminExport(String sort, String sortColmn, Integer agentId, Integer positionType, Integer state, Integer userId, String positionSn, String beginTime, String endTime, int pageNum, int pageSize, Integer buyType);

    ServerResponse newStockBuy(Stock stock, Integer buyNum, String phone, BigDecimal buyPrice, HttpServletRequest request) throws Exception;

    List<UserPosition> findPositionByUserIdAndSellIdIsNullWhereRz(Integer userId);

    List<UserPosition> findPositionByUserIdAndSellIdIsNullWhereRzAndAll();

    List<Integer> findDistinctUserIdListAndRz();


    ServerResponse updatePrice(String positionSn, BigDecimal buyPrice, Date orderTime);

    void sellByAll(List<PositionProfitVO> positionProfitVOList);

    PositionVO findUserFutPositionAllProfitAndLose(Integer id);
}
