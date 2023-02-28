package com.stock.mx2.controller;


import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.pojo.SiteSpread;
import com.stock.mx2.pojo.User;
import com.stock.mx2.service.ISiteAmtTransLogService;
import com.stock.mx2.service.ISiteSpreadService;
import com.stock.mx2.service.IUserService;

import com.stock.mx2.utils.PropertiesUtil;

import com.stock.mx2.utils.redis.CookieUtils;

import com.stock.mx2.utils.redis.JsonUtil;

import com.stock.mx2.utils.redis.RedisConst;

import com.stock.mx2.utils.redis.RedisShardedPoolUtils;

import com.stock.mx2.vo.user.UserLoginResultVO;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;


@Controller
@RequestMapping({"/api/user/"})
public class UserApiController {
    private static final Logger log = LoggerFactory.getLogger(UserApiController.class);

    @Autowired
    IUserService iUserService;

    @Autowired
    ISiteSpreadService iSiteSpreadService;

    @Autowired
    private ISiteAmtTransLogService siteAmtTransLogService;
    //註冊
    @RequestMapping(value = {"reg.do"}, method = {RequestMethod.POST})
    @ResponseBody
    public ServerResponse reg(@RequestParam("agentCode") String agentCode, @RequestParam("phone") String phone, @RequestParam(value = "yzmCode", defaultValue = "") String yzmCode, @RequestParam("userPwd") String userPwd, HttpServletRequest httpServletRequest) {
        return this.iUserService.reg(yzmCode, agentCode, phone, userPwd, httpServletRequest);
}


    public static void main(String[] args) {

    }
    //登錄
    @RequestMapping(value = {"login.do"}, method = {RequestMethod.POST})
    @ResponseBody
    public ServerResponse login(@RequestParam("phone") String phone, @RequestParam("userPwd") String userPwd, HttpSession httpSession, HttpServletRequest request, HttpServletResponse response) {
        String pc_cookie_name = PropertiesUtil.getProperty("user.cookie.name");
        String token = System.currentTimeMillis()+phone;
        ServerResponse serverResponse = this.iUserService.login(phone, userPwd, request);
        if (serverResponse.isSuccess()) {
            String redisSetExResult = RedisShardedPoolUtils.setEx(token, JsonUtil.obj2String(serverResponse.getData()), 999999999);
            log.info("redis setex user result : {}", redisSetExResult);
            UserLoginResultVO resultVO = new UserLoginResultVO();
            resultVO.setKey(pc_cookie_name);
            resultVO.setToken(token);
            return ServerResponse.createBySuccess("登錄成功", resultVO);
        }
        return serverResponse;
    }

    //註銷
    @RequestMapping({"logout.do"})
    @ResponseBody
    public ServerResponse logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String cookie_name = PropertiesUtil.getProperty("user.cookie.name");
        String header = httpServletRequest.getHeader(cookie_name);
//        String logintoken = CookieUtils.readLoginToken(httpServletRequest, cookie_name);
        log.info("用戶 token = {} ,退出登陸", header);
        RedisShardedPoolUtils.del(header);
//        CookieUtils.delLoginToken(httpServletRequest, httpServletResponse, cookie_name);
        return ServerResponse.createBySuccess();
    }

    //查詢手機號是否存在
    @RequestMapping({"checkPhone.do"})
    @ResponseBody
    public ServerResponse checkPhone(String phoneNum) {
        return this.iUserService.checkPhone(phoneNum);
    }

    //找回密碼
    @RequestMapping({"updatePwd.do"})
    @ResponseBody
    public ServerResponse updatePwd(String phoneNum, String code, String newPwd) {
        return this.iUserService.updatePwd(phoneNum, code, newPwd);
    }


    //分頁查詢日誌管理 所有資金互轉記錄信息及模糊查詢
    @RequestMapping({"transList.do"})
    @ResponseBody
    public ServerResponse transList(HttpServletRequest httpServletRequest,  @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = iUserService.getCurrentUser(httpServletRequest);

        if (user == null ){
            return ServerResponse.createBySuccessMsg("請先登錄");
        }

        return this.siteAmtTransLogService.transList(user.getId(), null, pageNum, pageSize);
    }
    /**
     * 查詢點差費率
     * @author lr
     * @date 2020/07/01
     * applies：漲跌幅
     * turnover：成交額
     * code:股票代碼
     * unitprice：股票單價
     **/
    @RequestMapping({"findSpreadRateOne.do"})
    @ResponseBody
    public ServerResponse findSpreadRateOne(BigDecimal applies, BigDecimal turnover, String code, BigDecimal unitprice) {
        SiteSpread siteSpread = this.iSiteSpreadService.findSpreadRateOne(applies,turnover,code,unitprice);
        return ServerResponse.createBySuccess("獲取成功", siteSpread);
    }


}

