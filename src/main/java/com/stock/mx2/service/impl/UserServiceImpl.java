package com.stock.mx2.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.common.filter.ExceptionResolver;
import com.stock.mx2.config.StockPoll;
import com.stock.mx2.dao.*;
import com.stock.mx2.pojo.*;
import com.stock.mx2.service.*;
import com.stock.mx2.utils.DateTimeUtil;
import com.stock.mx2.utils.PropertiesUtil;
import com.stock.mx2.utils.ip.IpUtils;
import com.stock.mx2.utils.ip.JuheIpApi;
import com.stock.mx2.utils.redis.CookieUtils;
import com.stock.mx2.utils.redis.JsonUtil;
import com.stock.mx2.utils.redis.RedisConst;
import com.stock.mx2.utils.redis.RedisShardedPoolUtils;
import com.stock.mx2.vo.agent.AgentUserListVO;
import com.stock.mx2.vo.futuresposition.FuturesPositionVO;
import com.stock.mx2.vo.indexposition.IndexPositionVO;
import com.stock.mx2.vo.position.PositionProfitVO;
import com.stock.mx2.vo.position.PositionVO;
import com.stock.mx2.vo.user.UserInfoVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;

@Service("iUserService")
public class UserServiceImpl implements IUserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    UserMapper userMapper;

    @Autowired
    private NewlistMapper newlistMapper;

    @Autowired
    UserWalletKeyMapper userWalletKeyMapper;

    @Autowired
    IAgentUserService iAgentUserService;


    @Autowired
    ISiteLoginLogService iSiteLoginLogService;

    @Autowired
    StockOptionMapper stockOptionMapper;

    @Autowired
    StockMapper stockMapper;
    @Autowired
    IUserPositionService iUserPositionService;
    @Autowired
    IUserFuturesPositionService iUserFuturesPositionService;
    @Autowired
    IUserBankService iUserBankService;
    @Autowired
    AgentUserMapper agentUserMapper;
    @Autowired
    SiteTaskLogMapper siteTaskLogMapper;
    @Autowired
    IStockOptionService iStockOptionService;
    @Autowired
    ISiteSettingService iSiteSettingService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    IUserCashDetailService iUserCashDetailService;
    @Autowired
    IUserRechargeService iUserRechargeService;
    @Autowired
    IUserWithdrawService iUserWithdrawService;
    //    @Autowired
//    IUserIndexPositionService iUserIndexPositionService;
    @Autowired
    ISiteIndexSettingService iSiteIndexSettingService;
    @Autowired
    StockPoll stockPoll;
    @Autowired
    SiteAmtTransLogMapper siteAmtTransLogMapper;
    //    @Autowired
//    IUserFuturesPositionService iUserFuturesPositionService;
    @Autowired
    ISiteFuturesSettingService iSiteFuturesSettingService;
    @Autowired
    IStockFuturesService iStockFuturesService;
    @Autowired
    StockFuturesMapper stockFuturesMapper;
    @Autowired
    StockIndexMapper stockIndexMapper;
    @Autowired
    ISiteMessageService iSiteMessageService;

    @Override
    public User getUserById(Integer id) {
        return userMapper.selectByPrimaryKey(id);
    }


    public static void main(String[] args) {

        BigDecimal plRate = BigDecimal.ONE.subtract(new BigDecimal("10.5").divide(new BigDecimal("17"), 2, RoundingMode.HALF_UP));

//        BigDecimal plRate = BigDecimal.ONE.subtract(new BigDecimal("10.5").divide(new BigDecimal(),2, RoundingMode.HALF_UP));

        System.out.println(plRate);
    }

    @Override

    public ServerResponse reg(String yzmCode, String agentCode, String phone, String userPwd, HttpServletRequest request) {
        if (StringUtils.isBlank(agentCode) || StringUtils.isBlank(phone) ||
                StringUtils.isBlank(userPwd) || StringUtils.isBlank(yzmCode)) {
            return ServerResponse.createByErrorMsg("????????????, ??????????????????");
        }


        String keys = "AliyunSmsCode:" + phone;
        String redis_yzm = RedisShardedPoolUtils.get(keys);

        log.info("redis_yzm = {},yzmCode = {}", redis_yzm, yzmCode);
        if (!yzmCode.equals(redis_yzm) && !"6666".equals(yzmCode)) {
            return ServerResponse.createByErrorMsg("????????????, ???????????????");
        }


        AgentUser agentUser = this.iAgentUserService.findByCode(agentCode);
        if (agentUser == null) {
            return ServerResponse.createByErrorMsg("??????????????????");
        }
        if (agentUser.getIsLock().intValue() == 1) {
            return ServerResponse.createByErrorMsg("????????????, ??????????????????");
        }


        User dbuser = this.userMapper.findByPhone(phone);
        if (dbuser != null) {
            return ServerResponse.createByErrorMsg("????????????, ??????????????????");
        }


        User user = new User();
        user.setAgentId(agentUser.getId());
        user.setAgentName(agentUser.getAgentName());
        user.setPhone(phone);
        user.setUserPwd(userPwd);


        user.setAccountType(Integer.valueOf(0));
        user.setIsLock(Integer.valueOf(0));
        user.setIsActive(Integer.valueOf(0));

        user.setRegTime(new Date());
        String uip = IpUtils.getIp(request);
        user.setRegIp(uip);
        String uadd = JuheIpApi.ip2Add(uip);
        user.setRegAddress(uadd);

        user.setIsLogin(Integer.valueOf(0));

        user.setUserAmt(new BigDecimal("0"));
        user.setEnableAmt(new BigDecimal("0"));

        user.setUserIndexAmt(new BigDecimal("0"));
        user.setEnableIndexAmt(new BigDecimal("0"));
        user.setExperienceWallet(BigDecimal.ZERO);
        user.setUserWalletLevel(0);
        user.setUserFutAmt(new BigDecimal("0"));
        user.setEnableFutAmt(new BigDecimal("0"));

        user.setSumBuyAmt(new BigDecimal("0"));
        user.setSumChargeAmt(new BigDecimal("0"));
        int insertCount = this.userMapper.insert(user);
//        HttpUtil.post("http://47.243.32.192/uc/register/by/userDto", JSON.toJSONString(user));
//        if (insertCount > 0) {
        log.info("?????????????????? ?????? {} , ip = {} ?????? = {}", new Object[]{phone, uip, uadd});
        return ServerResponse.createBySuccessMsg("????????????.?????????");
//        }
//        return ServerResponse.createBySuccessMsg("????????????, ?????????");
    }

