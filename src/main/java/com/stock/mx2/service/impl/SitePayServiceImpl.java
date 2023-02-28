package com.stock.mx2.service.impl;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.SitePayMapper;
import com.stock.mx2.pojo.SitePay;
import com.stock.mx2.service.ISitePayService;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("iSitePayService")
public class SitePayServiceImpl
        implements ISitePayService {
    @Autowired
    SitePayMapper sitePayMapper;

    public ServerResponse add(SitePay sitePay) {
        if (StringUtils.isBlank(sitePay.getChannelType()) ||
                StringUtils.isBlank(sitePay.getChannelName()) ||
                StringUtils.isBlank(sitePay.getChannelAccount()) || sitePay

                .getChannelMinLimit() == null || sitePay
                .getChannelMaxLimit() == null || sitePay
                .getIsShow() == null || sitePay
                .getIsLock() == null) {
            return ServerResponse.createByErrorMsg("參數不能為空");
        }


        SitePay dbSitePay = this.sitePayMapper.findByChannelType(sitePay.getChannelType());
        if (dbSitePay != null) {
            return ServerResponse.createByErrorMsg("支付類型已存在");
        }

        int insertCount = this.sitePayMapper.insert(sitePay);
        if (insertCount > 0) {
            return ServerResponse.createBySuccessMsg("添加成功");
        }
        return ServerResponse.createByErrorMsg("添加失敗");
    }


    public ServerResponse listByAdmin(String channelType, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<SitePay> sitePays = this.sitePayMapper.listByAdmin(channelType);
        PageInfo pageInfo = new PageInfo(sitePays);

        return ServerResponse.createBySuccess(pageInfo);
    }


    public ServerResponse update(SitePay sitePay) {
        if (sitePay.getId() == null) {
            return ServerResponse.createByErrorMsg("修改id不能為空");
        }

        int updateCount = this.sitePayMapper.updateByPrimaryKeySelective(sitePay);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("修改成功");
        }
        return ServerResponse.createByErrorMsg("修改失敗");
    }


    public ServerResponse del(Integer cId) {
        if (cId == null) {
            return ServerResponse.createByErrorMsg("id不能為空");
        }
        int delCount = this.sitePayMapper.deleteByPrimaryKey(cId);
        if (delCount > 0) {
            return ServerResponse.createBySuccessMsg("刪除成功");
        }
        return ServerResponse.createByErrorMsg("刪除失敗");
    }


    public ServerResponse getPayInfo() {
        List<SitePay> sitePays = this.sitePayMapper.getPayInfo();
        return ServerResponse.createBySuccess(sitePays);
    }


    public ServerResponse getPayInfoById(Integer payId) {
        return ServerResponse.createBySuccess(this.sitePayMapper.selectByPrimaryKey(payId));
    }
}
