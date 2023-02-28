package com.stock.mx2.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.*;
import com.stock.mx2.pojo.*;
import com.stock.mx2.service.IFundsApplyService;
import com.stock.mx2.service.IFundsSettingService;
import com.stock.mx2.service.IUserService;
import com.stock.mx2.utils.DateTimeUtil;
import com.stock.mx2.utils.KeyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 配資申請
 * @author lr
 * @date 2020/07/24
 */
@Service("IFundsApplyService")
public class FundsApplyServiceImpl implements IFundsApplyService {
    private static final Logger log = LoggerFactory.getLogger(FundsApplyServiceImpl.class);

    @Resource
    private FundsApplyMapper fundsApplyMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    UserCashDetailMapper userCashDetailMapper;
    @Autowired
    FundsTradingAccountMapper fundsTradingAccountMapper;

    @Autowired
    IUserService iUserService;
    @Autowired
    IFundsSettingService iFundsSettingService;


    @Transactional
    public ServerResponse insert(FundsApply model, HttpServletRequest request) throws Exception {
        int ret = 0;
        if (model == null) {
            return ServerResponse.createBySuccessMsg("操作異常，請稍後再試！");
        }
        User user = this.iUserService.getCurrentRefreshUser(request);
        if(user == null){
            return ServerResponse.createBySuccessMsg("請登錄後再操作");
        }
        BigDecimal user_enable_amt = user.getEnableAmt();
        //支付金額=保證金+管理費
        BigDecimal pay_amount = model.getMargin().add(model.getManageFee());
        int compareUserAmtInt = user_enable_amt.compareTo(pay_amount);
        log.info("用戶可用金額 = {}  實際購買金額 =  {} 比較結果 = {} ", user_enable_amt, pay_amount, compareUserAmtInt);
        if (compareUserAmtInt == -1) {
            return ServerResponse.createByErrorMsg("申請失敗，可用金額小於" + pay_amount + "元");
        }

        //自動生成訂單編號
        model.setOrderNumber(KeyUtils.getUniqueKey());
        model.setPayAmount(pay_amount);
        ret = fundsApplyMapper.insert(model);
        if(ret>0){
            //修改用戶可用餘額= 當前餘額-支付金額
            BigDecimal reckon_enable = user_enable_amt.subtract(pay_amount);
            user.setEnableAmt(reckon_enable);
            int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
            if (updateUserCount > 0) {
                log.info("【用戶交易下單】修改用戶金額成功");
                UserCashDetail ucd = new UserCashDetail();
                ucd.setPositionId(model.getId());
                ucd.setAgentId(user.getAgentId());
                ucd.setAgentName(user.getAgentName());
                ucd.setUserId(user.getId());
                ucd.setUserName(user.getRealName());
                ucd.setDeType("配資凍結");
                ucd.setDeAmt(model.getPayAmount().multiply(new BigDecimal("-1")));
                ucd.setDeSummary("申請按天配資:" + model.getOrderNumber() + "，凍結金額：" + model.getPayAmount().multiply(new BigDecimal("-1")) );
                ucd.setAddTime(new Date());
                ucd.setIsRead(Integer.valueOf(0));
                int insertSxfCount = this.userCashDetailMapper.insert(ucd);
                if (insertSxfCount > 0) {
                    log.info("【按天配資】申請成功");
                }

            } else {
                log.error("【按天配資】修改用戶金額出錯");
                throw new Exception("【按天配資】修改用戶金額出錯");
            }
            return ServerResponse.createBySuccessMsg("申請成功！");
        } else {
            return ServerResponse.createBySuccessMsg("申請失敗，請稍後重試！");
        }
    }

    @Override
    public int update(FundsApply model) {
        int ret = fundsApplyMapper.update(model);
        return ret>0 ? ret: 0;
    }

    /**
     * 配資申請-保存
     */
    @Override
    public ServerResponse save(FundsApply model) {
        int ret = 0;
        if(model!=null && model.getId()>0){
            ret = fundsApplyMapper.update(model);
        } else{
            ret = fundsApplyMapper.insert(model);
        }
        if(ret>0){
            return ServerResponse.createBySuccessMsg("操作成功");
        }
        return ServerResponse.createByErrorMsg("操作失敗");
    }