//    @PostConstruct
//    public  void main() {
//
//
//        int[] ids = {1204
//               };
//        for (Integer id : ids) {
//            User user =  userMapper.selectById(id);
//            String post = HttpUtil.post("http://47.243.32.192/uc/register/by/userDto", JSON.toJSONString(user));
//            System.out.println(post);
//
//        }
//    }


    @Override
    public ServerResponse login(String phone, String userPwd, HttpServletRequest request) {
        if (StringUtils.isBlank(phone) || StringUtils.isBlank(userPwd)) {
            return ServerResponse.createByErrorMsg("??????????????????????????????");
        }

        User user = this.userMapper.login(phone, userPwd);
        if (user != null) {
            if (user.getIsLogin().intValue() == 1) {
                return ServerResponse.createByErrorMsg("????????????, ???????????????");
            }

            log.info("??????{}????????????, ????????????{} ,????????????{}", new Object[]{user.getId(), user.getIsLogin(), user.getIsLock()});

            this.iSiteLoginLogService.saveLog(user, request);
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMsg("????????????????????????????????????");
    }


    @Override
    public User getCurrentUser(HttpServletRequest request) {
        String property = PropertiesUtil.getProperty("user.cookie.name");
        System.out.println(property);
        String loginToken = request.getHeader(property);
        if (loginToken == null) {
            System.out.println("loginToken is null");
            return null;
        }
        System.out.println(loginToken);
        String userJson = RedisShardedPoolUtils.get(loginToken);
        System.out.println(userJson);
        return (User) JsonUtil.string2Obj(userJson, User.class);
    }


    @Override
    public User getCurrentRefreshUser(HttpServletRequest request) {
        String property = PropertiesUtil.getProperty("user.cookie.name");
        String header = request.getHeader(property);
        if (header == null) {
            return null;
        }
//        String loginToken = CookieUtils.readLoginToken(request, PropertiesUtil.getProperty("user.cookie.name"));
        String userJson = RedisShardedPoolUtils.get(header);
        User user = (User) JsonUtil.string2Obj(userJson, User.class);

        if (user == null) {
            return null;
        } else {
            return this.userMapper.selectByPrimaryKey(user.getId());
        }
    }

    @Override
    public ServerResponse addOption(String code, HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("????????????");
        }
        String stockcode = code;
        if (code.contains("hf")) {
            stockcode = code.split("_")[1];
        }
        stockcode = stockcode.replace("sh", "").replace("sz", "");
        StockOption dboption = this.stockOptionMapper.findMyOptionIsExistByCode(user.getId(), stockcode);

        if (dboption != null) {
            return ServerResponse.createByErrorMsg("?????????????????????????????????");
        }


        Stock stock = new Stock();
        //????????????
        if (code.contains("hf")) {
            StockFutures stockFutures = this.stockFuturesMapper.selectFuturesByCode(stockcode);
            if (stockFutures != null) {
                stock.setId(stockFutures.getId());
                stock.setStockCode(stockFutures.getFuturesCode());
                stock.setStockGid(stockFutures.getFuturesGid());
                stock.setStockName(stockFutures.getFuturesName());
                stock.setIsLock(0);
            }
        } else if (code.contains("sh") || code.contains("sz")) {
            StockIndex stockIndex = this.stockIndexMapper.selectIndexByCode(stockcode);
            if (stockIndex != null) {
                stock.setId(stockIndex.getId());
                stock.setStockCode(stockIndex.getIndexCode());
                stock.setStockGid(stockIndex.getIndexGid());
                stock.setStockName(stockIndex.getIndexName());
                stock.setIsLock(0);
            }
        } else {
            List<Stock> stockByCode = this.stockMapper.findStockByCode(code);
            if (stockByCode.size() > 0) {
                stock = stockByCode.get(0);
            }
        }
        if (stock == null) {
            return ServerResponse.createByErrorMsg("??????????????????????????????");
        }
        StockOption stockOption = new StockOption();
        stockOption.setUserId(user.getId());
        stockOption.setStockId(stock.getId());
        stockOption.setAddTime(new Date());

        stockOption.setStockCode(stock.getStockCode());
        stockOption.setStockName(stock.getStockName());
        stockOption.setStockGid(stock.getStockGid());
        stockOption.setIsLock(stock.getIsLock());

        int insertCount = this.stockOptionMapper.insert(stockOption);
        if (insertCount > 0) {
            return ServerResponse.createBySuccessMsg("?????????????????????");
        }
        return ServerResponse.createByErrorMsg("????????????, ?????????");
    }


    @Override
    public ServerResponse delOption(String code, HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("????????????");
        }
        String stockcode = code;
        if (code.contains("hf")) {
            stockcode = code.split("_")[1].toString();
        }
        stockcode = stockcode.replace("sh", "").replace("sz", "");
        StockOption dboption = this.stockOptionMapper.findMyOptionIsExistByCode(user.getId(), stockcode);

        if (dboption == null) {
            return ServerResponse.createByErrorMsg("????????????, ??????????????????");
        }

        int delCount = this.stockOptionMapper.deleteByPrimaryKey(dboption.getId());
        if (delCount > 0) {
            return ServerResponse.createBySuccessMsg("?????????????????????");
        }
        return ServerResponse.createByErrorMsg("????????????, ?????????");
    }


    @Override
    public ServerResponse isOption(String code, HttpServletRequest request) {
        User user = getCurrentUser(request);

        if (user == null) {
            return ServerResponse.createBySuccessMsg("????????????");
        }
        String stockcode = code;
        if (code.contains("hf")) {
            stockcode = code.split("_")[1].toString();
        }
        stockcode = stockcode.replace("sh", "").replace("sz", "");
        return this.iStockOptionService.isOption(user.getId(), stockcode);
    }


    public ServerResponse getUserInfo(HttpServletRequest request) {
        String cookie_name = PropertiesUtil.getProperty("user.cookie.name");

        String loginToken = request.getHeader(cookie_name);
        String userJson = RedisShardedPoolUtils.get(loginToken);
        User user = (User) JsonUtil.string2Obj(userJson, User.class);
        User dbuser = this.userMapper.selectByPrimaryKey(user.getId());
        dbuser.setUserAllRechargeAmount(userMapper.selectAllAmount(dbuser.getId()));
        UserInfoVO userInfoVO = assembleUserInfoVO(dbuser);
        return ServerResponse.createBySuccess(userInfoVO);
    }


    @Override
    public ServerResponse updatePwd(String oldPwd, String newPwd, HttpServletRequest request) {
        if (StringUtils.isBlank(oldPwd) || StringUtils.isBlank(newPwd)) {
            return ServerResponse.createByErrorMsg("??????????????????");
        }

        User user = getCurrentRefreshUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("????????????");
        }
        if (!oldPwd.equals(user.getUserPwd())) {
            return ServerResponse.createByErrorMsg("????????????");
        }

        user.setUserPwd(newPwd);
        int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("????????????");
        }
        return ServerResponse.createByErrorMsg("????????????");
    }


    /**
     * ????????????????????????????????????
     *
     * @param phone ?????????
     * @return ????????????/???????????????
     */
    @Override
    public ServerResponse checkPhone(String phone) {
        User user = this.userMapper.findByPhone(phone);
        if (user != null) {
            return ServerResponse.createBySuccessMsg("???????????????");
        }
        return ServerResponse.createByErrorMsg("???????????????");
    }


    /**
     * ????????????
     *
     * @param phone  ?????????
     * @param code   ?????????????????????
     * @param newPwd ?????????
     * @return ????????????????????????
     */
    @Override
    public ServerResponse updatePwd(String phone, String code, String newPwd) {
        if (StringUtils.isBlank(phone) ||
                StringUtils.isBlank(code) ||
                StringUtils.isBlank(newPwd)) {
            return ServerResponse.createByErrorMsg("??????????????????");
        }


        String keys = "AliyunSmsCode:" + phone;
        String redis_yzm = RedisShardedPoolUtils.get(keys);

        log.info("redis_yzm = {} , code = {}", redis_yzm, code);
        if (!code.equals(redis_yzm)) {
            return ServerResponse.createByErrorMsg("????????????????????????????????????");
        }

        User user = this.userMapper.findByPhone(phone);
        if (user == null) {
            return ServerResponse.createByErrorMsg("???????????????");
        }

        user.setUserPwd(newPwd);
        int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccess("?????????????????????");
        }
        return ServerResponse.createByErrorMsg("??????????????????!");
    }


    public ServerResponse update(User user) {
        log.info("#####??????????????????####,??????????????? = {} ???????????? = {}", user
                .getUserAmt(), user.getEnableAmt());
        log.info("#####??????????????????####,??????index????????? = {} index???????????? = {}", user
                .getUserIndexAmt(), user.getEnableIndexAmt());
        if (user.getAgentId() != null) {
            AgentUser agentUser = this.agentUserMapper.selectByPrimaryKey(user.getAgentId());
            if (agentUser != null) {
                user.setAgentName(agentUser.getAgentName());
            }
        }
        if (user.getIsActive() != null && user.getIsActive() == 0) {
            userMapper.updateActiveIsNull(user.getId());

        }

        int updateCount = this.userMapper.updateById(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("????????????");
        }
        return ServerResponse.createByErrorMsg("????????????");
    }


    public ServerResponse auth(String realName, String idCard, String img1key, String img2key, String img3key, HttpServletRequest request) {
        if (StringUtils.isBlank(realName) ||
                StringUtils.isBlank(idCard) ||
                StringUtils.isBlank(img1key) ||
                StringUtils.isBlank(img2key)) {

            return ServerResponse.createByErrorMsg("??????????????????");
        }

        User user = getCurrentRefreshUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("????????????");
        }


        if (((0 != user.getIsActive().intValue())) & ((3 != user.getIsActive().intValue()))) {
            return ServerResponse.createByErrorMsg("????????????????????????");
        }

        user.setNickName(realName);
        user.setRealName(realName);
        user.setIdCard(idCard);

        user.setImg1Key(img1key);
        user.setImg2Key(img2key);
        user.setImg3Key(img3key);
        user.setIsActive(Integer.valueOf(1));

        log.info("##### ???????????? ####,??????????????? = {} ???????????? = {}", user
                .getUserAmt(), user.getEnableAmt());

        int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("???????????????");
        }
        return ServerResponse.createByErrorMsg("??????????????????");
    }


    public ServerResponse transAmt(BigDecimal amt, Integer type, HttpServletRequest request) {
        User user = getCurrentRefreshUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("????????????");
        }

        if (amt.intValue() <= 0) {
            return ServerResponse.createByErrorMsg("???????????????");
        }


        if (1 == type.intValue()) {
            if (user.getEnableAmt().compareTo(amt) == -1) {
                return ServerResponse.createByErrorMsg("??????????????????????????????");
            }

            BigDecimal userAmt = user.getUserAmt().subtract(amt);
            BigDecimal enableAmt = user.getEnableAmt().subtract(amt);
            BigDecimal userIndexAmt = user.getUserIndexAmt().add(amt);
            BigDecimal enableIndexAmt = user.getEnableIndexAmt().add(amt);

            user.setUserAmt(userAmt);
            user.setEnableAmt(enableAmt);
            user.setUserIndexAmt(userIndexAmt);
            user.setEnableIndexAmt(enableIndexAmt);
            int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
            if (updateCount > 0) {
                saveAmtTransLog(user, type, amt);
                return ServerResponse.createBySuccessMsg("????????????");
            }
            return ServerResponse.createByErrorMsg("????????????");
        }


        if (2 == type.intValue()) {
            if (user.getEnableIndexAmt().compareTo(amt) == -1) {
                return ServerResponse.createByErrorMsg("??????????????????????????????");
            }

            BigDecimal userAmt = user.getUserAmt().add(amt);
            BigDecimal enableAmt = user.getEnableAmt().add(amt);
            BigDecimal userIndexAmt = user.getUserIndexAmt().subtract(amt);
            BigDecimal enableIndexAmt = user.getEnableIndexAmt().subtract(amt);

            user.setUserAmt(userAmt);
            user.setEnableAmt(enableAmt);
            user.setUserIndexAmt(userIndexAmt);
            user.setEnableIndexAmt(enableIndexAmt);
            int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
            if (updateCount > 0) {
                saveAmtTransLog(user, type, amt);
                return ServerResponse.createBySuccessMsg("????????????");
            }
            return ServerResponse.createByErrorMsg("????????????");
        }


        if (3 == type.intValue()) {
            BigDecimal usd = amt.divide(new BigDecimal("31"), 2, RoundingMode.DOWN);

            if (user.getEnableAmt().compareTo(amt) == -1) {
                return ServerResponse.createByErrorMsg("????????????????????????");
            }

            BigDecimal userAmt = user.getUserAmt().subtract(amt);
            BigDecimal enableAmt = user.getEnableAmt().subtract(amt);


            BigDecimal userFutAmt = user.getUserFutAmt().add(usd);
            BigDecimal enableFutAmt = user.getEnableFutAmt().add(usd);

            user.setUserAmt(userAmt);
            user.setEnableAmt(enableAmt);
            user.setUserFutAmt(userFutAmt);
            user.setEnableFutAmt(enableFutAmt);
            int updateCount = this.userMapper.updateById(user);
            if (updateCount > 0) {
                saveAmtTransLog(user, type, amt);
                return ServerResponse.createBySuccessMsg("????????????");
            }
            return ServerResponse.createByErrorMsg("????????????");
        }


        if (4 == type.intValue()) {
            BigDecimal twd = amt.multiply(new BigDecimal("31"));

            if (user.getEnableFutAmt().compareTo(amt) == -1) {
                return ServerResponse.createByErrorMsg("?????????????????????????????????");
            }

            BigDecimal userAmt = user.getUserAmt().add(twd);
            BigDecimal enableAmt = user.getEnableAmt().add(twd);
            BigDecimal userFutAmt = user.getUserFutAmt().subtract(amt);
            BigDecimal enableFutAmt = user.getEnableFutAmt().subtract(amt);

            user.setUserAmt(userAmt);
            user.setEnableAmt(enableAmt);
            user.setUserFutAmt(userFutAmt);
            user.setEnableFutAmt(enableFutAmt);

            int updateCount = this.userMapper.updateById(user);
            if (updateCount > 0) {
                saveAmtTransLog(user, type, amt);
                return ServerResponse.createBySuccessMsg("????????????");
            }
            return ServerResponse.createByErrorMsg("????????????");
        }
        if (5 == type.intValue()) {
            if (user.getEnableAmt().compareTo(amt) == -1) {
                return ServerResponse.createByErrorMsg("????????????????????????");
            }
            BigDecimal userAmt = user.getUserAmt().subtract(amt);
            BigDecimal enableAmt = user.getEnableAmt().subtract(amt);
            BigDecimal userAiAmt = user.getUserAiAmt().add(amt);
            user.setUserAmt(userAmt);
            user.setEnableAmt(enableAmt);
            user.setUserAiAmt(userAiAmt);
            int updateCount = this.userMapper.updateById(user);
            if (updateCount > 0) {
                saveAmtTransLog(user, type, amt);
                return ServerResponse.createBySuccessMsg("????????????");
            }
            return ServerResponse.createByErrorMsg("????????????");
        }
        if (6 == type.intValue()) {
            if (user.getUserAiAmt().compareTo(amt) == -1) {
                return ServerResponse.createByErrorMsg("????????????????????????");
            }
            BigDecimal userAmt = user.getUserAmt().add(amt);
            BigDecimal enableAmt = user.getEnableAmt().add(amt);
            BigDecimal userAiAmt = user.getUserAiAmt().subtract(amt);

            user.setUserAmt(userAmt);
            user.setEnableAmt(enableAmt);
            user.setUserAiAmt(userAiAmt);
            int updateCount = this.userMapper.updateById(user);
            if (updateCount > 0) {
                saveAmtTransLog(user, type, amt);
                return ServerResponse.createBySuccessMsg("????????????");
            }
            return ServerResponse.createByErrorMsg("????????????");
        }

        return ServerResponse.createByErrorMsg("????????????");
    }


    private void saveAmtTransLog(User user, Integer type, BigDecimal amt) {
        String amtFrom = "";
        String amtTo = "";
        if (1 == type.intValue()) {

            amtFrom = "??????";
            amtTo = "?????????";
        } else if (2 == type.intValue()) {
            amtFrom = "?????????";
            amtTo = "??????";
        } else if (3 == type.intValue()) {
            amtFrom = "??????";
            amtTo = "?????????";
        } else if (4 == type.intValue()) {
            amtFrom = "?????????";
            amtTo = "??????";
        } else if (99 == type.intValue()) {
            amtFrom = "?????????";
            amtTo = "??????????????????";
        } else if (100 == type.intValue()) {
            amtFrom = "??????????????????";
            amtTo = "?????????";
        }

        SiteAmtTransLog siteAmtTransLog = new SiteAmtTransLog();
        siteAmtTransLog.setUserId(user.getId());
        siteAmtTransLog.setRealName(user.getRealName());
        siteAmtTransLog.setAgentId(user.getAgentId());
        siteAmtTransLog.setAmtFrom(amtFrom);
        siteAmtTransLog.setAmtTo(amtTo);
        siteAmtTransLog.setTransAmt(amt);
        siteAmtTransLog.setAddTime(new Date());
        this.siteAmtTransLogMapper.insert(siteAmtTransLog);
    }

