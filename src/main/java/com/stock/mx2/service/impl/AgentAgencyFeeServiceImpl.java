package com.stock.mx2.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.*;
import com.stock.mx2.pojo.*;
import com.stock.mx2.service.IAgentAgencyFeeService;
import com.stock.mx2.service.IAgentUserService;
import com.stock.mx2.service.IUserPositionService;
import com.stock.mx2.utils.PropertiesUtil;
import com.stock.mx2.utils.redis.CookieUtils;
import com.stock.mx2.utils.redis.JsonUtil;
import com.stock.mx2.utils.redis.RedisShardedPoolUtils;
import com.stock.mx2.vo.agent.AgentAgencyFeeVO;
import com.stock.mx2.vo.agent.AgentSecondInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

@Service("IAgentAgencyFeeService")
public class AgentAgencyFeeServiceImpl implements IAgentAgencyFeeService {
    @Resource
    private AgentAgencyFeeMapper agentAgencyFeeMapper;

    @Autowired
    IAgentUserService iAgentUserService;

    @Autowired
    private UserPositionItemMapper userPositionItemMapper;


    @Autowired
    IUserPositionService iUserPositionService;

    @Autowired
    AgentUserMapper agentUserMapper;

    @Autowired
    UserPositionMapper userPositionMapper;

    @Autowired
    UserMapper userMapper;

    @Override
    public int insert(AgentAgencyFee agentAgencyFee) {
        int ret = 0;
        // valid
        if (agentAgencyFee == null) {
            return 0;
        }

        ret = agentAgencyFeeMapper.insert(agentAgencyFee);
        return ret;
    }

    @Override
    public int update(AgentAgencyFee agentAgencyFee) {
        int ret = agentAgencyFeeMapper.update(agentAgencyFee);
        return ret>0 ? ret: 0;
    }

    /*獲取收費比例
    * feeType：費用類型：1、入倉手續費，2、平倉手續費，3、延遞費(留倉費)，4、分紅
    * */
    private  BigDecimal getScale(AgentUser agentUser, int feeType){
        BigDecimal scale = new BigDecimal(0);//遞延費比例
        if(feeType == 1 || feeType == 2){
            scale = agentUser.getPoundageScale();
        } else if(feeType == 3){
            scale = agentUser.getDeferredFeesScale();
        } else if(feeType == 4){
            scale = agentUser.getReceiveDividendsScale();
        }
        return scale;
    }

