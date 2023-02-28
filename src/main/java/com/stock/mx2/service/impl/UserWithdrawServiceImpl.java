package com.stock.mx2.service.impl;


import cn.hutool.core.date.DateUtil;
import com.stock.mx2.pojo.*;
import com.stock.mx2.service.*;
import com.github.pagehelper.PageHelper;

import com.github.pagehelper.PageInfo;

import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.dao.AgentUserMapper;

import com.stock.mx2.dao.UserMapper;

import com.stock.mx2.dao.UserWithdrawMapper;

import com.stock.mx2.service.*;
import com.stock.mx2.utils.DateTimeUtil;
import com.stock.mx2.utils.stock.WithDrawUtils;

import java.math.BigDecimal;

import java.util.Date;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;


@Service("iUserWithdrawService")
public class UserWithdrawServiceImpl implements IUserWithdrawService {

    private static final Logger log = LoggerFactory.getLogger(UserWithdrawServiceImpl.class);


    @Autowired
    UserWithdrawMapper userWithdrawMapper;


    @Autowired
    IUserService iUserService;


    @Autowired
    UserMapper userMapper;


    @Autowired
    IAgentUserService iAgentUserService;

    @Autowired
    AgentUserMapper agentUserMapper;

    @Autowired
    IUserPositionService iUserPositionService;

    @Autowired
    IUserBankService iUserBankService;

    @Autowired
    ISiteSettingService iSiteSettingService;

    @Autowired
    ISiteProductService iSiteProductService;