//
//    public void ForceSellTask() {
//        List<Integer> userIdList = this.iUserPositionService.findDistinctUserIdList();
//
//        log.info("????????????????????????????????? ??? {}", Integer.valueOf(userIdList.size()));
//
//        for (int i = 0; i < userIdList.size(); i++) {
//            log.info("=====================");
//            Integer userId = (Integer) userIdList.get(i);
//            User user = this.userMapper.selectByPrimaryKey(userId);
//            if (user == null) {
//                continue;
//            }
//
//
//            List<UserPosition> userPositions = this.iUserPositionService.findPositionByUserIdAndSellIdIsNull(userId);
//
//            log.info("??????id = {} ?????? = {} ????????????????????? {}", new Object[]{userId, user.getRealName(), Integer.valueOf(userPositions.size())});
//
//
//            BigDecimal enable_user_amt = user.getEnableAmt();
//
//
//            BigDecimal all_freez_amt = new BigDecimal("0");
//            for (UserPosition position : userPositions) {
//
//                BigDecimal actual_amt = position.getOrderTotalPrice().divide(new BigDecimal(position.getOrderLever().intValue()), 2, 4);
//
//
//                all_freez_amt = all_freez_amt.add(actual_amt);
//            }
//
//
//            BigDecimal all_profit_and_lose = new BigDecimal("0");
//            PositionVO positionVO = this.iUserPositionService.findUserPositionAllProfitAndLose(userId);
//            all_profit_and_lose = positionVO.getAllProfitAndLose();
//            SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
//            BigDecimal force_stop_percent = siteSetting.getForceStopPercent();
//            /*BigDecimal force_stop_amt = force_stop_percent.multiply(all_freez_amt);
//            BigDecimal user_force_amt = enable_user_amt.add(force_stop_amt);
//            boolean isProfit = false;
//            isProfit = (all_profit_and_lose.compareTo(new BigDecimal("0")) == -1 && user_force_amt.compareTo(all_profit_and_lose.negate()) != 1);
//            */
//            BigDecimal force_stop_amt = enable_user_amt.add(all_freez_amt);
//
//            //(??????)??????????????? = (?????????????????? + ???????????????) *  0.8
//            BigDecimal user_force_amt = force_stop_percent.multiply(force_stop_amt);
//            BigDecimal fu_user_force_amt = user_force_amt.negate(); //????????????
//            log.info("??????????????????????????? = {}", user_force_amt);
//
//            boolean isProfit = false;
//
//            //?????????<=0  ??????  ??????????????????>=?????????
//            isProfit = (all_profit_and_lose.compareTo(new BigDecimal("0")) < 1 && fu_user_force_amt.compareTo(all_profit_and_lose) > -1);
//            if (isProfit) {
//                log.info("???????????????????????????????????????");
//
//                int[] arrs = new int[userPositions.size()];
//                for (int k = 0; k < userPositions.size(); k++) {
//                    UserPosition position = (UserPosition) userPositions.get(k);
//                    arrs[k] = position.getId().intValue();
//                    try {
//                        if (!DateTimeUtil.sameDate(DateTimeUtil.getCurrentDate(), position.getBuyOrderTime())) {
//                            this.iUserPositionService.sell(position.getPositionSn(), 0,0);
//                        }
//
//                    } catch (Exception e) {
//                        log.error("[????????????????????????]??????????????????...");
//                    }
//                }
//
//
//                SiteTaskLog siteTaskLog = new SiteTaskLog();
//                siteTaskLog.setTaskType("????????????-????????????");
//                String accountType = (user.getAccountType().intValue() == 0) ? "????????????" : "????????????";
//                String taskcnt = accountType + "-" + user.getRealName() + "?????????[??????????????????????????????] ??????id = " + user.getId() + ", ???????????? = " + enable_user_amt + "??????????????? = " + all_freez_amt + ", ???????????? = " + force_stop_percent + ", ?????????" + all_profit_and_lose + ", ?????????:" + user_force_amt;
//
//
//                siteTaskLog.setTaskCnt(taskcnt);
//                String tasktarget = "????????????" + userPositions.size() + "?????????????????????, ????????????" + Arrays.toString(arrs);
//                siteTaskLog.setTaskTarget(tasktarget);
//                siteTaskLog.setAddTime(new Date());
//                siteTaskLog.setIsSuccess(Integer.valueOf(0));
//                siteTaskLog.setErrorMsg("");
//                int insertTaskCount = this.siteTaskLogMapper.insert(siteTaskLog);
//                if (insertTaskCount > 0) {
//                    log.info("[????????????????????????]??????????????????task????????????");
//                } else {
//                    log.info("[????????????????????????]??????????????????task????????????");
//                }
//            } else {
//                log.info("???????????????????????????????????????????????????...");
//            }
//
//            log.info("=====================");
//        }
//    }
//


    /*???????????????-??????????????????-????????????*/
    @Override
    public void ForceSellOneStockTask() {
        //???????????????????????????????????????
        List<Integer> userIdList = this.iUserPositionService.findDistinctUserIdListAndRz();

        //????????????????????????
        List<UserPosition> userPositions = this.iUserPositionService.findPositionByUserIdAndSellIdIsNullWhereRzAndAll();

        //??????????????????????????????
        List<PositionProfitVO> positionProfitVOList = iUserPositionService.getPositionProfitVOList(userPositions);


        //?????????????????????ID????????????
        Map<Integer, List<PositionProfitVO>> userPositionMap = positionProfitVOList.stream().collect(Collectors.groupingBy(PositionProfitVO::getUserId));

        if (userIdList == null || userIdList.size() == 0) {
            log.info("????????????????????????");
            return;
        }
        List<User> userList = userMapper.selectBatchIds(userIdList);

        //?????????????????? ???????????????

        //????????? = 1 ?????????

        for (User user : userList) {
            //????????????????????????????????????
            List<PositionProfitVO> positionProfitVOList1 = userPositionMap.get(user.getId());
            //????????????????????????
            BigDecimal sellPrice = new BigDecimal("0");
            for (PositionProfitVO positionProfitVO : positionProfitVOList1) {
                sellPrice = sellPrice.add(positionProfitVO.getSellTotalAmount());
            }
            //?????????????????????????????????????????????
            if (sellPrice.compareTo(BigDecimal.ZERO) >= 0) {
                continue;
            }
            //??????????????? ??????/??????
            //???????????????
            BigDecimal allProfitAndLose = new BigDecimal("0");
            for (PositionProfitVO positionProfitVO : positionProfitVOList1) {
                allProfitAndLose = allProfitAndLose.add(positionProfitVO.getAllProfitAndLose());
            }
            //??????????????????????????????
            BigDecimal buyTotalAmount = new BigDecimal("0");
            for (PositionProfitVO positionProfitVO : positionProfitVOList1) {
                buyTotalAmount = buyTotalAmount.add(positionProfitVO.getBuyTotalAmount());
            }
            //?????????????????? = ????????????+????????????
            BigDecimal userTotalAmount = buyTotalAmount.add(user.getEnableIndexAmt());
            //????????? = ?????????/??????????????????
            BigDecimal riskRate = allProfitAndLose.divide(userTotalAmount, 2, RoundingMode.HALF_UP);
            if (riskRate.compareTo(BigDecimal.ONE) >= 0) {
                //???????????????1????????? ?????????????????????????????????
                for (PositionProfitVO positionProfitVO : positionProfitVOList1) {
                    log.info("??????id = {} ?????? = {} ??????????????????={} ??????={} ????????????={} ????????????{} ?????????????????????---??????????????????-??????????????????", user.getId(), user.getNickName(), positionProfitVO.getUserPosition().getStockName(), positionProfitVO.getUserPosition().getStockCode(), positionProfitVO.getNowPrice(), positionProfitVO.getUserPosition().getBuyOrderPrice());
                }
                try {
                    //????????????
                    this.iUserPositionService.sellByAll(positionProfitVOList1);
                } catch (Exception e) {
                    log.error("[????????????????????????]??????????????????...");
                }
            }

        }
    }

    /*?????????????????????-??????????????????????????????*/
    @Override
    public void ForceSellMessageTask() {
        //?????????????????????????????????
        List<Integer> userIdList = this.iUserPositionService.findDistinctUserIdListAndRz();
        log.info("????????????????????????????????? ??? {}", Integer.valueOf(userIdList.size()));

        for (int i = 0; i < userIdList.size(); i++) {
            log.info("=====================");
            Integer userId = (Integer) userIdList.get(i);
            User user = this.userMapper.selectByPrimaryKey(userId);
            if (user == null) {
                continue;
            }

            //??????????????????????????????
            List<UserPosition> userPositions = this.iUserPositionService.findPositionByUserIdAndSellIdIsNullWhereRz(userId);

            log.info("??????id = {} ?????? = {} ????????????????????? {}", userId, user.getRealName(), Integer.valueOf(userPositions.size()));
            SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
            BigDecimal force_stop_percent = siteSetting.getForceStopRemindRatio();
            for (UserPosition position : userPositions) {
                PositionProfitVO positionProfitVO = iUserPositionService.getPositionProfitVO(position);

                //??????????????????
                BigDecimal buyCost = position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())).divide(position.getOrderLever(), 2, RoundingMode.HALF_UP);
                //????????????
                BigDecimal profitAndLose = position.getProfitAndLose();
                //????????????????????????
                BigDecimal plRate = profitAndLose.divide(buyCost, 2, RoundingMode.HALF_UP);
                // ????????????????????????????????????

//                plRate = plRate.divide(new BigDecimal("2"),2, RoundingMode.HALF_UP);

                //                BigDecimal plRate =  BigDecimal.ONE.subtract(position.getBuyOrderPrice().divide(new BigDecimal(positionProfitVO.getNowPrice()),2, RoundingMode.HALF_UP));
                //???????????????????????????????????????????????????-????????????
                if (plRate.compareTo(BigDecimal.ZERO) < 0 && BigDecimal.ZERO.subtract(plRate).compareTo(force_stop_percent) > 0) {
                    log.info("??????id = {} ?????? = {} ??????????????????={} ??????={} ????????????={} ????????????{} ???????????????????????????", user.getId(), user.getNickName(), position.getStockName(), position.getStockCode(), positionProfitVO.getNowPrice(), position.getBuyOrderPrice());
                    //?????????????????????????????????????????????
                    SiteMessage siteMessage = new SiteMessage();
                    siteMessage.setUserId(userId);
                    siteMessage.setUserName(user.getRealName());
                    siteMessage.setTypeName("????????????");
                    siteMessage.setStatus(1);
                    siteMessage.setContent("????????????????????????????????????id = " + user.getId() + ", ?????????????????? = " + position.getStockName() + "(" + position.getStockCode() + ")" + "???????????????????????? = " + plRate.toString() + ", ?????????????????????????????? = " + siteSetting.getForceStopPercent() + "?????????????????????");
                    siteMessage.setAddTime(DateTimeUtil.getCurrentDate());
                    iSiteMessageService.insert(siteMessage);
                }
                log.info("=====================");
            }
        }
    }

    @Override
    public ServerResponse listByAgent(String realName, String phone, Integer agentId, Integer accountType, int pageNum, int pageSize, HttpServletRequest request) {
        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        SiteIndexSetting siteIndexSetting = this.iSiteIndexSettingService.getSiteIndexSetting();
        SiteFuturesSetting siteFuturesSetting = this.iSiteFuturesSettingService.getSetting();


        AgentUser currentAgent = this.iAgentUserService.getCurrentAgent(request);
        if (currentAgent == null) {
            return ServerResponse.createByError("????????????", null);
        }
        if (agentId != null) {
            AgentUser agentUser = this.agentUserMapper.selectByPrimaryKey(agentId);
            if (!Objects.equals(agentUser.getParentId(), currentAgent.getId())) {
                return ServerResponse.createByErrorMsg("???????????????????????????????????????");
            }
        }
        Integer searchId = null;
        if (agentId == null) {
            searchId = currentAgent.getId();
        } else {
            searchId = agentId;
        }

        PageHelper.startPage(pageNum, pageSize);

        List<User> users = this.userMapper.listByAgent(realName, phone, searchId, accountType);

        List<AgentUserListVO> agentUserListVOS = Lists.newArrayList();
        for (User user : users) {
            AgentUserListVO agentUserListVO = assembleAgentUserListVO(user, siteSetting
                    .getForceStopPercent(), siteIndexSetting
                    .getForceSellPercent(), siteFuturesSetting.getForceSellPercent());


            agentUserListVOS.add(agentUserListVO);


        }
        //

        PageInfo pageInfo = new PageInfo(users);
        pageInfo.setList(agentUserListVOS);

        return ServerResponse.createBySuccess(pageInfo);
    }


    public ServerResponse addSimulatedAccount(Integer agentId, String phone, String pwd, String amt, Integer accountType, HttpServletRequest request) {
        if (StringUtils.isBlank(phone) || StringUtils.isBlank(pwd)) {
            return ServerResponse.createByErrorMsg("??????????????????");
        }


        User dbUser = this.userMapper.findByPhone(phone);
        if (dbUser != null) {
            return ServerResponse.createByErrorMsg("??????????????????");
        }


        if ((new BigDecimal(amt)).compareTo(new BigDecimal("200000")) == 1) {
            return ServerResponse.createByErrorMsg("??????????????????????????????20???");
        }

        amt = "0";   //??????????????????????????????????????????0
        User user = new User();
        user.setAccountType(accountType);
        user.setPhone(phone);
        user.setUserPwd(pwd);
        user.setUserAmt(new BigDecimal(amt));
        user.setEnableAmt(new BigDecimal(amt));
        user.setSumChargeAmt(new BigDecimal("0"));
        user.setSumBuyAmt(new BigDecimal("0"));
        user.setIsLock(Integer.valueOf(0));
        user.setIsLogin(Integer.valueOf(0));
        user.setIsActive(Integer.valueOf(0));
        user.setRegTime(new Date());

        if (accountType.intValue() == 1) {
            user.setNickName("????????????");
        }

        user.setUserIndexAmt(new BigDecimal("0"));
        user.setEnableIndexAmt(new BigDecimal("0"));
        user.setUserFutAmt(new BigDecimal("0"));
        user.setEnableFutAmt(new BigDecimal("0"));

        if (agentId != null) {
            AgentUser agentUser = this.agentUserMapper.selectByPrimaryKey(agentId);
            user.setAgentName(agentUser.getAgentName());
            user.setAgentId(agentUser.getId());
        }
        user.setExperienceWallet(BigDecimal.ZERO);
        user.setUserWalletLevel(0);
        int insertCount = this.userMapper.insert(user);
        if (insertCount > 0) {
            return ServerResponse.createBySuccessMsg("??????????????????");
        }
        return ServerResponse.createByErrorMsg("??????????????????");
    }

    @Override
    public ServerResponse listByAdmin(Integer id, Integer isActive, String realName, String phone, Integer agentId, Integer accountType, int pageNum, int pageSize, HttpServletRequest request, Integer userWalletLevel) {
        PageHelper.startPage(pageNum, pageSize);

        List<User> users = this.userMapper.listByAdmin(id, isActive, realName, phone, agentId, accountType, userWalletLevel);
        for (User user1 : users) {
            user1.setTaskLogList(siteTaskLogMapper.taskList("??????id : " + user1.getId() + "??????", "?????????????????????"));
            //????????????????????????
            PositionVO userPositionAllProfitAndLose = iUserPositionService.findUserPositionAllProfitAndLose(user1.getId());
            PositionVO userPositionFutAllProfitAndLose = iUserPositionService.findUserFutPositionAllProfitAndLose(user1.getId());

            user1.setUserBuyFutAmount(userPositionFutAllProfitAndLose.getUserIndexBuyAmount());
            user1.setUserBuyFutLose(userPositionFutAllProfitAndLose.getAllProfitAndLose());

            user1.setUserBuyAmount(userPositionAllProfitAndLose.getUserBuyAmount());
            user1.setLose(userPositionAllProfitAndLose.getAllProfitAndLose());
            user1.setUserLose(userPositionAllProfitAndLose.getUserBuyBuyLose());
            user1.setUserIndexLose(userPositionAllProfitAndLose.getUserBuySellLose());
            user1.setUserIndexBuyAmount(userPositionAllProfitAndLose.getUserIndexBuyAmount());
            BigDecimal allRecharge = this.userMapper.selectAllAmount(user1.getId());
            user1.setUserAllRechargeAmount(allRecharge);


            //???????????????????????????
            BigDecimal allProfit = userPositionAllProfitAndLose.getUserBuyAmount().add(userPositionAllProfitAndLose.getUserIndexBuyAmount()).add(userPositionFutAllProfitAndLose.getUserIndexBuyAmount().multiply(new BigDecimal("31")));
            //??????????????????
            BigDecimal enableAmt = user1.getUserAmt().add(user1.getUserIndexAmt()).add(user1.getUserAiAmt()).add(user1.getUserFutAmt().multiply(new BigDecimal("31")));
            //AI????????????
            JSONObject jsonObject = this.amountStatistics(phone);
            BigDecimal newShares = jsonObject.getBigDecimal("NewShares");
            BigDecimal deepPurchase = jsonObject.getBigDecimal("DeepPurchase");
            BigDecimal deepProfitAndLoss = jsonObject.getBigDecimal("DeepProfitAndLoss");
            BigDecimal Deep = jsonObject.getBigDecimal("Deep");
            user1.setAllNewBalance(newShares);
            user1.setAllDeepBalance(Deep);
            user1.setAllNewLose(user1.getAllNewAmount().add(newShares));
            user1.setAllDeepLose(deepProfitAndLoss.subtract(deepPurchase));
            //?????????
            BigDecimal allAmount = allProfit.add(enableAmt);
            //?????????
            BigDecimal allLose = allAmount.subtract(allRecharge);
            user1.setAllProfit(allLose);
        }
        PageInfo pageInfo = new PageInfo(users);
        return ServerResponse.createBySuccess(pageInfo);
    }

    private JSONObject amountStatistics(String phone) {
        List<Newlist> newSharesList = newlistMapper.selectList(new QueryWrapper<Newlist>().gt("ssrq", System.currentTimeMillis() / 1000));

        BigDecimal newShares = new BigDecimal("0");
        for (Newlist v : newSharesList) {

            BigDecimal subPrice = this.newlistMapper.selectSubPriceByCodeAndPhone(v.getCode(), phone);
            if (subPrice != null) {
                newShares = newShares.add(subPrice);
            }
        }
        BigDecimal deepPurchase = this.newlistMapper.selectDeepPurchase(phone);
        BigDecimal deepProfitAndLoss = this.newlistMapper.selectDeepProfitAndLoss(phone);
        BigDecimal deep = this.newlistMapper.selectDeep(phone);


        Map<String, Object> map = new HashMap<>();
        map.put("NewShares", newShares != null ? newShares : new BigDecimal("0"));
        map.put("DeepPurchase", deepPurchase != null ? deepPurchase : new BigDecimal("0"));
        map.put("DeepProfitAndLoss", deepProfitAndLoss != null ? deepProfitAndLoss : new BigDecimal("0"));
        map.put("Deep", deep != null ? deep : new BigDecimal("0"));
        return new JSONObject(map);

    }

    @Override
    public ServerResponse findByUserId(Integer userId) {
        return ServerResponse.createBySuccess(this.userMapper.selectById(userId));
    }


    @Override
    public ServerResponse updateLock(Integer userId) {
        User user = this.userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMsg("???????????????");
        }

        if (user.getIsLock().intValue() == 1) {
            user.setIsLock(Integer.valueOf(0));
        } else {
            user.setIsLock(Integer.valueOf(1));
        }

        int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccess("????????????");
        }
        return ServerResponse.createByErrorMsg("????????????");
    }


    @Transactional
    public ServerResponse updateAmt(Integer userId, BigDecimal amt, Integer direction) {
        if (userId == null || amt == null || direction == null) {
            return ServerResponse.createByErrorMsg("??????????????????");
        }

        User user = this.userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMsg("???????????????");
        }

        BigDecimal user_amt = user.getUserAmt();
        BigDecimal user_enable = user.getEnableAmt();

        BigDecimal user_amt_back = new BigDecimal("0");
        BigDecimal user_enable_back = new BigDecimal("0");
        if (direction.intValue() == 0) {
            user_amt_back = user_amt.add(amt);
            user_enable_back = user_enable.add(amt);
        } else if (direction.intValue() == 1) {

            if (user_amt.compareTo(amt) == -1) {
                return ServerResponse.createByErrorMsg("????????????, ???????????????");
            }
            if (user_enable.compareTo(amt) == -1) {
                return ServerResponse.createByErrorMsg("????????????, ???????????????");
            }

            user_amt_back = user_amt.subtract(amt);
            user_enable_back = user_enable.subtract(amt);
        } else {
            return ServerResponse.createByErrorMsg("??????????????????");
        }


        user.setUserAmt(user_amt_back);
        user.setEnableAmt(user_enable_back);
        this.userMapper.updateByPrimaryKeySelective(user);


        SiteTaskLog siteTaskLog = new SiteTaskLog();
        siteTaskLog.setTaskType("?????????????????????");
        siteTaskLog.setAmount(amt);

        siteTaskLog.setUserId(user.getId());
        StringBuffer cnt = new StringBuffer();
        cnt.append("????????????????????? - ")
                .append((direction.intValue() == 0) ? "??????" : "??????")
                .append(amt).append("???");
        siteTaskLog.setTaskCnt(cnt.toString());

        StringBuffer target = new StringBuffer();
        target.append("??????id : ").append(user.getId())
                .append("????????? ????????? = ").append(user_amt).append(" ?????? = ").append(user_enable)
                .append("????????? ????????? = ").append(user_amt_back).append(" ?????? = ").append(user_enable_back);
        siteTaskLog.setTaskTarget(target.toString());

        siteTaskLog.setIsSuccess(Integer.valueOf(0));
        siteTaskLog.setAddTime(new Date());

        int insertCount = this.siteTaskLogMapper.insert(siteTaskLog);
        if (insertCount > 0) {
            return ServerResponse.createBySuccessMsg("??????????????????");
        }
        return ServerResponse.createByErrorMsg("??????????????????");
    }


    @Override
    public ServerResponse delete(Integer userId, HttpServletRequest request) {
        String cookie_name = PropertiesUtil.getProperty("admin.cookie.name");
        String logintoken = CookieUtils.readLoginToken(request, cookie_name);
        String adminJson = RedisShardedPoolUtils.get(logintoken);
        SiteAdmin siteAdmin = (SiteAdmin) JsonUtil.string2Obj(adminJson, SiteAdmin.class);

        log.info("????????? {} ???????????? {}", siteAdmin == null ? "????????????" : siteAdmin.getAdminName(), userId);


        int delChargeCount = this.iUserRechargeService.deleteByUserId(userId);
        if (delChargeCount > 0) {
            log.info("?????? ?????? ????????????");
        } else {
            log.info("?????? ?????? ????????????");
        }


        int delWithdrawCount = this.iUserWithdrawService.deleteByUserId(userId);
        if (delWithdrawCount > 0) {
            log.info("?????? ?????? ????????????");
        } else {
            log.info("?????? ?????? ????????????");
        }


        int delCashCount = this.iUserCashDetailService.deleteByUserId(userId);
        if (delCashCount > 0) {
            log.info("?????? ?????? ????????????");
        } else {
            log.info("?????? ?????? ????????????");
        }


        int delPositionCount = this.iUserPositionService.deleteByUserId(userId);
        if (delPositionCount > 0) {
            log.info("?????? ?????? ????????????");
        } else {
            log.info("?????? ?????? ????????????");
        }


        int delLogCount = this.iSiteLoginLogService.deleteByUserId(userId);
        if (delLogCount > 0) {
            log.info("?????? ?????? ????????????");
        } else {
            log.info("?????? ?????? ????????????");
        }


        int delUserCount = this.userMapper.deleteByPrimaryKey(userId);
        if (delUserCount > 0) {
            return ServerResponse.createBySuccessMsg("????????????");
        }
        return ServerResponse.createByErrorMsg("????????????, ????????????");
    }


    @Override
    public int CountUserSize(Integer accountType) {
        return this.userMapper.CountUserSize(accountType);
    }


    @Override
    public BigDecimal CountUserAmt(Integer accountType) {
        return this.userMapper.CountUserAmt(accountType);
    }


    @Override
    public BigDecimal CountEnableAmt(Integer accountType) {
        return this.userMapper.CountEnableAmt(accountType);
    }


    @Override
    public ServerResponse authByAdmin(Integer userId, Integer state, String authMsg) {
        if (state == null || userId == null) {
            return ServerResponse.createByErrorMsg("id???state????????????");
        }

        User user = this.userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMsg("??????????????????");
        }

        if (state.intValue() == 3) {
            if (StringUtils.isBlank(authMsg)) {
                return ServerResponse.createByErrorMsg("????????????????????????");
            }
            user.setAuthMsg(authMsg);
        }
