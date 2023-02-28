package com.stock.mx2.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Qq {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String qq;

    private String name;

    private Integer start;

    private String department;
}
