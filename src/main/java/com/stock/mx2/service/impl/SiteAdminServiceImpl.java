package com.stock.mx2.service.impl;

import com.stock.mx2.dao.SiteAdminMapper;
import com.github.pagehelper.PageHelper;

import com.github.pagehelper.PageInfo;

import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.pojo.SiteAdmin;

import com.stock.mx2.pojo.UserRecharge;

import com.stock.mx2.service.IAgentUserService;

import com.stock.mx2.service.ISiteAdminService;

import com.stock.mx2.service.IStockService;

import com.stock.mx2.service.IUserPositionService;

import com.stock.mx2.service.IUserRechargeService;

import com.stock.mx2.service.IUserService;

import com.stock.mx2.service.IUserWithdrawService;

import com.stock.mx2.utils.PropertiesUtil;

import com.stock.mx2.utils.redis.CookieUtils;
import com.stock.mx2.utils.redis.JsonUtil;
import com.stock.mx2.utils.redis.RedisConst;
import com.stock.mx2.utils.redis.RedisShardedPoolUtils;

import com.stock.mx2.vo.admin.AdminCountVO;

import java.math.BigDecimal;

import java.util.Date;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;


@Service("iSiteAdminServiceImpl")

public class SiteAdminServiceImpl implements ISiteAdminService {
    private static final Logger log = LoggerFactory.getLogger(SiteAdminServiceImpl.class);


    @Autowired

    SiteAdminMapper siteAdminMapper;


    @Autowired

    IUserRechargeService iUserRechargeService;


    @Autowired

    IUserService iUserService;


    @Autowired

    IUserWithdrawService iUserWithdrawService;


    @Autowired

    IUserPositionService iUserPositionService;

    @Autowired

    IAgentUserService iAgentUserService;

    @Autowired

    IStockService iStockService;


    public ServerResponse login(String adminPhone, String adminPwd, String verifyCode, HttpServletRequest request) {

        if (StringUtils.isBlank(verifyCode)) {

            return ServerResponse.createByErrorMsg("驗證碼不能為空");

        }

        String original = (String) request.getSession().getAttribute("KAPTCHA_SESSION_KEY");

        /*if (!verifyCode.equalsIgnoreCase(original)) {

            return ServerResponse.createByErrorMsg("驗證碼錯誤");

        }*/


        if (StringUtils.isBlank(adminPhone) || StringUtils.isBlank(adminPwd)) {

            return ServerResponse.createByErrorMsg("參數不能為空");

        }


        SiteAdmin siteAdmin = this.siteAdminMapper.login(adminPhone, adminPwd);

        if (siteAdmin == null) {

            return ServerResponse.createByErrorMsg("賬號密碼錯誤");

        }


        if (siteAdmin.getIsLock().intValue() == 1) {

            return ServerResponse.createByErrorMsg("賬號已被鎖定");

        }


        siteAdmin.setAdminPwd(null);
        HttpSession httpSession = request.getSession();
        String token = RedisConst.getAdminRedisKey(httpSession.getId());

        String str = RedisShardedPoolUtils.setEx(token,
                    JsonUtil.obj2String(siteAdmin), 5400);


        siteAdmin.setToken(token);
        return ServerResponse.createBySuccess(siteAdmin);

    }


    public ServerResponse<PageInfo> listByAdmin(String adminName, String adminPhone, HttpServletRequest request, int pageNum, int pageSize) {

        PageHelper.startPage(pageNum, pageSize);


        String superAdmin = PropertiesUtil.getProperty("admin.super.name");


        List<SiteAdmin> siteAdmins = this.siteAdminMapper.listByAdmin(adminName, adminPhone, superAdmin);


        PageInfo pageInfo = new PageInfo(siteAdmins);

        return ServerResponse.createBySuccess(pageInfo);

    }


