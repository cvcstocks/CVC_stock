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
            return ServerResponse.createByErrorMsg("????????????????????????" + maxOrder + "?????????");
        }
        return ServerResponse.createBySuccess();
    }


    public ServerResponse inMoney(String amt, String payType, HttpServletRequest request) {
        if (StringUtils.isBlank(amt) || StringUtils.isBlank(payType)) {
            return ServerResponse.createByErrorMsg("??????????????????");
        }

        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            return ServerResponse.createByErrorMsg("??????set????????????");
        }
        if ((new BigDecimal(siteSetting.getChargeMinAmt() + "")).compareTo(new BigDecimal(amt)) == 1) {
            return ServerResponse.createByErrorMsg("????????????????????????" + siteSetting.getChargeMinAmt() + "???");
        }


        SiteInfo siteInfo = null;
        ServerResponse serverResponseInfo = this.iSiteInfoService.getInfo();
        if (serverResponseInfo.isSuccess()) {
            siteInfo = (SiteInfo) serverResponseInfo.getData();
            /*if (StringUtils.isBlank(siteInfo.getSiteHost()) ||
                    StringUtils.isBlank(siteInfo.getSiteEmailTo())) {
                return ServerResponse.createByErrorMsg("????????????Host and ToEmail");
            }*/
        } else {
            return serverResponseInfo;
        }

        User user = this.iUserService.getCurrentRefreshUser(request);
        if (user == null){
            return ServerResponse.createBySuccessMsg("????????????");
        }
