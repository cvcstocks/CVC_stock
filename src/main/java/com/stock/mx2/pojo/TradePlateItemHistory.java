package com.stock.mx2.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;


/**
 * 用户合约交易对表
 */
@Data
public class TradePlateItemHistory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String type;

    private String json;

    private Long time;


}
