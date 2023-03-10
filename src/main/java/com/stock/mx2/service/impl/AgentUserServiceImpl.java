package com.stock.mx2.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.AgentUserMapper;
import com.stock.mx2.dao.SiteTaskLogMapper;
import com.stock.mx2.pojo.*;
import com.stock.mx2.service.IAgentAgencyFeeService;
import com.stock.mx2.service.IAgentDistributionUserService;
import com.stock.mx2.service.IAgentUserService;
import com.stock.mx2.service.ISiteInfoService;
import com.stock.mx2.utils.KeyUtils;
import com.stock.mx2.utils.PropertiesUtil;
import com.stock.mx2.utils.redis.CookieUtils;
import com.stock.mx2.utils.redis.JsonUtil;
import com.stock.mx2.utils.redis.RedisShardedPoolUtils;
import com.stock.mx2.vo.agent.AgentInfoVO;
import com.stock.mx2.vo.agent.AgentSecondInfoVO;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;


@Service("iAgentUserService")
public class AgentUserServiceImpl implements IAgentUserService {
    private static final Logger log = LoggerFactory.getLogger(AgentUserServiceImpl.class);


    @Autowired
    AgentUserMapper agentUserMapper;
    @Autowired
    SiteTaskLogMapper siteTaskLogMapper;


    @Autowired
    ISiteInfoService iSiteInfoService;

    @Autowired
    IAgentDistributionUserService iAgentDistributionUserService;

    @Autowired
    IAgentAgencyFeeService iAgentAgencyFeeService;


    public AgentUser getCurrentAgent(HttpServletRequest request) {
        String loginToken = request.getHeader(PropertiesUtil.getProperty("agent.cookie.name"));
        String agentJson = RedisShardedPoolUtils.get(loginToken);
        AgentUser agentUser = (AgentUser) JsonUtil.string2Obj(agentJson, AgentUser.class);
        if (agentUser ==null){
            return null;
        }

        return this.agentUserMapper.selectByPrimaryKey(agentUser.getId());
    }


    public AgentUser findByCode(String agentCode) {
        return this.agentUserMapper.findByCode(agentCode);
    }


    public ServerResponse login(String agentPhone, String agentPwd, String verifyCode, HttpServletRequest request) {
//        if (StringUtils.isBlank(verifyCode)) {
//            return ServerResponse.createByErrorMsg("?????????????????????");
//        }
        String original = (String) request.getSession().getAttribute("KAPTCHA_SESSION_KEY");

//        if (!verifyCode.equalsIgnoreCase(original)) {
//            return ServerResponse.createByErrorMsg("???????????????");
//        }

        if (StringUtils.isBlank(agentPhone) || StringUtils.isBlank(agentPwd)) {
            return ServerResponse.createByErrorMsg("??????????????????");
        }

        AgentUser agentUser = this.agentUserMapper.login(agentPhone, agentPwd);
        if (agentUser == null) {
            return ServerResponse.createByErrorMsg("???????????????????????????");
        }

        if (agentUser.getIsLock().intValue() == 1) {
            return ServerResponse.createByErrorMsg("??????????????????????????????????????????");
        }

        return ServerResponse.createBySuccess(agentUser);
    }


    public ServerResponse getAgentInfo(HttpServletRequest request) {
        String host = "";
        ServerResponse serverResponse = this.iSiteInfoService.getInfo();
        if (serverResponse.isSuccess()) {
            SiteInfo siteInfo = (SiteInfo) serverResponse.getData();
            if (StringUtils.isBlank(siteInfo.getSiteHost())) {
                return ServerResponse.createByErrorMsg("info host?????????");
            }
            host = siteInfo.getSiteHost();
        }
        String loginToken = CookieUtils.readLoginToken(request, PropertiesUtil.getProperty("agent.cookie.name"));
        String agentJson = RedisShardedPoolUtils.get(loginToken);
        AgentUser agentUser = (AgentUser) JsonUtil.string2Obj(agentJson, AgentUser.class);
        if (agentUser ==null){
          return   ServerResponse.createByError("????????????",null);
        }
        AgentUser dbuser = this.agentUserMapper.selectByPrimaryKey(agentUser.getId());
        AgentInfoVO agentInfoVO = assembleAgentInfoVO(dbuser, host);
        return ServerResponse.createBySuccess(agentInfoVO);
    }