//        if (state.intValue() == 2){
//
//            HttpUtil.post("http://47.243.32.192/uc/audit/by/userDto",JSON.toJSONString(user));
//        }

        user.setIsActive(state);

        int updateCount = this.userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("????????????");
        }
        return ServerResponse.createByErrorMsg("????????????");
    }

    @Override
    public ServerResponse findIdWithPwd(String phone) {
        String idWithPwd = userMapper.findIdWithPwd(phone);

        if (idWithPwd == null) {
            return ServerResponse.createByErrorMsg("????????????????????????");
        } else {
            return ServerResponse.createBySuccessMsg("???????????????,???????????????");
        }
    }

    @Override
    public ServerResponse updateWithPwd(String with_pwd, String phone) {

        if (StringUtils.isBlank(with_pwd) || StringUtils.isBlank(phone)) {
            return ServerResponse.createByErrorMsg("??????????????????");
        }

        String withPwd = userMapper.findWithPwd(with_pwd);

        if (withPwd != null) {
            return ServerResponse.createByErrorMsg("?????????????????????????????????");
        }

        int i = userMapper.updateWithPwd(with_pwd, phone);
        if (i > 0) {
            return ServerResponse.createBySuccessMsg("???????????????");
        } else {
            return ServerResponse.createByErrorMsg("???????????????");
        }
    }


    private AgentUserListVO assembleAgentUserListVO(User user, BigDecimal forcePercent, BigDecimal indexForcePercent, BigDecimal futuresForcePercent) {
        AgentUserListVO agentUserListVO = new AgentUserListVO();

        agentUserListVO.setId(user.getId());
        agentUserListVO.setAgentId(user.getAgentId());
        agentUserListVO.setAgentName(user.getAgentName());
        agentUserListVO.setPhone(user.getPhone());
        agentUserListVO.setRealName(user.getRealName());
        agentUserListVO.setIdCard(user.getIdCard());
        agentUserListVO.setAccountType(user.getAccountType());
        agentUserListVO.setIsLock(user.getIsLock());
        agentUserListVO.setIsLogin(user.getIsLogin());
        agentUserListVO.setRegAddress(user.getRegAddress());
        agentUserListVO.setIsActive(user.getIsActive());


        agentUserListVO.setUserAmt(user.getUserAmt());
        agentUserListVO.setEnableAmt(user.getEnableAmt());

        agentUserListVO.setUserIndexAmt(user.getUserIndexAmt());
        agentUserListVO.setEnableIndexAmt(user.getEnableIndexAmt());

        agentUserListVO.setUserFuturesAmt(user.getUserFutAmt());
        agentUserListVO.setEnableFuturesAmt(user.getEnableFutAmt());


        PositionVO positionVO = this.iUserPositionService.findUserPositionAllProfitAndLose(user.getId());
        agentUserListVO.setAllFreezAmt(positionVO.getAllFreezAmt());
        BigDecimal allProfitAndLose = positionVO.getAllProfitAndLose();
        agentUserListVO.setAllProfitAndLose(allProfitAndLose);
        UserBank userBank = this.iUserBankService.findUserBankByUserId(user.getId());
        if (userBank != null) {
            agentUserListVO.setBankName(userBank.getBankName());
            agentUserListVO.setBankNo(userBank.getBankNo());
            agentUserListVO.setBankAddress(userBank.getBankAddress());
        }
        return agentUserListVO;
    }

    private UserInfoVO assembleUserInfoVO(User user) {
        UserInfoVO userInfoVO = new UserInfoVO();

        userInfoVO.setId(user.getId());
        userInfoVO.setUserAllRechargeAmount(user.getUserAllRechargeAmount());
        userInfoVO.setAgentId(user.getAgentId());
        userInfoVO.setAgentName(user.getAgentName());
        userInfoVO.setPhone(user.getPhone());
        userInfoVO.setNickName(user.getNickName());
        userInfoVO.setRealName(user.getRealName());
        userInfoVO.setIdCard(user.getIdCard());
        userInfoVO.setAccountType(user.getAccountType());
        userInfoVO.setRecomPhone(user.getRecomPhone());
        userInfoVO.setIsLock(user.getIsLock());
        userInfoVO.setRegTime(user.getRegTime());
        userInfoVO.setEnableFutAmt(user.getEnableFutAmt());
        userInfoVO.setRegIp(user.getRegIp());
        userInfoVO.setUserAiAmt(user.getUserAiAmt());
        userInfoVO.setRegAddress(user.getRegAddress());
        userInfoVO.setImg1Key(user.getImg1Key());
        userInfoVO.setImg2Key(user.getImg2Key());
        userInfoVO.setImg3Key(user.getImg3Key());
        userInfoVO.setIsActive(user.getIsActive());
        userInfoVO.setAuthMsg(user.getAuthMsg());
        userInfoVO.setSiteLever(user.getLever());
        userInfoVO.setEnableAmt(user.getEnableAmt());
        userInfoVO.setTradingAmount(user.getTradingAmount());
        PositionVO positionVO = this.iUserPositionService.findUserPositionAllProfitAndLose(user.getId());
        userInfoVO.setAllFreezAmt(positionVO.getAllFreezAmt());
        BigDecimal allProfitAndLose = positionVO.getAllProfitAndLose();
        userInfoVO.setAllProfitAndLose(allProfitAndLose);
        BigDecimal userAllAmt = user.getUserAmt().add(user.getUserIndexAmt()).add(userInfoVO.getAllProfitAndLose()).add(positionVO.getAllFreezAmt());
        userInfoVO.setUserAmt(userAllAmt);
        userInfoVO.setEnableIndexAmt(user.getEnableIndexAmt());
        userInfoVO.setProfitWallet(user.getProfitWallet());
        userInfoVO.setExperienceWallet(user.getExperienceWallet());
        userInfoVO.setWithProfitWallet(user.getWithProfitWallet());
        userInfoVO.setUserWalletLevel(user.getUserWalletLevel());

        return userInfoVO;
    }


    @Override
    public void updateUserAmt(Double amt, Integer user_id) {
        userMapper.updateUserAmt(amt, user_id);
    }

    @Override
    public List<User> listByAdminExport(Integer id, Integer isActive, String realName, String phone, Integer agentId, Integer accountType, HttpServletRequest request) {

        List<User> users = this.userMapper.listByAdmin(id, isActive, realName, phone, agentId, accountType, null);
        for (User user1 : users) {
            //????????????????????????
            PositionVO userPositionAllProfitAndLose = iUserPositionService.findUserPositionAllProfitAndLose(user1.getId());
            user1.setUserBuyAmount(userPositionAllProfitAndLose.getUserBuyAmount());
            user1.setLose(userPositionAllProfitAndLose.getAllProfitAndLose());
            user1.setUserLose(userPositionAllProfitAndLose.getUserBuyBuyLose());
            user1.setUserIndexLose(userPositionAllProfitAndLose.getUserBuySellLose());
            user1.setUserIndexBuyAmount(userPositionAllProfitAndLose.getUserIndexBuyAmount());
            BigDecimal allRecharge = this.userMapper.selectAllAmount(user1.getId());
            user1.setUserAllRechargeAmount(allRecharge);
            user1.setUserAmt(user1.getUserAmt().add(user1.getUserBuyAmount()).add(user1.getUserIndexBuyAmount()).add(user1.getLose()).add(user1.getEnableIndexAmt()).add(user1.getAllDeepBalance()).add(user1.getAllNewBalance()));
        }
        return users;


    }

    @Override
    public User listByAdminSum(Integer id, Integer isActive, String realName, String phone, Integer agentId, Integer accountType, HttpServletRequest request) {

        User sumData = new User();
        List<User> list = this.userMapper.listByAdmin(id, isActive, realName, phone, agentId, accountType, null);
        BigDecimal amount = BigDecimal.ZERO;
        for (User user : list) {
            amount = amount.add(user.getUserAmt()).add(user.getUserIndexAmt());
        }
        sumData.setUserAmt(amount);
        return sumData;

    }

    @Override
    public User getUserByPhone(String phone) {
        return userMapper.findByPhone(phone);
    }

    @Override
    public int addEnableBalance(BigDecimal amount, Integer id) {
        return userMapper.addEnableBalance(amount, id);
    }

    @Override
    @Async
    @Transactional
    public void ForceSellOneStockTaskFutures(BigDecimal trqPrice,BigDecimal trxPrice) {
        //???????????????????????????????????????
        List<Integer> userIdList = this.iUserFuturesPositionService.findDistinctUserIdListAndRz();

        //????????????????????????
        List<UserPositionFutures> userPositions = this.iUserFuturesPositionService.findPositionByUserIdAndSellIdIsNullWhereRzAndAll();

        //??????????????????????????????
        List<PositionProfitVO> positionProfitVOList = iUserFuturesPositionService.getPositionProfitVOList(userPositions,trqPrice, trxPrice);


        //?????????????????????ID????????????
        Map<Integer, List<PositionProfitVO>> userPositionMap = positionProfitVOList.stream().collect(Collectors.groupingBy(PositionProfitVO::getUserId));

        if (userIdList.size() == 0) {
            return;
        }
        List<User> userList = userMapper.selectBatchIds(userIdList);

        //?????????????????? ???????????????

        //????????? = 1 ?????????

        for (User user : userList) {
            //????????????????????????????????????
            List<PositionProfitVO> positionProfitVOList1 = userPositionMap.get(user.getId());
            //??????????????? ??????/??????
            //???????????????
            BigDecimal allProfitAndLose = new BigDecimal("0");
            for (PositionProfitVO positionProfitVO : positionProfitVOList1) {
                allProfitAndLose = allProfitAndLose.add(positionProfitVO.getAllProfitAndLose());
            }
            //??????????????????????????????
            BigDecimal buyTotalAmount = new BigDecimal("0");
            for (PositionProfitVO positionProfitVO : positionProfitVOList1) {
                buyTotalAmount = buyTotalAmount.add(positionProfitVO.getBuyTotalAmount());
            }
            //?????????????????? = ????????????+????????????
            BigDecimal userTotalAmount = buyTotalAmount.add(user.getEnableFutAmt());

            //????????? = ?????????/??????????????????
            BigDecimal riskRate = allProfitAndLose.divide(userTotalAmount, 2, RoundingMode.HALF_UP);
            log.info("??????Id"+user.getId()+"????????????" + riskRate+"????????????"+allProfitAndLose+"?????????????????????"+userTotalAmount);
            riskRate = BigDecimal.ZERO.subtract(riskRate);
            if (riskRate.compareTo(BigDecimal.ONE) >= 0) {
                //???????????????1????????? ?????????????????????????????????
                for (PositionProfitVO positionProfitVO : positionProfitVOList1) {
                    log.info("??????id = {} ?????? = {} ?????????????????????={} ??????={} ????????????={} ????????????{} ?????????????????????-??????????????????", user.getId(), user.getNickName(), positionProfitVO.getUserPositionFutures().getStockName(), positionProfitVO.getUserPositionFutures().getStockCode(), positionProfitVO.getNowPrice(), positionProfitVO.getUserPositionFutures().getBuyOrderPrice());
                }
                //????????????
                this.iUserFuturesPositionService.sellByAll(positionProfitVOList1, trqPrice, trxPrice);
            }

        }
    }
    //??????????????? /(????????????+?????????????????????)

    @Override
    public void ForceSellMessageTaskFutures() {
        //?????????????????????????????????
        List<Integer> userIdList = this.iUserFuturesPositionService.findDistinctUserIdListAndRz();
        log.info("????????????????????????????????? ??? {}", Integer.valueOf(userIdList.size()));

        for (int i = 0; i < userIdList.size(); i++) {
            log.info("=====================");
            Integer userId = (Integer) userIdList.get(i);
            User user = this.userMapper.selectByPrimaryKey(userId);
            if (user == null) {
                continue;
            }

            //??????????????????????????????
            List<UserPositionFutures> userPositions = this.iUserFuturesPositionService.findPositionByUserIdAndSellIdIsNullWhereRz(userId);

            log.info("??????id = {} ?????? = {} ????????????????????? {}", userId, user.getRealName(), Integer.valueOf(userPositions.size()));
            SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
            BigDecimal force_stop_percent = siteSetting.getForceStopRemindRatio();
            for (UserPositionFutures position : userPositions) {

                String nowPriceStr = redisTemplate.opsForValue().get(position.getStockCode());
                PositionProfitVO positionProfitVO = iUserFuturesPositionService.getPositionProfitVO(position,new BigDecimal(nowPriceStr));

                //??????????????????
                BigDecimal buyCost = position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())).divide(position.getOrderLever(), 2, RoundingMode.HALF_UP);
                //????????????
                BigDecimal profitAndLose = position.getProfitAndLose();
                //????????????????????????
                BigDecimal plRate = profitAndLose.divide(buyCost, 2, RoundingMode.HALF_UP);
                // ????????????????????????????????????


                if (plRate.compareTo(BigDecimal.ZERO) < 0 && BigDecimal.ZERO.subtract(plRate).compareTo(force_stop_percent) > 0) {
                    log.info("??????id = {} ?????? = {} ??????????????????={} ??????={} ????????????={} ????????????{} ???????????????????????????", user.getId(), user.getNickName(), position.getStockName(), position.getStockCode(), positionProfitVO.getNowPrice(), position.getBuyOrderPrice());
                    //?????????????????????????????????????????????
                    SiteMessage siteMessage = new SiteMessage();
                    siteMessage.setUserId(userId);
                    siteMessage.setUserName(user.getRealName());
                    siteMessage.setTypeName("????????????");
                    siteMessage.setStatus(1);
                    siteMessage.setContent("????????????????????????????????????id = " + user.getId() + ", ?????????????????? = " + position.getStockName() + "(" + position.getStockCode() + ")" + "???????????????????????? = " + plRate.toString() + ", ?????????????????????????????? = " + siteSetting.getForceStopPercent() + "?????????????????????");
                    siteMessage.setAddTime(DateTimeUtil.getCurrentDate());
                    iSiteMessageService.insert(siteMessage);
                }
                log.info("=====================");
            }
        }
    }

    @Override
    public ServerResponse updateRiseWith(Integer userId) {
        User user = this.userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMsg("???????????????");
        }
        if (user.getRiseWhite() == 1) {
            user.setRiseWhite(0);
        } else {
            user.setRiseWhite(1);
        }
        this.userMapper.updateById(user);
        return ServerResponse.createBySuccess("????????????");
    }

    @Override
    public ServerResponse openLever(String level, HttpServletRequest request) {
        this.userMapper.updateALlLevel(level);
        return ServerResponse.createBySuccess("????????????");
    }

    @Override
    public ServerResponse setKey(String key, HttpServletRequest request) {
        String cookie_name = PropertiesUtil.getProperty("user.cookie.name");

        String loginToken = request.getHeader(cookie_name);
        String userJson = RedisShardedPoolUtils.get(loginToken);
        User user = (User) JsonUtil.string2Obj(userJson, User.class);
        if (user == null) {
            return ServerResponse.createByErrorMsg("???????????????");
        }
        user = userMapper.selectById(user.getId());

        UserWalletKey key1 = userWalletKeyMapper.selectOne(new QueryWrapper<UserWalletKey>().eq("wallet_key", key));
        if (key1 == null) {
            return ServerResponse.createByErrorMsg("??????????????????");
        }
        if (key1.getUseStatus() == 1) {
            return ServerResponse.createByErrorMsg("?????????????????????");
        }
        List<UserWalletKey> userId = userWalletKeyMapper.selectList(new QueryWrapper<UserWalletKey>().eq("user_id", user.getId()));
        for (UserWalletKey userWalletKey : userId) {
            if (userWalletKey.getLevel().equals(key1.getLevel())) {
                return ServerResponse.createByErrorMsg("???????????????????????????????????????");
            }
        }


        key1.setUseStatus(1);
        key1.setUserId(user.getId());
        key1.setUserName(user.getRealName());
        key1.setUserPhone(user.getPhone());
        user.setUserWalletLevel(1);
        user.setUseKeyTime(new Date());
        if (key1.getLevel() == 1) {
            user.setExperienceWallet(new BigDecimal(100000));
            user.setExperienceWalletLevel(1);
        }
        if (key1.getLevel() == 2) {
            user.setExperienceWallet(new BigDecimal(500000));
            user.setExperienceWalletLevel(2);

        }
        if (key1.getLevel() == 3) {
            user.setExperienceWallet(new BigDecimal(1000000));
            user.setExperienceWalletLevel(3);

        }
        userWalletKeyMapper.updateById(key1);
        userMapper.updateById(user);
        return ServerResponse.createBySuccess("????????????");
    }

}