    @Transactional
    public ServerResponse outMoney(String amt, String with_Pwd, HttpServletRequest request) throws Exception {
        if (StringUtils.isBlank(amt)) {
            return ServerResponse.createByErrorMsg("參數不能為空");
        }
        User user = this.iUserService.getCurrentRefreshUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        String w = user.getWithPwd();
        if (w == null) {
            w = "";
        }
        if (with_Pwd == null) {
            with_Pwd = "";
        }
        if (w.equals(with_Pwd)) {
            if (user.getIsLogin().intValue() == 1) {
                return ServerResponse.createByErrorMsg("用戶被鎖定");
            }

//
//            List<UserPosition> userPositions = this.iUserPositionService.findPositionByUserIdAndSellIdIsNull(user.getId());
//
//            if (userPositions.size() > 0) {
//
//                return ServerResponse.createByErrorMsg("有持倉單不能出金");
//
//            }


            if (user.getIsActive() != 2) {

                return ServerResponse.createByErrorMsg("未實名認證");

            }

            UserBank userBank = this.iUserBankService.findUserBankByUserId(user.getId());

            if (userBank == null) {

                return ServerResponse.createByErrorMsg("未添加銀行卡");

            }


            if (user.getAccountType().intValue() == 1) {

                return ServerResponse.createByErrorMsg("模擬用戶不能出金");

            }


            SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();

            if ((new BigDecimal(amt)).compareTo(new BigDecimal(siteSetting.getWithMinAmt().intValue())) == -1) {

                return ServerResponse.createByErrorMsg("出金金額不得低於" + siteSetting.getWithMinAmt() + "元");

            }


            int with_time_begin = siteSetting.getWithTimeBegin().intValue();

            int with_time_end = siteSetting.getWithTimeEnd().intValue();

            SiteProduct siteProduct = iSiteProductService.getProductSetting();
            if (siteProduct.getHolidayDisplay()) {
                return ServerResponse.createByErrorMsg("周末或節假日不能出金！");
            }

            if (!WithDrawUtils.checkIsWithTime(with_time_begin, with_time_end)) {

                return ServerResponse.createByErrorMsg("出金失敗，出金時間在" + with_time_begin + "點 - " + with_time_end + "點 之間");

            }


            BigDecimal futures_user_amt = user.getUserFutAmt();

            if (futures_user_amt.compareTo(new BigDecimal("0")) == -1) {

                return ServerResponse.createByErrorMsg("體現資金不能小於0");

            }


    BigDecimal     enable_amt = user.getEnableAmt().add(user.getWithProfitWallet());
            int compareAmt = enable_amt.compareTo(new BigDecimal(amt));

            if (compareAmt == -1) {

                return ServerResponse.createByErrorMsg("體現失敗，可用資金不足");

            }


          if (user.getWithProfitWallet().compareTo(BigDecimal.ZERO)>0){

              user.setWithProfitWallet(user.getWithProfitWallet().subtract(new BigDecimal(amt)));
              if (user.getWithProfitWallet().compareTo(BigDecimal.ZERO) < 0) {
                  user.setEnableAmt(user.getEnableAmt().add(user.getWithProfitWallet()));
                  user.setWithProfitWallet(BigDecimal.ZERO);
              }
          }else {
              user.setEnableAmt(user.getEnableAmt().subtract(new BigDecimal(amt)));
          }
            user.setUserAmt(user.getEnableAmt());

            int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);

            if (updateUserCount > 0) {

                log.info("修改用戶資金成功");

            } else {

                log.error("修改用戶資金失敗");

                throw new Exception("用戶提現，修改用戶資金失敗");

            }


            UserWithdraw userWithdraw = new UserWithdraw();

            userWithdraw.setUserId(user.getId());

            userWithdraw.setNickName(user.getRealName());

            userWithdraw.setAgentId(user.getAgentId());

            userWithdraw.setWithAmt(new BigDecimal(amt));

            userWithdraw.setApplyTime(new Date());

            userWithdraw.setWithName(userBank.getUserName());
            userWithdraw.setRealName(user.getRealName());

            userWithdraw.setBankNo(userBank.getBankNo());

            userWithdraw.setBankName(userBank.getBankName());

            userWithdraw.setBankAddress(userBank.getBankAddress());

            userWithdraw.setWithStatus(Integer.valueOf(0));


            BigDecimal withfee = siteSetting.getWithFeePercent().multiply(new BigDecimal(amt)).add(new BigDecimal(siteSetting.getWithFeeSingle().intValue()));

            userWithdraw.setWithFee(withfee);


            int insertCount = this.userWithdrawMapper.insert(userWithdraw);

            if (insertCount > 0) {

                return ServerResponse.createBySuccessMsg("提現成功");

            }

            log.error("保存提現記錄失敗");

            throw new Exception("用戶提現，保存提現記錄失敗");
        } else {
            return ServerResponse.createByErrorMsg("提現密碼不正確！！");
        }

    }


    public ServerResponse<PageInfo> findUserWithList(String withStatus, HttpServletRequest request, int pageNum, int pageSize) {

        PageHelper.startPage(pageNum, pageSize);


        User user = this.iUserService.getCurrentUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("請先登錄");
        }

        List<UserWithdraw> userWithdraws = this.userWithdrawMapper.findUserWithList(user.getId(), withStatus);


        PageInfo pageInfo = new PageInfo(userWithdraws);


        return ServerResponse.createBySuccess(pageInfo);

    }


    public ServerResponse userCancel(Integer withId) {

        if (withId == null) {

            return ServerResponse.createByErrorMsg("id不能為空");

        }


        UserWithdraw userWithdraw = this.userWithdrawMapper.selectByPrimaryKey(withId);

        if (userWithdraw == null) {

            return ServerResponse.createByErrorMsg("訂單不存在");

        }


        if (0 != userWithdraw.getWithStatus().intValue()) {

            return ServerResponse.createByErrorMsg("當前訂單不能取消");

        }


        Date applyTime = userWithdraw.getApplyTime();
        long time = new Date().getTime();
        time = time - 20 * 60 * 60 * 1000;
        if (applyTime.getTime() < time) {
            return ServerResponse.createByErrorMsg("請聯繫客服取消您的提領申請");
        }
        userWithdraw.setWithStatus(Integer.valueOf(3));

        userWithdraw.setWithMsg("用戶取消出金");

        int updateCount = this.userWithdrawMapper.updateByPrimaryKeySelective(userWithdraw);

        if (updateCount > 0) {

            log.info("修改用戶提現訂單 {} 狀態成功", withId);


            User user = this.userMapper.selectByPrimaryKey(userWithdraw.getUserId());

            user.setUserAmt(user.getUserAmt().add(userWithdraw.getWithAmt()));

            user.setEnableAmt(user.getEnableAmt().add(userWithdraw.getWithAmt()));

            int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);

            if (updateUserCount > 0) {

                log.info("反還用戶資金，總 {} 可用 {}", user.getUserAmt(), user.getEnableAmt());

                return ServerResponse.createBySuccessMsg("取消成功");

            }

            return ServerResponse.createByErrorMsg("取消失敗");

        }


        log.info("修改用戶提現訂單 {} 狀態失敗", withId);

        return ServerResponse.createByErrorMsg("取消失敗");

    }


    public ServerResponse listByAgent(Integer agentId, String realName, Integer state, HttpServletRequest request, int pageNum, int pageSize) {

        AgentUser currentAgent = this.iAgentUserService.getCurrentAgent(request);

        if (currentAgent == null) {
            return ServerResponse.createByError("請先登錄", null);
        }
        if (agentId != null) {

            AgentUser agentUser = this.agentUserMapper.selectByPrimaryKey(agentId);
            if (agentUser == null) {
                return ServerResponse.createByError("請先登錄", null);
            }
            if (agentUser.getParentId() != currentAgent.getId()) {

                return ServerResponse.createByErrorMsg("不能查詢非下級代理記錄");

            }

        }

        Integer searchId = null;

        if (agentId == null) {

            searchId = currentAgent.getId();

        } else {

            searchId = agentId;

        }


        PageHelper.startPage(pageNum, pageSize);


        List<UserWithdraw> userWithdraws = this.userWithdrawMapper.listByAgent(searchId, realName, state);


        PageInfo pageInfo = new PageInfo(userWithdraws);


        return ServerResponse.createBySuccess(pageInfo);

    }


    @Override
    public ServerResponse<PageInfo> listByAdmin(String phone, Integer agentId, Integer userId, String realName, Integer state, String beginTime, String endTime, HttpServletRequest request, int pageNum, int pageSize, String applyBeginTime, String applyEndTime) {

        PageHelper.startPage(pageNum, pageSize);


        List<UserWithdraw> userWithdraws = this.userWithdrawMapper.listByAdmin(phone, agentId, userId, realName, state, beginTime, endTime, applyBeginTime, applyEndTime);


        PageInfo pageInfo = new PageInfo(userWithdraws);


        return ServerResponse.createBySuccess(pageInfo);

    }


    @Transactional
    public ServerResponse updateState(Integer withId, Integer state, String authMsg) throws Exception {

        UserWithdraw userWithdraw = this.userWithdrawMapper.selectByPrimaryKey(withId);

        if (userWithdraw == null) {

            return ServerResponse.createByErrorMsg("提現訂單不存在");

        }

        if (state.intValue() == 2 &&

                StringUtils.isBlank(authMsg)) {

            return ServerResponse.createByErrorMsg("失敗信息必填");

        }


        if (state.intValue() == 2) {


            User user = this.userMapper.selectByPrimaryKey(userWithdraw.getUserId());

            if (user == null) {

                return ServerResponse.createByErrorMsg("用戶不存在");

            }

            BigDecimal user_amt = user.getUserAmt().add(userWithdraw.getWithAmt());

            log.info("管理員確認提現訂單失敗，返還用戶 {} 總資金，原金額 = {} , 返還后 = {}", new Object[]{user.getId(), user.getUserAmt(), user_amt});

            user.setUserAmt(user_amt);

            BigDecimal user_enable_amt = user.getEnableAmt().add(userWithdraw.getWithAmt());

            log.info("管理員確認提現訂單失敗，返還用戶 {} 可用資金，原金額 = {} , 返還后 = {}", new Object[]{user.getId(), user.getEnableAmt(), user_enable_amt});

            user.setEnableAmt(user_enable_amt);


            int updateCount = this.userMapper.updateByPrimaryKeySelective(user);

            if (updateCount > 0) {

                log.info("提現失敗，返還用戶資金成功！");

            } else {

                log.error("返還用戶資金出錯，拋出異常");

                throw new Exception("修改用戶資金出錯，拋出異常");

            }


            userWithdraw.setWithMsg(authMsg);

        }

        if (userWithdraw.getWithStatus() == 2 && state == 1) {

            //提现失败强行修改为提现成功
            User user = this.userMapper.selectByPrimaryKey(userWithdraw.getUserId());

            if (user == null) {

                return ServerResponse.createByErrorMsg("用戶不存在");

            }

            BigDecimal user_amt = user.getUserAmt().subtract(userWithdraw.getWithAmt());

            if (user_amt.compareTo(BigDecimal.ZERO) < 0) {
                return ServerResponse.createByErrorMsg("当前订单客户现股账户资金不足与扣除，请确认");
            }
            log.info("管理員確認提現訂單强行成功，强行扣除用戶 {} 總資金，原金額 = {} , 强行扣除后 = {}", new Object[]{user.getId(), user.getUserAmt(), user_amt});

            user.setUserAmt(user_amt);

            BigDecimal user_enable_amt = user.getEnableAmt().subtract(userWithdraw.getWithAmt());

            if (user_enable_amt.compareTo(BigDecimal.ZERO) < 0) {
                return ServerResponse.createByErrorMsg("当前订单客户现股账户资金不足与扣除，请确认");
            }
            log.info("管理員確認提現訂單失敗，强行扣除用戶 {} 可用資金，原金額 = {} ,强行扣除后 = {}", new Object[]{user.getId(), user.getEnableAmt(), user_enable_amt});

            user.setEnableAmt(user_enable_amt);


            int updateCount = this.userMapper.updateByPrimaryKeySelective(user);

            if (updateCount > 0) {

                log.info("提現成功，强行扣除用戶資金成功！");

            } else {

                log.error("强行扣除用戶資金出錯，拋出異常");

                throw new Exception("修改用戶資金出錯，拋出異常");

            }


        }


        userWithdraw.setWithStatus((state == 1) ? 1 : 2);


        userWithdraw.setTransTime(new Date());


        int updateCount = this.userWithdrawMapper.updateByPrimaryKeySelective(userWithdraw);

        if (updateCount > 0) {

            return ServerResponse.createBySuccessMsg("操作成功！");

        }

        return ServerResponse.createByErrorMsg("操作失敗！");

    }


    public int deleteByUserId(Integer userId) {
        return this.userWithdrawMapper.deleteByUserId(userId);
    }


    public BigDecimal CountSpWithSumAmtByState(Integer withState) {
        return this.userWithdrawMapper.CountSpWithSumAmtByState(withState);
    }

    public BigDecimal CountSpWithSumAmTodaytByState(Integer withState) {
        return this.userWithdrawMapper.CountSpWithSumAmTodaytByState(withState);
    }

    public ServerResponse deleteWithdraw(Integer withdrawId) {
        if (withdrawId == null) {
            return ServerResponse.createByErrorMsg("刪除id不能為空");
        }
        int updateCount = this.userWithdrawMapper.deleteByPrimaryKey(withdrawId);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("刪除成功");
        }
        return ServerResponse.createByErrorMsg("刪除失敗");
    }

    @Override
    public List<UserWithdraw> exportByAdmin(String phone, Integer agentId, Integer userId, String realName, Integer state, String beginTime, String endTime, HttpServletRequest request,String applyBeginTime,String applyEndTime) {

        return this.userWithdrawMapper.listByAdmin(phone, agentId, userId, realName, state, beginTime, endTime,applyBeginTime,applyEndTime);


    }

    @Override
    public UserWithdraw sumByAdmin(String phone, Integer agentId, Integer userId, String realName, Integer state, String beginTime, String endTime, HttpServletRequest request,String applyBeginTime,String applyEndTime) {
        UserWithdraw sumData = new UserWithdraw();
        List<UserWithdraw> list = this.userWithdrawMapper.listByAdmin(phone, agentId, userId, realName, state, beginTime, endTime,applyBeginTime,applyEndTime);
        BigDecimal amount = BigDecimal.ZERO;
        for (UserWithdraw cashDetail : list) {
            amount = amount.add(cashDetail.getWithAmt());
        }
        sumData.setWithAmt(amount);

        return sumData;
    }


}