    /**
     * 配資申請-審核
     */
    /*     */   @Transactional
    /*     */   public ServerResponse audit(FundsApply model, HttpServletRequest request) throws Exception {
        /* 135 */     FundsApply fundsApply = this.fundsApplyMapper.load(model.getId().intValue());
        /* 136 */     int ret = 0;
        /* 137 */     if (model != null && model.getId().intValue() > 0) {
            /* 138 */       User user = this.userMapper.selectByPrimaryKey(fundsApply.getUserId());
            /*     */
            /* 140 */       if (model.getStatus().intValue() == 1) {
                /* 141 */         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                /* 142 */         String begtime = df.format(new Date()).split(" ")[0] + " 0:00:00";
                /* 143 */         Date date = DateTimeUtil.strToDate(begtime);
                /* 144 */         Date begDate = DateTimeUtil.addDay(date, 1);
                /* 145 */         model.setBeginTime(begDate);
                /* 146 */         String endtime = df.format(new Date()).split(" ")[0] + " 23:59:59";
                /* 147 */         Date endDate = DateTimeUtil.strToDate(endtime);
                /* 148 */         endDate = DateTimeUtil.addDay(endDate, model.getTradersCycle().intValue() + 1);
                /* 149 */         model.setEndTime(endDate);
                /* 150 */         model.setEnabledTradingAmount(fundsApply.getTotalTradingAmount());
                /* 151 */         FundsSetting fundsSetting = this.iFundsSettingService.getFundsSetting();
                /*     */
                /* 153 */         BigDecimal lineUnwind = fundsApply.getMargin().multiply(fundsSetting.getDaysUnwind()).add(fundsApply.getFundsAmount()).setScale(2, 4);
                /* 154 */         model.setLineUnwind(lineUnwind);
                /*     */
                /* 156 */         BigDecimal lineWarning = fundsApply.getMargin().multiply(fundsSetting.getDaysWarning()).add(fundsApply.getFundsAmount()).setScale(2, 4);
                /* 157 */         model.setLineWarning(lineWarning);
                /*     */       }
            /* 159 */       model.setAuditTime(DateTimeUtil.getCurrentDate());
            /* 160 */       ret = this.fundsApplyMapper.update(model);
            /* 161 */       if (ret > 0) {
                /* 162 */         BigDecimal user_enable_amt = user.getEnableAmt();
                /*     */
                /* 164 */         if (model.getStatus().intValue() == 1) {
                    /* 165 */           BigDecimal user_all_amt = user.getUserAmt();
                    /*     */
                    /* 167 */           BigDecimal reckon_all = user_all_amt.subtract(fundsApply.getManageFee());
                    /*     */
                    /* 169 */           BigDecimal tradingAmount = user.getTradingAmount().add(fundsApply.getTotalTradingAmount());
                    /* 170 */           log.info("【配資審核通過】用戶平總資金  = {} , 可用資金 = {} , 總操盤資金 = {}", new Object[] { reckon_all, user_enable_amt, tradingAmount });
                    /* 171 */           user.setUserAmt(reckon_all);
                    /* 172 */           user.setTradingAmount(tradingAmount);
                    /* 173 */           int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
                    /* 174 */           if (updateUserCount > 0) {
                        /* 175 */             log.info("【配資審核通過】修改用戶金額成功");
                        /*     */
                        /* 177 */             FundsTradingAccount fundsTradingAccount = this.fundsTradingAccountMapper.getAccountByNumber(model.getSubaccountNumber());
                        /* 178 */             if (fundsTradingAccount != null) {
                            /* 179 */               fundsTradingAccount.setStatus(Integer.valueOf(1));
                            /* 180 */               this.fundsTradingAccountMapper.update(fundsTradingAccount);
                            /*     */             }
                        /*     */
                        /* 183 */             UserCashDetail ucd = new UserCashDetail();
                        /* 184 */             ucd.setPositionId(fundsApply.getId());
                        /* 185 */             ucd.setAgentId(user.getAgentId());
                        /* 186 */             ucd.setAgentName(user.getAgentName());
                        /* 187 */             ucd.setUserId(user.getId());
                        /* 188 */             ucd.setUserName(user.getRealName());
                        /* 189 */             ucd.setDeType("配資審核通過");
                        /* 190 */             ucd.setDeAmt(fundsApply.getPayAmount());
                        /* 191 */             ucd.setDeSummary("申請按天配資:" + fundsApply.getOrderNumber() + "，配資審核通過，解凍保證金到配資帳戶：" + fundsApply.getPayAmount());
                        /* 192 */             ucd.setAddTime(new Date());
                        /* 193 */             ucd.setIsRead(Integer.valueOf(0));
                        /* 194 */             int insertSxfCount = this.userCashDetailMapper.insert(ucd);
                        /* 195 */             if (insertSxfCount > 0) {
                            /* 196 */               log.info("【配資審核通過】申請成功");
                            /*     */             }
                        /*     */           } else {
                        /* 199 */             log.error("【配資審核通過】修改用戶金額出錯");
                        /* 200 */             throw new Exception("【配資審核通過】修改用戶金額出錯");
                        /*     */           }
                    /*     */
                    /*     */         } else {
                    /*     */
                    /* 205 */           BigDecimal reckon_enable = user_enable_amt.add(fundsApply.getPayAmount());
                    /* 206 */           user.setEnableAmt(reckon_enable);
                    /* 207 */           int updateUserCount = this.userMapper.updateByPrimaryKeySelective(user);
                    /* 208 */           if (updateUserCount > 0) {
                        /* 209 */             log.info("【配資審核未通過】修改用戶金額成功");
                        /* 210 */             UserCashDetail ucd = new UserCashDetail();
                        /* 211 */             ucd.setPositionId(fundsApply.getId());
                        /* 212 */             ucd.setAgentId(user.getAgentId());
                        /* 213 */             ucd.setAgentName(user.getAgentName());
                        /* 214 */             ucd.setUserId(user.getId());
                        /* 215 */             ucd.setUserName(user.getRealName());
                        /* 216 */             ucd.setDeType("配資審核未通過");
                        /* 217 */             ucd.setDeAmt(fundsApply.getPayAmount());
                        /* 218 */             ucd.setDeSummary("申請按天配資:" + fundsApply.getOrderNumber() + "，配資審核未通過，解凍保證金到餘額：" + fundsApply.getPayAmount() + "，原因：" + model.getAuditOpinion());
                        /* 219 */             ucd.setAddTime(new Date());
                        /* 220 */             ucd.setIsRead(Integer.valueOf(0));
                        /* 221 */             int insertSxfCount = this.userCashDetailMapper.insert(ucd);
                        /* 222 */             if (insertSxfCount > 0) {
                            /* 223 */               log.info("【按天配資】申請成功");
                            /*     */             }
                        /*     */           } else {
                        /* 226 */             log.error("【按天配資】修改用戶金額出錯");
                        /* 227 */             throw new Exception("【按天配資】修改用戶金額出錯");
                        /*     */           }
                    /*     */         }
                /*     */
                /* 231 */         log.info("配資申請-審核 = {}  實際購買金額 =  {} 比較結果 = {} ", Integer.valueOf(0));
                /*     */       }
            /*     */     }
        /* 234 */     if (ret > 0) {
            /* 235 */       return ServerResponse.createBySuccessMsg("操作成功");
            /*     */     }
        /* 237 */     return ServerResponse.createByErrorMsg("操作失敗");
        /*     */   }