    public ServerResponse authCharge(String token, Integer state, String orderSn) {

        if (StringUtils.isBlank(token) || state == null || StringUtils.isBlank(orderSn)) {

            return ServerResponse.createByErrorMsg("參數不能為空");

        }


        String redis_token = RedisShardedPoolUtils.get(token);

        if (StringUtils.isBlank(redis_token)) {

            return ServerResponse.createByErrorMsg("token錯誤或已過有效期");

        }


        ServerResponse serverResponse = this.iUserRechargeService.findUserRechargeByOrderSn(orderSn);

        if (!serverResponse.isSuccess()) {

            return serverResponse;

        }


        UserRecharge userRecharge = (UserRecharge) serverResponse.getData();

        ServerResponse returnResponse = null;

        try {

            if (state.intValue() == 1) {

                returnResponse = this.iUserRechargeService.chargeSuccess(userRecharge);

            } else if (state.intValue() == 2) {

                returnResponse = this.iUserRechargeService.chargeFail(userRecharge);

            } else if (state.intValue() == 3) {

                returnResponse = this.iUserRechargeService.chargeCancel(userRecharge);

            } else {

                return ServerResponse.createByErrorMsg("狀態不對，不做處理");

            }

        } catch (Exception e) {

            log.error("email 審核入金狀態出錯，錯誤信息 = {}", e);

        }

        return returnResponse;

    }


    public ServerResponse updateLock(Integer adminId) {

        SiteAdmin siteAdmin = this.siteAdminMapper.selectByPrimaryKey(adminId);

        if (siteAdmin == null) {

            return ServerResponse.createByErrorMsg("管理員不存在");

        }

        if (siteAdmin.getIsLock().intValue() == 0) {

            siteAdmin.setIsLock(Integer.valueOf(1));

        } else {

            siteAdmin.setIsLock(Integer.valueOf(0));

        }

        int updateCount = this.siteAdminMapper.updateByPrimaryKeySelective(siteAdmin);

        if (updateCount > 0) {

            return ServerResponse.createBySuccessMsg("修改成功");

        }

        return ServerResponse.createByErrorMsg("修改失敗");

    }


    public ServerResponse add(SiteAdmin siteAdmin) {

        if (StringUtils.isBlank(siteAdmin.getAdminName()) ||

                StringUtils.isBlank(siteAdmin.getAdminPhone()) ||

                StringUtils.isBlank(siteAdmin.getAdminPwd()) || siteAdmin

                .getIsLock() == null) {

            return ServerResponse.createByErrorMsg("參數不能為空");

        }


        SiteAdmin siteAdmin1 = this.siteAdminMapper.findAdminByName(siteAdmin.getAdminName());

        if (siteAdmin1 != null) {

            return ServerResponse.createByErrorMsg("管理名存在");

        }

        SiteAdmin siteAdmin2 = this.siteAdminMapper.findAdminByPhone(siteAdmin.getAdminPhone());

        if (siteAdmin2 != null) {

            return ServerResponse.createByErrorMsg("手機號存在");

        }


        SiteAdmin dbadmin = new SiteAdmin();

        dbadmin.setAdminName(siteAdmin.getAdminName());

        dbadmin.setAdminPhone(siteAdmin.getAdminPhone());

        dbadmin.setAdminPwd(siteAdmin.getAdminPwd());

        dbadmin.setIsLock(siteAdmin.getIsLock());

        dbadmin.setAddTime(new Date());


        int insertCount = this.siteAdminMapper.insert(dbadmin);

        if (insertCount > 0) {

            return ServerResponse.createBySuccessMsg("添加成功");

        }

        return ServerResponse.createByErrorMsg("添加失敗");

    }


    public ServerResponse update(SiteAdmin siteAdmin) {

        if (siteAdmin.getId() == null) {

            return ServerResponse.createByErrorMsg("修改id不能為空");

        }


        int updateCount = this.siteAdminMapper.updateByPrimaryKeySelective(siteAdmin);

        if (updateCount > 0) {

            return ServerResponse.createBySuccessMsg("修改成功");

        }

        return ServerResponse.createByErrorMsg("修改失敗");

    }

    public ServerResponse deleteAdmin(Integer adminId) {

        if (adminId == null) {
            return ServerResponse.createByErrorMsg("刪除id不能為空");
        }
        int updateCount = this.siteAdminMapper.deleteByPrimaryKey(adminId);

        if (updateCount > 0) {

            return ServerResponse.createBySuccessMsg("刪除成功");

        }

        return ServerResponse.createByErrorMsg("刪除失敗");

    }


