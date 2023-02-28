package com.stock.mx2.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class UserWalletKey {
    /**
     * 用户id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String walletKey;

    private Integer level;

    private Integer useStatus;
    private Integer userId;
    private String userPhone;
    private String userName;
}
