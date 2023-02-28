package com.stock.mx2.service.impl;


import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.dao.SiteSettingMapper;

import com.stock.mx2.pojo.SiteSetting;

import com.stock.mx2.service.ISiteSettingService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;


@Service("iSiteSettingService")
public class SiteSettingServiceImpl implements ISiteSettingService {

    @Autowired
    SiteSettingMapper siteSettingMapper;


    public SiteSetting getSiteSetting() {

        SiteSetting siteSetting = null;

        List list = this.siteSettingMapper.findAllSiteSetting();

        if (list.size() > 0) {

            siteSetting = (SiteSetting) list.get(0);

        }
        return siteSetting;
    }


    public ServerResponse update(SiteSetting setting) {
        if (setting.getId() == null) {
            return ServerResponse.createByErrorMsg("ID 不能為空");
        }
        SiteSetting siteSetting = this.siteSettingMapper.selectByPrimaryKey(setting.getId());
        if (siteSetting == null) {
            return ServerResponse.createByErrorMsg("查詢不到設置紀錄");
        }

        int updateCount = this.siteSettingMapper.updateByPrimaryKeySelective(setting);

        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("修改成功");
        }
        return ServerResponse.createByErrorMsg("修改失敗");

    }

}
