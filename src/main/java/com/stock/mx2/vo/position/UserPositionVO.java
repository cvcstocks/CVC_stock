
package com.stock.mx2.vo.position;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.stock.mx2.pojo.UserPositionItem;
import com.stock.mx2.pojo.UserPositionItemFutures;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class UserPositionVO {
    private Integer id;
    private Integer positionType;
    private String positionSn;
    private Integer userId;
    private String nickName;

    private Integer buyType;

    private Integer agentId;

    private String stockName;

    private String stockCode;

    private String stockGid;

    private String stockSpell;

    private String buyOrderId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date buyOrderTime;

    private BigDecimal buyOrderPrice;

    private String sellOrderId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sellOrderTime;

    private BigDecimal sellOrderPrice;
    private BigDecimal profitTargetPrice;
    private BigDecimal stopTargetPrice;
    private Integer orderDirection;
    private Integer orderNum;
    private BigDecimal orderLever;

    private BigDecimal orderFee;
    private BigDecimal orderSpread;
    private BigDecimal orderStayFee;
    private Integer orderStayDays;

    private BigDecimal allProfitAndLose;
    private String now_price;
    private String stockPlate;
    /*點差費金額*/
    private BigDecimal spreadRatePrice;
    /*追加保證金額*/
    private BigDecimal marginAdd;


    /**
     * 买入市值
     */
    private BigDecimal buyTotalAmount;



    /**
     * 卖出市值（预估市值）
     */
    private BigDecimal sellTotalAmount;


    /**
     * 付出成本
     */
    private BigDecimal orderTotalPrice;


    /**
     * 浮动盈亏
     */
    private BigDecimal profitAndLose;


    private List<UserPositionItem> userPositionItemList;

    private List<UserPositionItemFutures> userPositionItemFuturesList;

}