    /*配資申請-查詢列表*/
    @Override
    public ServerResponse<PageInfo> getList(int pageNum, int pageSize, String keyword, Integer status, HttpServletRequest request){
        PageHelper.startPage(pageNum, pageSize);
        List<FundsApply> listData = this.fundsApplyMapper.pageList(pageNum, pageSize, keyword, status);
        PageInfo pageInfo = new PageInfo(listData);
        pageInfo.setList(listData);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /*配資申請-查詢詳情*/
    @Override
    public ServerResponse getDetail(int id) {
        return ServerResponse.createBySuccess(this.fundsApplyMapper.load(id));
    }

    /**
     * 配資申請-用戶配資列表
     */
    @Override
    public ServerResponse<PageInfo> getUserApplyList(int pageNum, int pageSize, int userId, HttpServletRequest request){
        User user = this.iUserService.getCurrentRefreshUser(request);
        if (user == null){
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        PageHelper.startPage(pageNum, pageSize);
        List<FundsApply> listData = this.fundsApplyMapper.getUserApplyList(pageNum, pageSize, user.getId());
        PageInfo pageInfo = new PageInfo(listData);
        pageInfo.setList(listData);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 配資申請-用戶操盤中子帳戶
     */
    @Override
    public ServerResponse<PageInfo> getUserEnabledSubaccount(HttpServletRequest request){
        User user = this.iUserService.getCurrentRefreshUser(request);
        if (user == null){
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        List<FundsApply> listData = this.fundsApplyMapper.getUserEnabledSubaccount(user.getId());
        PageInfo pageInfo = new PageInfo();
        pageInfo.setList(listData);
        return ServerResponse.createBySuccess(pageInfo);
    }

}
