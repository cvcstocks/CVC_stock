package com.stock.mx2.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 新股列对象 newlist
 *
 * @author Tellsea
 * @date 2022-12-26
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
public class Newlist  {

    /**
     * $column.columnComment
     */
    @TableId(type = IdType.AUTO)

    private Long newlistId;

    /**
     * 新股名称
     */
    private String names;

    /**
     * 申购代码
     */
    private String code;

    /**
     * 承销价
     */
    private BigDecimal price;

    /**
     * 状态
     */
    private Integer zt;

    /**
     * 拨券日
     */
    private Long ssrq;

    /**
     * 申购日期
     */
    private Long fxrq;

    /**
     * 市价
     */
    private BigDecimal cityPrice;

    /**
     * 溢价差
     */
    private BigDecimal premiumPrice;

    /**
     * 价差
     */
    private BigDecimal spreadPrice;

    /**
     * 申购截止日
     */
    private Long ofTime;

    /**
     * 预扣款日
     */
    private Long preTime;

    /**
     * 抽籤日
     */
    private Long middleTime;

    /**
     * 最低申购金额
     */
    private BigDecimal lowPrice;

    /**
     * 状态
     */
    private Integer status;

}
