package com.stock.mx2.service.impl;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.SiteLoginLogMapper;
import com.stock.mx2.pojo.SiteLoginLog;
import com.stock.mx2.pojo.User;
import com.stock.mx2.service.ISiteLoginLogService;
import com.stock.mx2.utils.ip.IpUtils;
import com.stock.mx2.utils.ip.JuheIpApi;

import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("iSiteLoginLogService")
public class SiteLoginLogServiceImpl
        implements ISiteLoginLogService {
    private static final Logger log = LoggerFactory.getLogger(SiteLoginLogServiceImpl.class);


    @Autowired
    SiteLoginLogMapper siteLoginLogMapper;


    public ServerResponse saveLog(User user, HttpServletRequest request) {
        SiteLoginLog siteLoginLog = new SiteLoginLog();
        siteLoginLog.setUserId(user.getId());
        siteLoginLog.setUserName(user.getRealName());
        String ips = IpUtils.getIp(request);

        siteLoginLog.setLoginIp(ips);
        String ip_address = JuheIpApi.ip2Add(ips);
        siteLoginLog.setLoginAddress(ip_address);
        siteLoginLog.setAddTime(new Date());
        this.siteLoginLogMapper.insert(siteLoginLog);

        return ServerResponse.createBySuccess();
    }


    public ServerResponse<PageInfo> loginList(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<SiteLoginLog> siteLoginLogs = this.siteLoginLogMapper.loginList(userId);
        PageInfo pageInfo = new PageInfo(siteLoginLogs);
        return ServerResponse.createBySuccess(pageInfo);
    }


    public int deleteByUserId(Integer userId) {
        return this.siteLoginLogMapper.deleteByUserId(userId);
    }

    public ServerResponse del(Integer id, HttpServletRequest request) {
        if (id == null) {
            return ServerResponse.createByErrorMsg("id不能為空");
        }

        int updateCount = this.siteLoginLogMapper.deleteByPrimaryKey(id);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("刪除成功");
        }
        return ServerResponse.createByErrorMsg("刪除失敗");
    }
}
