package com.stock.mx2.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class StockType
{

    @TableId(type = IdType.AUTO)
    private String id;
    private int type;
    private String exchange;
    private String name;

    private Date createTime;


}