    /*
    * 代理費收入，考慮多級代理的問題
    * feeType：費用類型：1、入倉手續費，2、平倉手續費，3、延遞費(留倉費)，4、分紅
    * positionSn：入倉單號
    * 調用關鍵詞：核算代理收入
    * */
    @Override
    public int AgencyFeeIncome(int feeType,UserPositionItem userPosition) {
        int ret = 0;
        int businessId = 0;
        int agentId = 0;
        int level = 0;//代理級別
        BigDecimal totalMoney = new BigDecimal(0);
        BigDecimal oneProfitMoney = new BigDecimal(0);//一級利潤
        BigDecimal upProfitMoney = new BigDecimal(0);//上級利潤
        BigDecimal selfProfitMoney = new BigDecimal(0);//上級利潤
        BigDecimal downProfitMoney = new BigDecimal(0);//下級利潤
        BigDecimal scale = new BigDecimal(0);//遞延費比例
        String remarks = "";
        int downAgentId = 0;
        AgentUser agentUser = null;

        agentUser = agentUserMapper.findAgentByAgentId(userPosition.getAgentId());
        agentId = agentUser.getId();
        level = agentUser.getAgentLevel();
        businessId = userPosition.getId();
        if(feeType == 1){
            totalMoney = userPosition.getOrderFee();
            remarks = "【入倉收入】入倉手續費總額："+userPosition.getOrderFee()+"，單號："+userPosition.getPositionSn();
        } else if(feeType == 2){
            //平倉浮動總盈虧負數，代理利潤增加，否則減少
            totalMoney = userPosition.getAllProfitAndLose().multiply(new BigDecimal(-1));
            remarks = "【平倉收入】平倉手續費總額："+userPosition.getOrderFee()+"，單號："+userPosition.getPositionSn();
        }else if(feeType == 4){
            totalMoney = userPosition.getAllProfitAndLose().multiply(new BigDecimal(-1));
            remarks = "【分紅收入】分紅總額："+userPosition.getOrderFee()+"，單號："+userPosition.getPositionSn();
        }
        //金額為0不計算分紅直接跳出
        if(totalMoney.compareTo(new BigDecimal(0))<=0){
            return -1;
        }
        //模擬用戶下單不計算分紅
        User user = userMapper.selectByPrimaryKey(userPosition.getUserId());
        if(user.getAccountType() != 0 || user.getIsLock() != 0 || user.getIsActive() != 2){
            return -2;
        }
        List<AgentUser> agentlist = iAgentUserService.getAgentSuperiorList(agentUser.getId());
        if(agentlist != null && agentlist.size()>0){
            if(agentlist.size() == 1){//一級代理的會員
                //一級代理利潤
                AgentAgencyFee agentAgencyFee = new AgentAgencyFee();
                scale = getScale(agentUser,feeType);
                selfProfitMoney = totalMoney.multiply(scale).setScale(4, 4);
                agentAgencyFee.setAgentId(agentUser.getId());
                agentAgencyFee.setStatus(1);
                agentAgencyFee.setAymentType(1);
                agentAgencyFee.setBusinessId(businessId);
                agentAgencyFee.setFeeType(feeType);
                agentAgencyFee.setTotalMoney(totalMoney);
                agentAgencyFee.setProfitMoney(selfProfitMoney);
                agentAgencyFee.setRemarks(remarks);
                saveAgencyFee(agentAgencyFee);
                //總代理利潤
                agentUser = agentUserMapper.findAgentByAgentId(agentUser.getParentId());
                AgentAgencyFee totalAgent = new AgentAgencyFee();
                totalAgent.setAgentId(agentUser.getId());
                totalAgent.setStatus(1);
                totalAgent.setAymentType(1);
                totalAgent.setBusinessId(businessId);
                totalAgent.setFeeType(feeType);
                upProfitMoney = totalMoney.subtract(selfProfitMoney);
                totalAgent.setTotalMoney(totalMoney);
                totalAgent.setProfitMoney(upProfitMoney);
                totalAgent.setRemarks(remarks);
                saveAgencyFee(totalAgent);
            } else if(agentlist.size()>1) {//二級代理以上會員
                for (int i = 1; i < agentlist.size(); i++) {
                    AgentUser model = agentlist.get(i);
                    //一級代理，要把總代理的收入算出來
                    if (i == 1) {
                        //一級代理利潤
                        AgentAgencyFee agentAgencyFee = new AgentAgencyFee();
                        if (agentlist.size() == 2) {
                            AgentUser selfAgentUser = agentUserMapper.findAgentByAgentId(agentId);
                            scale = getScale(model,feeType);
                            oneProfitMoney = totalMoney.multiply(scale).setScale(4, 4);
                            scale = getScale(selfAgentUser,feeType);
                            downProfitMoney = oneProfitMoney.multiply(scale).setScale(4, 4);
                            selfProfitMoney = oneProfitMoney.subtract(downProfitMoney);

                        } else {
                            AgentUser modeldown = agentlist.get(i + 1);
                            scale = getScale(model,feeType);
                            oneProfitMoney = totalMoney.multiply(scale).setScale(4, 4);
                            scale = getScale(modeldown,feeType);
                            downProfitMoney = oneProfitMoney.multiply(scale).setScale(4, 4);
                            selfProfitMoney = oneProfitMoney.subtract(downProfitMoney);
                        }
                        agentAgencyFee.setAgentId(model.getId());
                        agentAgencyFee.setStatus(1);
                        agentAgencyFee.setAymentType(1);
                        agentAgencyFee.setBusinessId(businessId);
                        agentAgencyFee.setFeeType(feeType);
                        agentAgencyFee.setTotalMoney(totalMoney);
                        agentAgencyFee.setProfitMoney(selfProfitMoney);
                        agentAgencyFee.setRemarks(remarks);
                        //增加一級代理利潤
                        saveAgencyFee(agentAgencyFee);
                        //總代理利潤
                        agentUser = agentUserMapper.findAgentByAgentId(model.getParentId());
                        AgentAgencyFee totalAgent = new AgentAgencyFee();
                        totalAgent.setAgentId(agentUser.getId());
                        totalAgent.setStatus(1);
                        totalAgent.setAymentType(1);
                        totalAgent.setBusinessId(businessId);
                        totalAgent.setFeeType(feeType);
                        upProfitMoney = totalMoney.subtract(oneProfitMoney);
                        totalAgent.setTotalMoney(totalMoney);
                        totalAgent.setProfitMoney(upProfitMoney);
                        totalAgent.setRemarks(remarks);
                        saveAgencyFee(totalAgent);
                        upProfitMoney = downProfitMoney;
                        ret = ret +1 ;
                    } else {
                        //二級以下代理利潤
                        AgentAgencyFee agentAgencyFee = new AgentAgencyFee();
                        if (i == (level-1)) {//倒數第二級，自己就是下級
                            AgentUser selfAgentUser = agentUserMapper.findAgentByAgentId(agentId);
                            scale = getScale(selfAgentUser,feeType);
                            downProfitMoney = upProfitMoney.multiply(scale).setScale(4, 4);
                            selfProfitMoney = upProfitMoney.subtract(downProfitMoney);
                        } else {
                            AgentUser modeldown = agentlist.get(i + 1);
                            scale = getScale(modeldown,feeType);
                            downProfitMoney = upProfitMoney.multiply(scale).setScale(4, 4);
                            selfProfitMoney = upProfitMoney.subtract(downProfitMoney);
                        }
                        agentAgencyFee.setAgentId(model.getId());
                        agentAgencyFee.setStatus(1);
                        agentAgencyFee.setAymentType(1);
                        agentAgencyFee.setBusinessId(businessId);
                        agentAgencyFee.setFeeType(feeType);
                        agentAgencyFee.setTotalMoney(totalMoney);
                        agentAgencyFee.setProfitMoney(selfProfitMoney);
                        agentAgencyFee.setRemarks(remarks);
                        saveAgencyFee(agentAgencyFee);
                        upProfitMoney = downProfitMoney;
                        ret = ret +1 ;
                    }
                }
                //最後一級的利潤
                AgentAgencyFee agentAgencyFee = new AgentAgencyFee();
                agentAgencyFee.setAgentId(agentId);
                agentAgencyFee.setStatus(1);
                agentAgencyFee.setAymentType(1);
                agentAgencyFee.setBusinessId(businessId);
                agentAgencyFee.setFeeType(feeType);
                agentAgencyFee.setTotalMoney(totalMoney);
                agentAgencyFee.setProfitMoney(downProfitMoney);
                agentAgencyFee.setRemarks(remarks);
                saveAgencyFee(agentAgencyFee);
            }
        }
        return ret;
    }

