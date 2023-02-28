package com.stock.mx2.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;


@Data
public class UserBank {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String userName;

    private String bankName;

    private String bankNo;

    private String bankAddress;

    private String bankImg;

    private String bankPhone;

    private Date addTime;

    private String imgOne;
    private String imgTwo;


}