    public SiteAdmin findAdminByName(String name) {
        return this.siteAdminMapper.findAdminByName(name);
    }


    public SiteAdmin findAdminByPhone(String phone) {
        return this.siteAdminMapper.findAdminByPhone(phone);
    }


    public ServerResponse count() {

        AdminCountVO adminCountVO = new AdminCountVO();


        int user_sp_num = this.iUserService.CountUserSize(Integer.valueOf(0));

        int user_moni_num = this.iUserService.CountUserSize(Integer.valueOf(1));

        adminCountVO.setUser_sp_num(user_sp_num);

        adminCountVO.setUser_moni_num(user_moni_num);


        int agent_num = this.iAgentUserService.CountAgentNum();

        adminCountVO.setAgent_num(agent_num);


        BigDecimal user_sp_sum_amt = this.iUserService.CountUserAmt(Integer.valueOf(0));

        BigDecimal user_sp_sum_enable = this.iUserService.CountEnableAmt(Integer.valueOf(0));

        adminCountVO.setUser_sp_sum_amt(user_sp_sum_amt);

        adminCountVO.setUser_sp_sum_enable(user_sp_sum_enable);

        //累計充值金額
        BigDecimal charge_sum_amt = this.iUserRechargeService.CountChargeSumAmt(Integer.valueOf(1));
        //今日充值金額
        BigDecimal charge_today_sum_amt = this.iUserRechargeService.CountTotalRechargeAmountByTime(Integer.valueOf(1));

        //累計提現金額
        BigDecimal sp_withdraw_sum_amt_success = this.iUserWithdrawService.CountSpWithSumAmtByState(Integer.valueOf(1));

        //今日提現金額
        BigDecimal sp_withdraw_sum_today_amt_success = this.iUserWithdrawService.CountSpWithSumAmTodaytByState(Integer.valueOf(1));

        BigDecimal sp_withdraw_sum_amt_apply = this.iUserWithdrawService.CountSpWithSumAmtByState(Integer.valueOf(0));

        adminCountVO.setCharge_sum_amt(charge_sum_amt);
        adminCountVO.setCharge_today_sum_amt(charge_today_sum_amt);

        adminCountVO.setSp_withdraw_sum_amt_success(sp_withdraw_sum_amt_success);
        adminCountVO.setSp_withdraw_sum_today_amt_success(sp_withdraw_sum_today_amt_success);

        adminCountVO.setSp_withdraw_sum_amt_apply(sp_withdraw_sum_amt_apply);


        int sp_position_num = this.iUserPositionService.CountPositionNum(Integer.valueOf(1), Integer.valueOf(0));

        int sp_pc_position_num = this.iUserPositionService.CountPositionNum(Integer.valueOf(2), Integer.valueOf(0));

        adminCountVO.setSp_position_num(sp_position_num);

        adminCountVO.setSp_pc_position_num(sp_pc_position_num);


        BigDecimal sp_profit_and_lose = this.iUserPositionService.CountPositionProfitAndLose();

        BigDecimal sp_all_profit_and_lose = this.iUserPositionService.CountPositionAllProfitAndLose();

        adminCountVO.setSp_profit_and_lose(sp_profit_and_lose);

        adminCountVO.setSp_all_profit_and_lose(sp_all_profit_and_lose);


        int stock_num = this.iStockService.CountStockNum();

        int stock_show_num = this.iStockService.CountShowNum(Integer.valueOf(0));

        int stock_un_lock_num = this.iStockService.CountUnLockNum(Integer.valueOf(0));

        adminCountVO.setStock_num(stock_num);

        adminCountVO.setStock_show_num(stock_show_num);

        adminCountVO.setStock_un_lock_num(stock_un_lock_num);

        return ServerResponse.createBySuccess(adminCountVO);
    }


    public static void main(String[] args) {
        System.out.println(RedisShardedPoolUtils.   get("1"));
        System.out.println(RedisShardedPoolUtils.get("2"));
    }

}
