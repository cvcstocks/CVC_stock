package com.stock.mx2.service.impl;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.stock.mx2.dao.UserRechargeMapper;
import com.stock.mx2.service.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.AgentUserMapper;
import com.stock.mx2.dao.UserCashDetailMapper;
import com.stock.mx2.dao.UserMapper;
import com.stock.mx2.pojo.AgentUser;
import com.stock.mx2.pojo.SiteInfo;
import com.stock.mx2.pojo.SiteSetting;
import com.stock.mx2.pojo.User;
import com.stock.mx2.pojo.UserCashDetail;
import com.stock.mx2.pojo.UserRecharge;
import com.stock.mx2.service.*;
import com.stock.mx2.utils.DateTimeUtil;
import com.stock.mx2.utils.KeyUtils;
import com.stock.mx2.utils.email.SendHTMLMail;
import com.stock.mx2.utils.redis.RedisShardedPoolUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("iUserRechargeService")
public class UserRechargeServiceImpl implements IUserRechargeService {
    private static final Logger log = LoggerFactory.getLogger(UserRechargeServiceImpl.class);

    @Autowired
    UserRechargeMapper userRechargeMapper;

    @Autowired
    IUserService iUserService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    IAgentUserService iAgentUserService;
    @Autowired
    private UserLevelWalletService userLevelWalletService;
    @Autowired
    AgentUserMapper agentUserMapper;
    @Autowired
    ISiteSettingService iSiteSettingService;
    @Autowired
    UserCashDetailMapper userCashDetailMapper;
    @Autowired
    ISiteInfoService iSiteInfoService;

    public ServerResponse checkInMoney(int maxOrder, Integer userId) {
        int count = this.userRechargeMapper.checkInMoney(0, userId);

        if (count > maxOrder) {
            return ServerResponse.createByErrorMsg("一小時內只能發起" + maxOrder + "次入金");
        }
        return ServerResponse.createBySuccess();
    }


    public ServerResponse inMoney(String amt, String payType, HttpServletRequest request) {
        if (StringUtils.isBlank(amt) || StringUtils.isBlank(payType)) {
            return ServerResponse.createByErrorMsg("參數不能為空");
        }

        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            return ServerResponse.createByErrorMsg("設置set未初始化");
        }
        if ((new BigDecimal(siteSetting.getChargeMinAmt() + "")).compareTo(new BigDecimal(amt)) == 1) {
            return ServerResponse.createByErrorMsg("充值金額不得低於" + siteSetting.getChargeMinAmt() + "元");
        }


        SiteInfo siteInfo = null;
        ServerResponse serverResponseInfo = this.iSiteInfoService.getInfo();
        if (serverResponseInfo.isSuccess()) {
            siteInfo = (SiteInfo) serverResponseInfo.getData();
            /*if (StringUtils.isBlank(siteInfo.getSiteHost()) ||
                    StringUtils.isBlank(siteInfo.getSiteEmailTo())) {
                return ServerResponse.createByErrorMsg("請先設置Host and ToEmail");
            }*/
        } else {
            return serverResponseInfo;
        }

        User user = this.iUserService.getCurrentRefreshUser(request);
        if (user == null){
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
//        if (user.getIsActive().intValue() != 2) {
//            return ServerResponse.createByErrorMsg("未實名認證不能發起充值");
//        }


        ServerResponse serverResponse = checkInMoney(10, user.getId());
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }


        UserRecharge userRecharge = new UserRecharge();

        userRecharge.setUserId(user.getId());
        userRecharge.setNickName(user.getRealName());
        userRecharge.setAgentId(user.getAgentId());

        String ordersn = KeyUtils.getRechargeOrderSn();
        userRecharge.setOrderSn(ordersn);

        userRecharge.setPayChannel(payType);
        userRecharge.setPayAmt(new BigDecimal(amt));
        userRecharge.setOrderStatus(Integer.valueOf(0));
        userRecharge.setAddTime(new Date());