//        if (user.getIsActive().intValue() != 2) {
//            return ServerResponse.createByErrorMsg("?????????????????????????????????");
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

            log.info("?????????????????????redis token?????????redisSetExResult = {}", redisSetExResult);

            /*SendHTMLMail.send(user, userRecharge, email_token, siteInfo
                    .getSiteHost(), siteInfo.getSiteEmailTo());
            log.info("???????????????????????????????????????");*/
            return ServerResponse.createBySuccessMsg("???????????????????????????");
        }
        return ServerResponse.createByErrorMsg("????????????????????????");
    }


    public ServerResponse findUserRechargeByOrderSn(String orderSn) {
        UserRecharge userRecharge = this.userRechargeMapper.findUserRechargeByOrderSn(orderSn);
        if (userRecharge != null) {
            return ServerResponse.createBySuccess(userRecharge);
        }
        return ServerResponse.createByErrorMsg("?????????????????????");
    }


    @Transactional
    public ServerResponse chargeSuccess(UserRecharge userRecharge) throws Exception {
        log.info("???????????? ?????????????????? id = {}", userRecharge.getId());

        if (userRecharge.getOrderStatus().intValue() != 0) {
            return ServerResponse.createByErrorMsg("??????????????????????????????");
        }


        User user = this.userMapper.selectByPrimaryKey(userRecharge.getUserId());
        if (user == null) {
            return ServerResponse.createByErrorMsg("???????????????");
        }
        BigDecimal userAmt_before = user.getUserAmt();
        BigDecimal enableAmt_before = user.getEnableAmt();
        user.setUserAmt(userAmt_before.add(userRecharge.getPayAmt()));
        user.setEnableAmt(enableAmt_before.add(userRecharge.getPayAmt()));
        int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            log.info("1.????????????????????????");
        } else {
            return ServerResponse.createByErrorMsg("?????????????????????????????????");
        }


        userRecharge.setOrderStatus(Integer.valueOf(1));
        userRecharge.setPayTime(new Date());
        int updateCCount = this.userRechargeMapper.updateByPrimaryKeySelective(userRecharge);
        if (updateCCount > 0) {
            log.info("2.????????????????????????");
        } else {
            throw new Exception("2. ????????????????????????!");
        }


        UserCashDetail ucd = new UserCashDetail();
        ucd.setAgentId(user.getAgentId());
        ucd.setAgentName(user.getAgentName());
        ucd.setUserId(user.getId());
        ucd.setUserName(user.getRealName());
        ucd.setDeType("????????????");
        ucd.setDeAmt(userRecharge.getPayAmt());
        ucd.setDeSummary("???????????????????????????????????????:" + userAmt_before + ",??????????????????:" + user.getUserAmt() + ",???????????????:" + enableAmt_before + ",???????????????:" + user
                .getEnableAmt());

        ucd.setAddTime(new Date());
        ucd.setIsRead(Integer.valueOf(0));
        int insertCount = this.userCashDetailMapper.insert(ucd);
        if (insertCount > 0) {
            return ServerResponse.createBySuccessMsg("???????????????");
        }
        return ServerResponse.createByErrorMsg("????????????");
    }


    public ServerResponse chargeFail(UserRecharge userRecharge) throws Exception {
        if (userRecharge.getOrderStatus().intValue() != 0) {
            return ServerResponse.createByErrorMsg("??????????????????????????????");
        }

        userRecharge.setOrderStatus(Integer.valueOf(2));
        int updateCCount = this.userRechargeMapper.updateByPrimaryKeySelective(userRecharge);
        if (updateCCount > 0) {
            return ServerResponse.createBySuccessMsg("????????????????????????");
        }
        return ServerResponse.createByErrorMsg("??????????????????");
    }


    public ServerResponse chargeCancel(UserRecharge userRecharge) throws Exception {
        if (userRecharge.getOrderStatus().intValue() != 0) {
            return ServerResponse.createByErrorMsg("??????????????????????????????");
        }

        userRecharge.setOrderStatus(Integer.valueOf(3));
        int updateCCount = this.userRechargeMapper.updateByPrimaryKeySelective(userRecharge);
        if (updateCCount > 0) {
            return ServerResponse.createBySuccessMsg("??????????????????");
        }
        return ServerResponse.createByErrorMsg("????????????????????????");
    }


    public ServerResponse<PageInfo> findUserChargeList(String payChannel, String orderStatus, HttpServletRequest request, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        User user = this.iUserService.getCurrentUser(request);
        if (user == null ){
            return ServerResponse.createBySuccessMsg("????????????");
        }


        List<UserRecharge> userRecharges = this.userRechargeMapper.findUserChargeList(user.getId(), payChannel, orderStatus);

        log.info("??????????????????????????? {} ???payChannel = {} , orderStatus = {}??? ?????? = {}", new Object[]{user.getId(), payChannel, orderStatus, userRecharges.size()});

        PageInfo pageInfo = new PageInfo(userRecharges);

        return ServerResponse.createBySuccess(pageInfo);
    }


    public ServerResponse<PageInfo> listByAgent(Integer agentId, String realName, String payChannel, Integer state, HttpServletRequest request, int pageNum, int pageSize) {
        AgentUser currentAgent = this.iAgentUserService.getCurrentAgent(request);
        if (currentAgent ==null){
            return  ServerResponse.createByError("????????????",null);
        }

        if (agentId != null) {
            AgentUser agentUser = this.agentUserMapper.selectByPrimaryKey(agentId);
            if (agentUser.getParentId() != currentAgent.getId()) {
                return ServerResponse.createByErrorMsg("?????????????????????????????????");
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
            return ServerResponse.createByErrorMsg("?????????????????????");
        }
        if (userRecharge.getOrderStatus().intValue() != 0) {
            return ServerResponse.createByErrorMsg("??????????????????????????????????????????");
        }

        User user = this.userMapper.selectById(userRecharge.getUserId());

        if (state.intValue() == 1) {
            userRecharge.setPayAmt(amount);
            if (user == null) {
                return ServerResponse.createByErrorMsg("???????????????");
            }
            BigDecimal user_amt = user.getUserAmt().add(amount);
            log.info("?????????????????????????????????????????? {} ????????????????????? = {} , ????????? = {}", new Object[]{user.getId(), user.getUserAmt(), user_amt});
            user.setUserAmt(user_amt);
            BigDecimal user_enable_amt = user.getEnableAmt().add(amount);
            log.info("?????????????????????????????????????????? {} ???????????????????????? = {} , ????????? = {}", new Object[]{user.getId(), user.getEnableAmt(), user_enable_amt});
            user.setEnableAmt(user_enable_amt);

            int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
            if (updateCount > 0) {
                log.info("???????????????????????????");
            } else {
                log.error("???????????????????????????????????????");
                throw new Exception("???????????????????????????????????????");
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
            return ServerResponse.createBySuccessMsg("???????????????????????????");
        }
        return ServerResponse.createByErrorMsg("???????????????????????????");
    }


    public ServerResponse createOrder(Integer userId, Integer state, Integer amt, String payChannel) {
        if (userId == null || state == null || amt == null) {
            return ServerResponse.createByErrorMsg("??????????????????");
        }

        User user = this.userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMsg("???????????????");
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
            return ServerResponse.createByErrorMsg("?????????????????????");
        }

        int insertCount = this.userRechargeMapper.insert(userRecharge);
        if (insertCount > 0) {
            return ServerResponse.createBySuccessMsg("?????????????????????");
        }
        return ServerResponse.createByErrorMsg("??????????????????????????????");
    }


    public ServerResponse del(Integer cId) {
        if (cId == null) {
            return ServerResponse.createByErrorMsg("id????????????");
        }
        UserRecharge recharge = userRechargeMapper.selectByPrimaryKey(cId);
        if (recharge==null){
            return ServerResponse.createByErrorMsg("???????????????");
        }

        if (recharge.getOrderStatus() == 1 ){
            User user = this.userMapper.selectByPrimaryKey(recharge.getUserId());
            if (user == null) {
                return ServerResponse.createByErrorMsg("???????????????");
            }
            if (user.getUserAmt().compareTo(recharge.getPayAmt())<0){
                return ServerResponse.createByErrorMsg("???????????????????????????");
            }
            if (user.getEnableAmt().compareTo(recharge.getPayAmt())<0){
                return ServerResponse.createByErrorMsg("?????????????????????????????????");
            }
            user.setUserAmt(user.getUserAmt().subtract(recharge.getPayAmt()));
            user.setEnableAmt(user.getEnableAmt().subtract(recharge.getPayAmt()));
            userMapper.updateByPrimaryKey(user);
        }

        int updateCount = this.userRechargeMapper.deleteByPrimaryKey(cId);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("????????????");
        }
        return ServerResponse.createByErrorMsg("????????????");
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