    /*增加代理利潤*/
    public int saveAgencyFee(AgentAgencyFee model){
        int k = 0;
        //添加利潤明細
        k = insert(model);
        //修改代理帳戶餘額
        AgentUser user = new AgentUser();
        user.setId(model.getAgentId());
        user.setTotalMoney(model.getProfitMoney());
        k = agentUserMapper.updateTotalMoney(user);
        return k;
    }

    /*查詢代理利潤明細列表
    * */
    public ServerResponse<PageInfo> getAgentAgencyFeeList(int pageNum, int pageSize, HttpServletRequest request) {
        Page<AgentAgencyFeeVO> page = PageHelper.startPage(pageNum, pageSize);
        AgentUser agentUser = getCurrentAgent(request);
        if (agentUser ==null){
            return  ServerResponse.createByError("請先登錄",null);
        }
        List<AgentAgencyFee> agentAgencyFees = this.agentAgencyFeeMapper.getAgentAgencyFeeList(agentUser.getId());
        PageInfo pageInfo = new PageInfo(page);
        pageInfo.setList(agentAgencyFees);
        return ServerResponse.createBySuccess(pageInfo);
    }

    public AgentUser getCurrentAgent(HttpServletRequest request) {
        String loginToken = CookieUtils.readLoginToken(request, PropertiesUtil.getProperty("agent.cookie.name"));
        String agentJson = RedisShardedPoolUtils.get(loginToken);
        AgentUser agentUser = (AgentUser) JsonUtil.string2Obj(agentJson, AgentUser.class);
       if (agentUser==null){
           return null;
       }
        return this.agentUserMapper.selectByPrimaryKey(agentUser.getId());
    }
}
