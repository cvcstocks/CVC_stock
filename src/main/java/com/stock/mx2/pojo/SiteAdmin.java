package com.stock.mx2.pojo;
import lombok.Data;

import java.util.Date;

@Data
public class SiteAdmin {
    private Integer id;
    private String adminName;
    private String adminPwd;
    private String adminPhone;
    private Integer isLock;
    private Date addTime;

    private String token;


}