        int insertCount = this.userRechargeMapper.insert(userRecharge);
        if (insertCount > 0) {

            String email_token = KeyUtils.getUniqueKey();

            String redisSetExResult = RedisShardedPoolUtils.setEx(email_token, email_token, 300);

            log.info("用戶充值，保存redis token成功，redisSetExResult = {}", redisSetExResult);

            /*SendHTMLMail.send(user, userRecharge, email_token, siteInfo
                    .getSiteHost(), siteInfo.getSiteEmailTo());
            log.info("用戶充值，發送審核郵件成功");*/
            return ServerResponse.createBySuccessMsg("創建支付訂單成功！");
        }
        return ServerResponse.createByErrorMsg("創建支付訂單失敗");
    }


    public ServerResponse findUserRechargeByOrderSn(String orderSn) {
        UserRecharge userRecharge = this.userRechargeMapper.findUserRechargeByOrderSn(orderSn);
        if (userRecharge != null) {
            return ServerResponse.createBySuccess(userRecharge);
        }
        return ServerResponse.createByErrorMsg("找不到充值訂單");
    }


    @Transactional
    public ServerResponse chargeSuccess(UserRecharge userRecharge) throws Exception {
        log.info("充值訂單 確認成功操作 id = {}", userRecharge.getId());

        if (userRecharge.getOrderStatus().intValue() != 0) {
            return ServerResponse.createByErrorMsg("訂單狀態不能重複修改");
        }


        User user = this.userMapper.selectByPrimaryKey(userRecharge.getUserId());
        if (user == null) {
            return ServerResponse.createByErrorMsg("用戶不存在");
        }
        BigDecimal userAmt_before = user.getUserAmt();
        BigDecimal enableAmt_before = user.getEnableAmt();
        user.setUserAmt(userAmt_before.add(userRecharge.getPayAmt()));
        user.setEnableAmt(enableAmt_before.add(userRecharge.getPayAmt()));
        int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            log.info("1.修改用戶資金成功");
        } else {
            return ServerResponse.createByErrorMsg("失敗，修改用户资金失敗");
        }


        userRecharge.setOrderStatus(Integer.valueOf(1));
        userRecharge.setPayTime(new Date());
        int updateCCount = this.userRechargeMapper.updateByPrimaryKeySelective(userRecharge);
        if (updateCCount > 0) {
            log.info("2.修改訂單狀態成功");
        } else {
            throw new Exception("2. 修改訂單狀態失敗!");
        }


        UserCashDetail ucd = new UserCashDetail();
        ucd.setAgentId(user.getAgentId());
        ucd.setAgentName(user.getAgentName());
        ucd.setUserId(user.getId());
        ucd.setUserName(user.getRealName());
        ucd.setDeType("用戶充值");
        ucd.setDeAmt(userRecharge.getPayAmt());
        ucd.setDeSummary("用戶充值成功，充值前總金額:" + userAmt_before + ",充值后總金額:" + user.getUserAmt() + ",充值前可用:" + enableAmt_before + ",充值后可用:" + user
                .getEnableAmt());

        ucd.setAddTime(new Date());
        ucd.setIsRead(Integer.valueOf(0));
        int insertCount = this.userCashDetailMapper.insert(ucd);
        if (insertCount > 0) {
            return ServerResponse.createBySuccessMsg("充值成功！");
        }
        return ServerResponse.createByErrorMsg("充值失敗");
    }


    public ServerResponse chargeFail(UserRecharge userRecharge) throws Exception {
        if (userRecharge.getOrderStatus().intValue() != 0) {
            return ServerResponse.createByErrorMsg("訂單狀態不能重複修改");
        }

        userRecharge.setOrderStatus(Integer.valueOf(2));
        int updateCCount = this.userRechargeMapper.updateByPrimaryKeySelective(userRecharge);
        if (updateCCount > 0) {
            return ServerResponse.createBySuccessMsg("訂單已修改為失敗");
        }
        return ServerResponse.createByErrorMsg("修改出現異常");
    }


    public ServerResponse chargeCancel(UserRecharge userRecharge) throws Exception {
        if (userRecharge.getOrderStatus().intValue() != 0) {
            return ServerResponse.createByErrorMsg("訂單狀態不能重複修改");
        }

        userRecharge.setOrderStatus(Integer.valueOf(3));
        int updateCCount = this.userRechargeMapper.updateByPrimaryKeySelective(userRecharge);
        if (updateCCount > 0) {
            return ServerResponse.createBySuccessMsg("訂單取消成功");
        }
        return ServerResponse.createByErrorMsg("訂單取消出現異常");
    }


    public ServerResponse<PageInfo> findUserChargeList(String payChannel, String orderStatus, HttpServletRequest request, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        User user = this.iUserService.getCurrentUser(request);
        if (user == null ){
            return ServerResponse.createBySuccessMsg("請先登錄");
        }


        List<UserRecharge> userRecharges = this.userRechargeMapper.findUserChargeList(user.getId(), payChannel, orderStatus);

        log.info("充值列表，增加用戶 {} ，payChannel = {} , orderStatus = {}， 數量 = {}", new Object[]{user.getId(), payChannel, orderStatus, userRecharges.size()});

        PageInfo pageInfo = new PageInfo(userRecharges);

        return ServerResponse.createBySuccess(pageInfo);
    }


    public ServerResponse<PageInfo> listByAgent(Integer agentId, String realName, String payChannel, Integer state, HttpServletRequest request, int pageNum, int pageSize) {
        AgentUser currentAgent = this.iAgentUserService.getCurrentAgent(request);
        if (currentAgent ==null){
            return  ServerResponse.createByError("請先登錄",null);
        }

        if (agentId != null) {
            AgentUser agentUser = this.agentUserMapper.selectByPrimaryKey(agentId);
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


        List<UserRecharge> userRecharges = this.userRechargeMapper.listByAgent(searchId, realName, payChannel, state);

        PageInfo pageInfo = new PageInfo(userRecharges);

        return ServerResponse.createBySuccess(pageInfo);
    }


    @Override
    public ServerResponse listByAdmin(String phone,Integer agentId, Integer userId, String realName, Integer state, String beginTime, String endTime, HttpServletRequest request, int pageNum, int pageSize,String applyBeginTime,String applyEndTime) {
        PageHelper.startPage(pageNum, pageSize);


        Timestamp begin_time = null;
        if (StringUtils.isNotBlank(beginTime)) {
            begin_time = DateTimeUtil.searchStrToTimestamp(beginTime);
        }
        Timestamp end_time = null;
        if (StringUtils.isNotBlank(endTime)) {
            end_time = DateTimeUtil.searchStrToTimestamp(endTime);
        }


        List<UserRecharge> userRecharges = this.userRechargeMapper.listByAdmin(phone,agentId, userId, realName, state, begin_time, end_time,applyBeginTime,applyEndTime);

        PageInfo pageInfo = new PageInfo(userRecharges);

        return ServerResponse.createBySuccess(pageInfo);
    }
    @Override
    public List<UserRecharge> exportByAdmin(String phone,Integer agentId, Integer userId, String realName, Integer state, String beginTime, String endTime, HttpServletRequest request,String applyBeginTime,String applyEndTime) {
        Timestamp begin_time = null;
        if (StringUtils.isNotBlank(beginTime)) {
            begin_time = DateTimeUtil.searchStrToTimestamp(beginTime);
        }
        Timestamp end_time = null;
        if (StringUtils.isNotBlank(endTime)) {
            end_time = DateTimeUtil.searchStrToTimestamp(endTime);
        }


        List<UserRecharge> userRecharges = this.userRechargeMapper.listByAdmin(phone,agentId, userId, realName, state, begin_time, end_time,applyBeginTime,applyEndTime);

        return userRecharges;
    }

    @Override
    public UserRecharge sumByAdmin(String phone,Integer agentId, Integer userId, String realName, Integer state, String beginTime, String endTime, HttpServletRequest request,String applyBeginTime,String applyEndTime) {
        Timestamp begin_time = null;
        if (StringUtils.isNotBlank(beginTime)) {
            begin_time = DateTimeUtil.searchStrToTimestamp(beginTime);
        }
        Timestamp end_time = null;
        if (StringUtils.isNotBlank(endTime)) {
            end_time = DateTimeUtil.searchStrToTimestamp(endTime);
        }

        List<UserRecharge> userRecharges = this.userRechargeMapper.listByAdmin( phone,agentId, userId, realName, state, begin_time, end_time,applyBeginTime,applyEndTime);
        UserRecharge userRecharge = new UserRecharge();
        BigDecimal amount = BigDecimal.ZERO;
        for (UserRecharge recharge : userRecharges) {
            amount  = amount.add(recharge.getPayAmt());
        }
        userRecharge.setPayAmt(amount);
        return userRecharge;
    }

    @Transactional
    public ServerResponse updateState(Integer chargeId, Integer state,BigDecimal amount) throws Exception {
        UserRecharge userRecharge = this.userRechargeMapper.selectByPrimaryKey(chargeId);

        if (userRecharge == null) {
            return ServerResponse.createByErrorMsg("充值訂單不存在");
        }
        if (userRecharge.getOrderStatus().intValue() != 0) {
            return ServerResponse.createByErrorMsg("訂單狀態不是下單狀態不能更改");
        }

        User user = this.userMapper.selectById(userRecharge.getUserId());

        if (state.intValue() == 1) {
            userRecharge.setPayAmt(amount);
            if (user == null) {
                return ServerResponse.createByErrorMsg("用戶不存在");
            }
            BigDecimal user_amt = user.getUserAmt().add(amount);
            log.info("管理員確認訂單成功，增加用戶 {} 總資金，原金額 = {} , 增加后 = {}", new Object[]{user.getId(), user.getUserAmt(), user_amt});
            user.setUserAmt(user_amt);
            BigDecimal user_enable_amt = user.getEnableAmt().add(amount);
            log.info("管理員確認訂單成功，增加用戶 {} 可用資金，原金額 = {} , 增加后 = {}", new Object[]{user.getId(), user.getEnableAmt(), user_enable_amt});
            user.setEnableAmt(user_enable_amt);

            int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
            if (updateCount > 0) {
                log.info("修改用戶資金成功！");
            } else {
                log.error("修改用戶資金出錯，拋出異常");
                throw new Exception("修改用戶資金出錯，拋出異常");
            }
        }

        userRecharge.setOrderStatus(Integer.valueOf((state.intValue() == 1) ? 1 : 2));
        userRecharge.setPayTime(new Date());
        int updateCount = this.userRechargeMapper.updateByPrimaryKeySelective(userRecharge);
        int i = userLevelWalletService.updateUserLevel(userRecharge.getUserId());
        if (user.getUserWalletLevel()<=2 &&i>=3){
            user.setExperienceWallet(null);
            user.setEnableAmt(user.getEnableAmt().add(user.getProfitWallet()).add(user.getWithProfitWallet()));
            user.setProfitWallet(BigDecimal.ZERO);
            user.setUserWalletLevel(i);
            user.setExperienceWallet(BigDecimal.ZERO);
            user.setWithProfitWallet(BigDecimal.ZERO);
            userMapper.updateById(user);
        }
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("修改訂單狀態成功！");
        }
        return ServerResponse.createByErrorMsg("修改訂單狀態失敗！");
    }


    public ServerResponse createOrder(Integer userId, Integer state, Integer amt, String payChannel) {
        if (userId == null || state == null || amt == null) {
            return ServerResponse.createByErrorMsg("參數不能為空");
        }

        User user = this.userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMsg("找不到用戶");
        }

        UserRecharge userRecharge = new UserRecharge();
        userRecharge.setUserId(user.getId());
        userRecharge.setNickName(user.getRealName());
        userRecharge.setAgentId(user.getAgentId());

        String ordersn = KeyUtils.getRechargeOrderSn();
        userRecharge.setOrderSn(ordersn);

        userRecharge.setPayChannel(payChannel);
        userRecharge.setPayAmt(new BigDecimal(amt.intValue()));
        userRecharge.setAddTime(new Date());
        userRecharge.setPayTime(new Date());

        if (state.intValue() == 0) {
            userRecharge.setOrderStatus(Integer.valueOf(0));
        } else if (state.intValue() == 1) {
            userRecharge.setOrderSn(payChannel);
            userRecharge.setPayChannel("2");
            userRecharge.setOrderStatus(Integer.valueOf(1));

            user.setUserAmt(user.getUserAmt().add(new BigDecimal(amt.intValue())));
            user.setEnableAmt(user.getEnableAmt().add(new BigDecimal(amt.intValue())));
            this.userMapper.updateByPrimaryKeySelective(user);
        } else if (state.intValue() == 2) {
            userRecharge.setOrderStatus(Integer.valueOf(2));
        } else {
            return ServerResponse.createByErrorMsg("訂單狀態不正確");
        }

        int insertCount = this.userRechargeMapper.insert(userRecharge);
        if (insertCount > 0) {
            return ServerResponse.createBySuccessMsg("生成訂單成功！");
        }
        return ServerResponse.createByErrorMsg("生成訂單失敗，請重試");
    }


    public ServerResponse del(Integer cId) {
        if (cId == null) {
            return ServerResponse.createByErrorMsg("id不能為空");
        }
        UserRecharge recharge = userRechargeMapper.selectByPrimaryKey(cId);
        if (recharge==null){
            return ServerResponse.createByErrorMsg("数据不存在");
        }

        if (recharge.getOrderStatus() == 1 ){
            User user = this.userMapper.selectByPrimaryKey(recharge.getUserId());
            if (user == null) {
                return ServerResponse.createByErrorMsg("找不到用戶");
            }
            if (user.getUserAmt().compareTo(recharge.getPayAmt())<0){
                return ServerResponse.createByErrorMsg("用户余额不足以扣除");
            }
            if (user.getEnableAmt().compareTo(recharge.getPayAmt())<0){
                return ServerResponse.createByErrorMsg("用户可用余额不足以扣除");
            }
            user.setUserAmt(user.getUserAmt().subtract(recharge.getPayAmt()));
            user.setEnableAmt(user.getEnableAmt().subtract(recharge.getPayAmt()));
            userMapper.updateByPrimaryKey(user);
        }

        int updateCount = this.userRechargeMapper.deleteByPrimaryKey(cId);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("刪除成功");
        }
        return ServerResponse.createByErrorMsg("刪除失敗");
    }


    public int deleteByUserId(Integer userId) {
        return this.userRechargeMapper.deleteByUserId(userId);
    }


    public BigDecimal CountChargeSumAmt(Integer chargeState) {
        return this.userRechargeMapper.CountChargeSumAmt(chargeState);
    }

    public BigDecimal CountTotalRechargeAmountByTime(Integer chargeState) {
        return this.userRechargeMapper.CountTotalRechargeAmountByTime(chargeState);
    }


}