        public ServerResponse updatePwd(String oldPwd, String newPwd, HttpServletRequest request) {
        if (StringUtils.isBlank(oldPwd) || StringUtils.isBlank(newPwd)) {
            return ServerResponse.createByErrorMsg("??????????????????");
        }

        AgentUser agentUser = getCurrentAgent(request);
            if (agentUser ==null){
             return    ServerResponse.createByError("????????????",null);
            }
        if (!oldPwd.equals(agentUser.getAgentPwd())) {
            return ServerResponse.createByErrorMsg("????????????");
        }

        agentUser.setAgentPwd(newPwd);
        int updateCount = this.agentUserMapper.updateByPrimaryKeySelective(agentUser);

        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("????????????");
        }
        return ServerResponse.createByErrorMsg("????????????");
    }


    public ServerResponse addAgentUser(String agentName, String agentPwd, String agentRealName, String agentPhone, Integer parentId, String poundageScale,  String deferredFeesScale, String receiveDividendsScale, HttpServletRequest request) {
        if (StringUtils.isBlank(agentName) ||
                StringUtils.isBlank(agentPwd) ||
                StringUtils.isBlank(agentRealName) ||
                StringUtils.isBlank(agentPhone)) {
            return ServerResponse.createByErrorMsg("?????????????????????????????????");
        }

        AgentUser dbuser = this.agentUserMapper.findByName(agentName);
        if (dbuser != null) {
            return ServerResponse.createByErrorMsg("????????????????????????????????????");
        }

        AgentUser dbuser2 = this.agentUserMapper.findByPhone(agentPhone);
        if (dbuser2 != null) {
            return ServerResponse.createByErrorMsg("????????????????????????????????????");
        }

        AgentUser agentUser = new AgentUser();
        agentUser.setAgentName(agentName);
        agentUser.setAgentPwd(agentPwd);
        agentUser.setAgentCode(KeyUtils.getAgentUniqueKey());
        agentUser.setAgentRealName(agentRealName);
        agentUser.setAgentPhone(agentPhone);
        agentUser.setAddTime(new Date());
        agentUser.setIsLock(Integer.valueOf(0));
        agentUser.setPoundageScale(new BigDecimal(poundageScale));
        agentUser.setDeferredFeesScale(new BigDecimal(deferredFeesScale));
        agentUser.setReceiveDividendsScale(new BigDecimal(receiveDividendsScale));
        agentUser.setTotalMoney(new BigDecimal(0));
        /*AgentUser currentAgent = getCurrentAgent(request);
        agentUser.setParentId(currentAgent.getId());
        agentUser.setParentName(currentAgent.getAgentName());*/
        AgentUser parentAgent = this.agentUserMapper.selectByPrimaryKey(parentId);
        if (parentId != null && parentId>0) {
            if (parentAgent != null) {
                if(parentAgent.getAgentLevel()>=6){
                    return ServerResponse.createByErrorMsg("??????????????????????????????");
                }
                agentUser.setParentId(parentAgent.getId());
                agentUser.setParentName(parentAgent.getAgentName());
                agentUser.setAgentLevel(parentAgent.getAgentLevel()+1);
            } else {
                //???????????????0???
                agentUser.setAgentLevel(Integer.valueOf(0));
                agentUser.setParentId(Integer.valueOf(0));
            }
        } else {
            //???????????????0???
            agentUser.setAgentLevel(Integer.valueOf(0));
            agentUser.setParentId(Integer.valueOf(0));
        }

        int insertCount = 0;
        try {
            this.agentUserMapper.insert(agentUser);
            insertCount = agentUser.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (insertCount > 0) {
            if (parentAgent != null) {
                //????????????????????????
                recursiveSaveAgentDistributionUser(insertCount,parentId,parentAgent.getAgentLevel());
            }
            return ServerResponse.createBySuccessMsg("????????????????????????");
        }
        return ServerResponse.createByErrorMsg("????????????????????????");
    }

    //?????????????????????????????????
    public int recursiveSaveAgentDistributionUser(int agentId, int parentId,int level){
        int k = 0;
        int pId = parentId;
        for (int i = level; i >= 0; i--){
            AgentUser parentAgent = this.agentUserMapper.selectByPrimaryKey(pId);
            System.out.print("??????????????????level="+i+"===parentid="+parentAgent.getParentId());
            //????????????????????????
            AgentDistributionUser agentDistributionUser = new AgentDistributionUser();
            agentDistributionUser.setAgentId(agentId);
            agentDistributionUser.setParentId(parentAgent.getId());
            agentDistributionUser.setParentLevel(parentAgent.getAgentLevel());
            iAgentDistributionUserService.insert(agentDistributionUser);

            if (parentAgent.getParentId()==null){
                pId = 0;
            }else{
                pId = parentAgent.getParentId();

            }
            k++;
        }
        return k;
    }


    public ServerResponse<PageInfo> getSecondAgent(int pageNum, int pageSize, HttpServletRequest request) {
        Page<AgentSecondInfoVO> page = PageHelper.startPage(pageNum, pageSize);
        AgentUser agentUser = getCurrentAgent(request);
        if (agentUser ==null){
        return     ServerResponse.createByError("????????????",null);
        }
        //log.info("======test=======" + agentUser.getId().toString());
        List<AgentUser> agentUsers = this.agentUserMapper.getSecondAgent(agentUser.getId());
        /*
        List<AgentSecondInfoVO> agentSecondInfoVOS = Lists.newArrayList();
        for (AgentUser agentUser1 : agentUsers) {
            AgentSecondInfoVO agentSecondInfoVO = assembleAgentSecondInfoVO(agentUser1);
            agentSecondInfoVOS.add(agentSecondInfoVO);
        }*/
        PageInfo pageInfo = new PageInfo(page);
        pageInfo.setList(agentUsers);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /*????????????????????????*/
    @Override
    public  List<AgentUser> getAgentSuperiorList(int agentId){
        return  this.agentUserMapper.getAgentSuperiorList(agentId);
    }

    public ServerResponse<PageInfo> listByAdmin(String realName, String phone, int pageNum, int pageSize, int id, HttpServletRequest request) {
        Page<AgentUser> page = PageHelper.startPage(pageNum, pageSize);

        this.agentUserMapper.listByAdmin(realName, phone, id);
        PageInfo pageInfo = new PageInfo(page);

        return ServerResponse.createBySuccess(pageInfo);
    }

    public ServerResponse add(AgentUser agentUser, HttpServletRequest request) {
        if (StringUtils.isBlank(agentUser.getAgentName()) ||
                StringUtils.isBlank(agentUser.getAgentPhone()) ||
                StringUtils.isBlank(agentUser.getAgentRealName()) ||
                StringUtils.isBlank(agentUser.getAgentPwd())) {
            return ServerResponse.createByErrorMsg("??????????????????");
        }

        AgentUser pAgent = this.agentUserMapper.findByPhone(agentUser.getAgentPhone());
        if (pAgent != null) {
            return ServerResponse.createByErrorMsg("??????????????????");
        }

        AgentUser nameAgent = this.agentUserMapper.findByName(agentUser.getAgentName());
        if (nameAgent != null) {
            return ServerResponse.createByErrorMsg("??????????????????");
        }

        AgentUser dbAgent = new AgentUser();
        dbAgent.setAgentName(agentUser.getAgentName());
        dbAgent.setAgentPwd(agentUser.getAgentPwd());
        dbAgent.setAgentPhone(agentUser.getAgentPhone());
        dbAgent.setAgentRealName(agentUser.getAgentRealName());
        dbAgent.setAddTime(new Date());
        dbAgent.setIsLock(Integer.valueOf(0));
        dbAgent.setAgentCode(KeyUtils.getAgentUniqueKey());
        //AgentUser loginAgent = getCurrentAgent(request);
        if (agentUser.getParentId() != null) {
            AgentUser parentAgent = this.agentUserMapper.selectByPrimaryKey(agentUser.getParentId());
            if (parentAgent != null) {
                dbAgent.setParentId(parentAgent.getId());
                dbAgent.setParentName(parentAgent.getAgentName());
                dbAgent.setAgentLevel(parentAgent.getAgentLevel()+1);
            } else {
                //???????????????0???
                dbAgent.setAgentLevel(Integer.valueOf(0));
                dbAgent.setParentId(Integer.valueOf(0));
            }
        }

        int insertCount = this.agentUserMapper.insert(dbAgent);
        if (insertCount > 0) {
            return ServerResponse.createBySuccessMsg("??????????????????");
        }
        return ServerResponse.createByErrorMsg("??????????????????");
    }


    public ServerResponse update(AgentUser agentUser) {
        AgentUser dbAgent = new AgentUser();

        if (StringUtils.isNotBlank(agentUser.getAgentName())) {
            return ServerResponse.createByErrorMsg("?????????????????????");
        }

        dbAgent.setId(agentUser.getId());
        if (StringUtils.isNotBlank(agentUser.getAgentPwd())) {
            dbAgent.setAgentPwd(agentUser.getAgentPwd());
        }
        if (StringUtils.isNotBlank(agentUser.getAgentRealName())) {
            dbAgent.setAgentRealName(agentUser.getAgentRealName());
        }
        if (StringUtils.isNotBlank(agentUser.getSiteLever())) {
            dbAgent.setSiteLever(agentUser.getSiteLever());
        }
        if (StringUtils.isNotBlank(agentUser.getAgentPhone())) {
            AgentUser phoneAgent = this.agentUserMapper.findByPhone(agentUser.getAgentPhone());

            if (phoneAgent == null || phoneAgent.getId() == agentUser.getId()) {
                dbAgent.setAgentPhone(agentUser.getAgentPhone());
            } else {
                return ServerResponse.createByErrorMsg("????????????????????????????????????");
            }
        }
        if (agentUser.getIsLock() != null) {
            dbAgent.setIsLock(agentUser.getIsLock());
        }

        if (agentUser.getParentId() != null) {

            AgentUser parentAgent = this.agentUserMapper.selectByPrimaryKey(agentUser.getParentId());
            if (parentAgent != null) {
                dbAgent.setParentId(parentAgent.getId());
                dbAgent.setParentName(parentAgent.getAgentName());
            }
        }

        int updateCount = this.agentUserMapper.updateByPrimaryKeySelective(dbAgent);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("??????????????????");
        }
        return ServerResponse.createByErrorMsg("??????????????????");
    }

    public int CountAgentNum() {
        return this.agentUserMapper.CountAgentNum();
    }


    private AgentSecondInfoVO assembleAgentSecondInfoVO(AgentUser agentUser) {
        AgentSecondInfoVO agentSecondInfoVO = new AgentSecondInfoVO();
        agentSecondInfoVO.setId(agentUser.getId());
        agentSecondInfoVO.setAgentCode(agentUser.getAgentCode());
        agentSecondInfoVO.setAgentName(agentUser.getAgentName());
        agentSecondInfoVO.setAgentPhone(agentUser.getAgentPhone());
        agentSecondInfoVO.setAgentRealName(agentUser.getAgentRealName());
        return agentSecondInfoVO;
    }

    private AgentInfoVO assembleAgentInfoVO(AgentUser agentUser, String host) {
        AgentInfoVO agentInfoVO = new AgentInfoVO();
        agentInfoVO.setId(agentUser.getId());
        agentInfoVO.setAgentName(agentUser.getAgentName());
        agentInfoVO.setAgentRealName(agentUser.getAgentRealName());
        agentInfoVO.setAgentPhone(agentUser.getAgentPhone());
        agentInfoVO.setAgentCode(agentUser.getAgentCode());
        agentInfoVO.setAddTime(agentUser.getAddTime());
        agentInfoVO.setIsLock(agentUser.getIsLock());
        agentInfoVO.setParentId(agentUser.getParentId());
        agentInfoVO.setParentName(agentUser.getParentName());
        agentInfoVO.setTotalMoney(agentUser.getTotalMoney());

        String pcUrl = host + PropertiesUtil.getProperty("site.pc.reg.url") + agentUser.getAgentCode();
        agentInfoVO.setPcUrl(pcUrl);
        String mUrl = host + PropertiesUtil.getProperty("site.m.reg.url") + agentUser.getAgentCode();
        agentInfoVO.setMUrl(mUrl);
        return agentInfoVO;
    }

    /*??????????????????*/
    @Transactional
    public ServerResponse updateAgentAmt(Integer agentId, Integer amt, Integer direction) {
        if (agentId == null || amt == null || direction == null) {
            return ServerResponse.createByErrorMsg("??????????????????");
        }

        AgentUser agentUser = this.agentUserMapper.selectByPrimaryKey(agentId);
        if (agentUser == null) {
            return ServerResponse.createByErrorMsg("???????????????");
        }

        BigDecimal totalMoney = agentUser.getTotalMoney();//???????????????
        BigDecimal back_amt = agentUser.getTotalMoney();//???????????????
        BigDecimal deduct_amt = new BigDecimal(amt);//????????????

        if (direction.intValue() == 0) {

        } else if (direction.intValue() == 1) {
            if (totalMoney.compareTo(new BigDecimal(amt.intValue())) == -1) {
                return ServerResponse.createByErrorMsg("????????????, ???????????????");
            }
            deduct_amt = deduct_amt.multiply(new BigDecimal(-1));
            back_amt = back_amt.subtract(new BigDecimal(amt));
        } else {
            return ServerResponse.createByErrorMsg("??????????????????");
        }

        //????????????????????????
        AgentUser user = new AgentUser();
        user.setId(agentUser.getId());
        user.setTotalMoney(deduct_amt);
        agentUserMapper.updateTotalMoney(user);
        //??????????????????
        AgentAgencyFee agentAgencyFee = new AgentAgencyFee();
        agentAgencyFee.setAgentId(agentUser.getId());
        agentAgencyFee.setStatus(1);
        agentAgencyFee.setAymentType(2);
        agentAgencyFee.setBusinessId(0);
        agentAgencyFee.setFeeType(0);
        agentAgencyFee.setTotalMoney(deduct_amt);
        agentAgencyFee.setProfitMoney(new BigDecimal(amt.intValue()));
        agentAgencyFee.setRemarks("?????????????????????????????????"+amt);
        iAgentAgencyFeeService.insert(agentAgencyFee);


        SiteTaskLog siteTaskLog = new SiteTaskLog();
        siteTaskLog.setTaskType("???????????????????????????");
        StringBuffer cnt = new StringBuffer();
        cnt.append("??????????????????????????? - ")
                .append((direction.intValue() == 0) ? "??????" : "??????")
                .append(amt).append("???");
        siteTaskLog.setTaskCnt(cnt.toString());

        StringBuffer target = new StringBuffer();
        target.append("??????id : ").append(agentUser.getId())
                .append("????????? ????????? = ").append(totalMoney)
                .append("????????? ????????? = ").append(back_amt);
        siteTaskLog.setTaskTarget(target.toString());

        siteTaskLog.setIsSuccess(Integer.valueOf(0));
        siteTaskLog.setAddTime(new Date());

        int insertCount = this.siteTaskLogMapper.insert(siteTaskLog);
        if (insertCount > 0) {
            return ServerResponse.createBySuccessMsg("??????????????????");
        }
        return ServerResponse.createByErrorMsg("??????????????????");
    }

    /*????????????*/
    public ServerResponse delAgent(Integer agentId) {
        AgentUser dbAgent = this.agentUserMapper.selectByPrimaryKey(agentId);

        if (dbAgent == null) {
            return ServerResponse.createByErrorMsg("????????????????????????????????????");
        }

        int updateCount = this.agentUserMapper.deleteByPrimaryKey(agentId);



        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("??????????????????");
        }
        return ServerResponse.createByErrorMsg("??????????????????");
    }
}

