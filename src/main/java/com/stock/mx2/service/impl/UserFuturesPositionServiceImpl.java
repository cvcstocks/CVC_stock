package com.stock.mx2.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.*;
import com.stock.mx2.pojo.*;
import com.stock.mx2.service.*;
import com.stock.mx2.utils.DateTimeUtil;
import com.stock.mx2.utils.KeyUtils;
import com.stock.mx2.utils.Pager;
import com.stock.mx2.utils.stock.BuyAndSellUtils;
import com.stock.mx2.vo.position.AdminPositionVO;
import com.stock.mx2.vo.position.PositionProfitVO;
import com.stock.mx2.vo.position.UserPositionVO;
import com.stock.mx2.vo.stockfutures.FuturesVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class
UserFuturesPositionServiceImpl extends ServiceImpl<UserPositionFuturesMapper, UserPositionFutures> implements IUserFuturesPositionService {
    private static final Logger log = LoggerFactory.getLogger(UserPositionServiceImpl.class);

    @Autowired
    private IUserService iUserService;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserCashDetailMapper userCashDetailMapper;
    @Autowired
    private UserPositionItemFuturesMapper userPositionItemFuturesMapper;
    @Autowired
    private IStockFuturesService iStockFuturesService;

    @Autowired
    private StockFuturesMapper stockFuturesMapper;
    @Autowired
    private ISiteSettingService iSiteSettingService;

    @Autowired
    private ISiteProductService iSiteProductService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Override
    public ServerResponse findUserFuturesPositionByCode(HttpServletRequest request, String fuCode) {
        User user = this.iUserService.getCurrentUser(request);

        if (user == null) {
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        List<UserPositionFutures> positionList = baseMapper.selectList(new QueryWrapper<UserPositionFutures>().eq("futures_code", fuCode).eq("user_id", user.getId()));
        if (positionList.size() == 0) {
            return ServerResponse.createBySuccessMsg("沒有持倉");
        }

        List<UserPositionVO> userPositionVOList = new ArrayList<>();
        for (UserPositionFutures userPositionFutures : positionList) {
            UserPositionVO userPositionVO = assembleUserPositionVO(userPositionFutures);
            userPositionVOList.add(userPositionVO);
        }

        return ServerResponse.createBySuccess(userPositionVOList);

    }

    @Override
    public ServerResponse wallet(HttpServletRequest request) {
        User user = this.iUserService.getCurrentUser(request);

        if (user == null) {
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        user = userMapper.selectById(user.getId());
        List<UserPositionFutures> positionList = baseMapper.selectList(new QueryWrapper<UserPositionFutures>().eq("user_id", user.getId()));

        List<UserPositionVO> userPositionVOList = new ArrayList<>();
        for (UserPositionFutures userPositionFutures : positionList) {
            UserPositionVO userPositionVO = assembleUserPositionVO(userPositionFutures);
            userPositionVOList.add(userPositionVO);
        }
        BigDecimal allLost = BigDecimal.ZERO;

        BigDecimal cost = BigDecimal.ZERO;
        for (UserPositionVO userPositionVO : userPositionVOList) {
            allLost = allLost.add(userPositionVO.getAllProfitAndLose());
            cost = cost.add(userPositionVO.getOrderTotalPrice());
        }
        BigDecimal riskRate = BigDecimal.ZERO;
        if (cost.add(user.getUserFutAmt()).compareTo(BigDecimal.ZERO) != 0) {
            //风险率
            riskRate = allLost.divide(cost.add(user.getUserFutAmt()), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));

        }


        Map<String, String> map = new HashMap<>();
        //未实现盈亏
        map.put("allLost", allLost.toString());
        //风险率
        map.put("riskRate", riskRate.toString());
        //总成本
        map.put("cost", cost.toString());
        //当前余额
        map.put("userFutAmt", user.getUserFutAmt().toString());
        return ServerResponse.createBySuccess(map);


    }

    @Override
    public ServerResponse updatePrice(String positionSn, BigDecimal buyPrice, Date orderTime) {
        UserPositionFutures position = baseMapper.selectOne(new QueryWrapper<UserPositionFutures>().eq("position_sn", positionSn));

        List<UserPositionItemFutures> userPositionItemList = userPositionItemFuturesMapper.selectList(new QueryWrapper<UserPositionItemFutures>().eq("position_sn", positionSn));

        position.setBuyOrderPrice(buyPrice);
        position.setBuyOrderTime(orderTime);
        position.setOrderTotalPrice(new BigDecimal(position.getOrderNum()).multiply(buyPrice));
        for (UserPositionItemFutures userPositionItem : userPositionItemList) {
            if (userPositionItem.getBuyType() == 0) {
                userPositionItem.setOrderTotalPrice(new BigDecimal(userPositionItem.getOrderNum()).multiply(buyPrice));
                userPositionItem.setBuyOrderPrice(buyPrice);
                userPositionItem.setOrderTime(orderTime);
                userPositionItemFuturesMapper.updateById(userPositionItem);
            }
        }
        baseMapper.updateById(position);
        return ServerResponse.createBySuccess();
    }

    @Override
    public ServerResponse del(Integer positionId) {
        if (positionId == null) {
            return ServerResponse.createByErrorMsg("id不能為空");
        }
        UserPositionFutures position = this.baseMapper.selectById(positionId);
        if (position == null) {
            ServerResponse.createByErrorMsg("該持倉不存在");
        }
        /*if (position.getSellOrderId() == null) {
            return ServerResponse.createByErrorMsg("持倉單不能刪除！");
        }*/
        int updateCount = this.baseMapper.deleteById(positionId);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("刪除成功");
        }
        return ServerResponse.createByErrorMsg("刪除失敗");
    }

    @Override
    public ServerResponse lock(Integer positionId, Integer state, String lockMsg) {
        if (positionId == null || state == null) {
            return ServerResponse.createByErrorMsg("參數不能為空");
        }

        UserPositionFutures position = this.baseMapper.selectById(positionId);
        if (position == null) {
            return ServerResponse.createByErrorMsg("持倉不存在");
        }

        if (position.getSellOrderId() != null) {
            return ServerResponse.createByErrorMsg("平倉單不能鎖倉");
        }

        if (state.intValue() == 1 &&
                StringUtils.isBlank(lockMsg)) {
            return ServerResponse.createByErrorMsg("鎖倉提示信息必填");
        }


        if (state.intValue() == 1) {
            position.setIsLock(Integer.valueOf(1));
            position.setLockMsg(lockMsg);
        } else {
            position.setIsLock(Integer.valueOf(0));
        }

        int updateCount = this.baseMapper.updateById(position);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMsg("操作成功");
        }
        return ServerResponse.createByErrorMsg("操作失敗");
    }

    @Override
    public List<AdminPositionVO> listByAdminExport(String sort, String sortColmn, Integer agentId, Integer positionType, Integer state, Integer userId, String positionSn, String beginTime, String endTime, int pageNum, int pageSize, Integer buyType) {


        Timestamp begin_time = null;
        if (StringUtils.isNotBlank(beginTime)) {
            begin_time = DateTimeUtil.searchStrToTimestamp(beginTime);
        }
        Timestamp end_time = null;
        if (StringUtils.isNotBlank(endTime)) {
            end_time = DateTimeUtil.searchStrToTimestamp(endTime);
        }


        if (state == 0) {
            List<UserPositionFutures> userPositions = this.baseMapper.listByAgent(positionType, state, userId, agentId, positionSn, begin_time, end_time, buyType, sort, sortColmn);
            List<AdminPositionVO> adminPositionVOS = assembleAdminPositionVOList(userPositions);
            if (sortColmn != null) {
                switch (sortColmn) {
                    case "sellOrderPrice":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getSellOrderPrice().compareTo(o2.getSellOrderPrice());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "buyOrderPrice":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getBuyOrderPrice().compareTo(o2.getBuyOrderPrice());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "now_price":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = new BigDecimal(o1.getNow_price()).compareTo(new BigDecimal(o2.getNow_price()));
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;

                    case "profitAndLose":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getProfitAndLose().compareTo(o2.getProfitAndLose());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "allProfitAndLose":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getAllProfitAndLose().compareTo(o2.getAllProfitAndLose());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                }
            }
            return adminPositionVOS;
        } else {
            List<UserPositionItemFutures> userPositions = this.userPositionItemFuturesMapper.listByAgent(positionType, state, userId, agentId, positionSn, begin_time, end_time, buyType, sort, sortColmn);
            List<AdminPositionVO> adminPositionVOS = Lists.newArrayList();
            BigDecimal profitAndLose = BigDecimal.ZERO;
            Integer orderNumber = 0;
            BigDecimal buyTotalAmount = BigDecimal.ZERO;
            BigDecimal allProfitAndLose = BigDecimal.ZERO;
            for (UserPositionItemFutures position : userPositions) {
                AdminPositionVO adminPositionVO = assembleAdminPositionItemVO(position);
                adminPositionVOS.add(adminPositionVO);
                if (adminPositionVO.getProfitAndLose() != null) {
                    profitAndLose = profitAndLose.add(adminPositionVO.getProfitAndLose().subtract(adminPositionVO.getOrderFee()));
                }
                if (adminPositionVO.getAllProfitAndLose() != null) {
                    allProfitAndLose = allProfitAndLose.add(adminPositionVO.getAllProfitAndLose());
                }
                if (adminPositionVO.getBuyTotalAmount() != null) {
                    buyTotalAmount = buyTotalAmount.add(adminPositionVO.getBuyTotalAmount());
                }
                if (adminPositionVO.getOrderNum() != null) {
                    orderNumber = orderNumber + adminPositionVO.getOrderNum();
                }
            }

            if (sortColmn != null) {
                switch (sortColmn) {
                    case "sellOrderPrice":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getSellOrderPrice().compareTo(o2.getSellOrderPrice());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "buyOrderPrice":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getBuyOrderPrice().compareTo(o2.getBuyOrderPrice());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "now_price":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = new BigDecimal(o1.getNow_price()).compareTo(new BigDecimal(o2.getNow_price()));
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;

                    case "profitAndLose":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getProfitAndLose().compareTo(o2.getProfitAndLose());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "allProfitAndLose":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getAllProfitAndLose().compareTo(o2.getAllProfitAndLose());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                }
            }

            return adminPositionVOS;
        }

    }

    @Override
    public ServerResponse listByAdmin(Integer sum, String sort, String sortColmn, Integer agentId, Integer positionType, Integer state, Integer userId, String positionSn, String beginTime, String endTime, int pageNum, int pageSize, Integer buyType) {


        Timestamp begin_time = null;
        if (StringUtils.isNotBlank(beginTime)) {
            begin_time = DateTimeUtil.searchStrToTimestamp(beginTime);
        }
        Timestamp end_time = null;
        if (StringUtils.isNotBlank(endTime)) {
            end_time = DateTimeUtil.searchStrToTimestamp(endTime);
        }
        Page<UserPosition> page = null;

        if (sum != 1 && (sortColmn == null || "".equals(sortColmn))) {
            page = PageHelper.startPage(pageNum, pageSize);
        }


        if (state == 0) {
            List<UserPositionFutures> userPositions = this.baseMapper.listByAgent(positionType, state, userId, agentId, positionSn, begin_time, end_time, buyType, sort, sortColmn);
            BigDecimal profitAndLose = BigDecimal.ZERO;
            Integer orderNumber = 0;
            BigDecimal buyTotalAmount = BigDecimal.ZERO;
            BigDecimal allProfitAndLose = BigDecimal.ZERO;

            List<AdminPositionVO> adminPositionVOS = assembleAdminPositionVOList(userPositions);

            for (AdminPositionVO adminPositionVO : adminPositionVOS) {
                if (adminPositionVO.getProfitAndLose() != null) {
                    profitAndLose = profitAndLose.add(adminPositionVO.getProfitAndLose().subtract(adminPositionVO.getOrderFee()));
                }
                if (adminPositionVO.getAllProfitAndLose() != null) {
                    allProfitAndLose = allProfitAndLose.add(adminPositionVO.getAllProfitAndLose());
                }
                if (adminPositionVO.getBuyTotalAmount() != null) {
                    buyTotalAmount = buyTotalAmount.add(adminPositionVO.getBuyTotalAmount());
                }
                if (adminPositionVO.getOrderNum() != null) {
                    orderNumber = orderNumber + adminPositionVO.getOrderNum();
                }
            }

            if (sortColmn != null) {
                switch (sortColmn) {
                    case "sellOrderPrice":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getSellOrderPrice().compareTo(o2.getSellOrderPrice());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "buyOrderPrice":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getBuyOrderPrice().compareTo(o2.getBuyOrderPrice());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "now_price":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = new BigDecimal(o1.getNow_price()).compareTo(new BigDecimal(o2.getNow_price()));
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;

                    case "profitAndLose":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getProfitAndLose().compareTo(o2.getProfitAndLose());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "allProfitAndLose":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getAllProfitAndLose().compareTo(o2.getAllProfitAndLose());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                }
            }

            //adminPositionVOS 根據
            Pager<AdminPositionVO> pager = Pager.create(adminPositionVOS, pageSize);
            List<AdminPositionVO> page1 = pager.getPagedList(pageNum);
            AdminPositionPageVo adminPositionPageVo = new AdminPositionPageVo();
            adminPositionPageVo.setList(page1);
            adminPositionPageVo.setTotal(adminPositionVOS.size());

            if (sum != 1 && (sortColmn == null || "".equals(sortColmn))) {
                PageInfo pageInfo = new PageInfo(page);
                adminPositionPageVo.setList(adminPositionVOS);
                adminPositionPageVo.setTotal(Integer.valueOf(pageInfo.getTotal() + ""));
            }
            adminPositionPageVo.setPageNum(pageNum);
            adminPositionPageVo.setPageSize(pageSize);
            adminPositionPageVo.setAllProfitAndLose(allProfitAndLose);
            adminPositionPageVo.setProfitAndLose(profitAndLose);
            adminPositionPageVo.setOrderNum(orderNumber);
            adminPositionPageVo.setBuyTotalAmount(buyTotalAmount);
            return ServerResponse.createBySuccess(adminPositionPageVo);
        } else {
            List<UserPositionItemFutures> userPositions = this.userPositionItemFuturesMapper.listByAgent(positionType, state, userId, agentId, positionSn, begin_time, end_time, buyType, sort, sortColmn);
            List<AdminPositionVO> adminPositionVOS = Lists.newArrayList();
            BigDecimal profitAndLose = BigDecimal.ZERO;
            Integer orderNumber = 0;
            BigDecimal buyTotalAmount = BigDecimal.ZERO;
            BigDecimal allProfitAndLose = BigDecimal.ZERO;
            for (UserPositionItemFutures position : userPositions) {
                AdminPositionVO adminPositionVO = assembleAdminPositionItemVO(position);
                adminPositionVOS.add(adminPositionVO);
                if (adminPositionVO.getProfitAndLose() != null) {
                    profitAndLose = profitAndLose.add(adminPositionVO.getProfitAndLose().subtract(adminPositionVO.getOrderFee()));
                }
                if (adminPositionVO.getAllProfitAndLose() != null) {
                    allProfitAndLose = allProfitAndLose.add(adminPositionVO.getAllProfitAndLose());
                }
                if (adminPositionVO.getBuyTotalAmount() != null) {
                    buyTotalAmount = buyTotalAmount.add(adminPositionVO.getBuyTotalAmount());
                }
                if (adminPositionVO.getOrderNum() != null) {
                    orderNumber = orderNumber + adminPositionVO.getOrderNum();
                }
            }

            if (sortColmn != null) {
                switch (sortColmn) {
                    case "sellOrderPrice":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getSellOrderPrice().compareTo(o2.getSellOrderPrice());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "buyOrderPrice":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getBuyOrderPrice().compareTo(o2.getBuyOrderPrice());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "now_price":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = new BigDecimal(o1.getNow_price()).compareTo(new BigDecimal(o2.getNow_price()));
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;

                    case "profitAndLose":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getProfitAndLose().compareTo(o2.getProfitAndLose());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                    case "allProfitAndLose":
                        adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                            @Override
                            //按照code的大小升序排列
                            public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                                int i = o1.getAllProfitAndLose().compareTo(o2.getAllProfitAndLose());
                                if ("ASC".equals(sort)) {
                                    return i;
                                } else {
                                    return Integer.compare(0, i);
                                }
                            }

                        });
                        break;
                }
            }
            //adminPositionVOS 根據
            Pager<AdminPositionVO> pager = Pager.create(adminPositionVOS, pageSize);
            List<AdminPositionVO> page1 = pager.getPagedList(pageNum);
            AdminPositionPageVo adminPositionPageVo = new AdminPositionPageVo();
            adminPositionPageVo.setList(page1);
            adminPositionPageVo.setTotal(adminPositionVOS.size());
            if (sum != 1 && (sortColmn == null || "".equals(sortColmn))) {

                PageInfo pageInfo = new PageInfo(page);
                adminPositionPageVo.setTotal(Integer.valueOf(pageInfo.getTotal() + ""));
            }
            adminPositionPageVo.setPageNum(pageNum);
            adminPositionPageVo.setPageSize(pageSize);
            adminPositionPageVo.setAllProfitAndLose(allProfitAndLose);
            adminPositionPageVo.setProfitAndLose(profitAndLose);
            adminPositionPageVo.setOrderNum(orderNumber);
            adminPositionPageVo.setBuyTotalAmount(buyTotalAmount);
            return ServerResponse.createBySuccess(adminPositionPageVo);
        }
    }

    @Override
    public ServerResponse listByAdminNb(Integer sum, String sort, String sortColmn, Integer agentId, Integer positionType, Integer state, Integer userId, String positionSn, String beginTime, String endTime, int pageNum, int pageSize, Integer buyType) {
        Page<UserPosition> page = null;
        if (sum != 1 && (sortColmn == null || "".equals(sortColmn))) {
            page = PageHelper.startPage(pageNum, pageSize);
        }

        Timestamp begin_time = null;
        if (StringUtils.isNotBlank(beginTime)) {
            begin_time = DateTimeUtil.searchStrToTimestamp(beginTime);
        }
        Timestamp end_time = null;
        if (StringUtils.isNotBlank(endTime)) {
            end_time = DateTimeUtil.searchStrToTimestamp(endTime);
        }


        List<UserPositionFutures> userPositions = this.baseMapper.listByAgentNb(positionType, state, userId, agentId, positionSn, begin_time, end_time, buyType, sort, sortColmn);
        List<AdminPositionVO> adminPositionVOS = Lists.newArrayList();

        BigDecimal profitAndLose = BigDecimal.ZERO;

        BigDecimal allProfitAndLose = BigDecimal.ZERO;
        for (UserPositionFutures position : userPositions) {
            AdminPositionVO adminPositionVO = assembleAdminPositionVO(position);
            adminPositionVOS.add(adminPositionVO);
            if (adminPositionVO.getProfitAndLose() != null) {
                profitAndLose = profitAndLose.add(adminPositionVO.getProfitAndLose().subtract(adminPositionVO.getOrderFee()));
            }
            if (adminPositionVO.getAllProfitAndLose() != null) {
                allProfitAndLose = allProfitAndLose.add(adminPositionVO.getAllProfitAndLose());
            }
        }

        if (sortColmn != null) {
            switch (sortColmn) {
                case "sellOrderPrice":
                    adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                        @Override
                        //按照code的大小升序排列
                        public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                            int i = o1.getSellOrderPrice().compareTo(o2.getSellOrderPrice());
                            if ("ASC".equals(sort)) {
                                return i;
                            } else {
                                return Integer.compare(0, i);
                            }
                        }

                    });
                    break;
                case "buyOrderPrice":
                    adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                        @Override
                        //按照code的大小升序排列
                        public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                            int i = o1.getBuyOrderPrice().compareTo(o2.getBuyOrderPrice());
                            if ("ASC".equals(sort)) {
                                return i;
                            } else {
                                return Integer.compare(0, i);
                            }
                        }

                    });
                    break;
                case "now_price":
                    adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                        @Override
                        //按照code的大小升序排列
                        public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                            int i = new BigDecimal(o1.getNow_price()).compareTo(new BigDecimal(o2.getNow_price()));
                            if ("ASC".equals(sort)) {
                                return i;
                            } else {
                                return Integer.compare(0, i);
                            }
                        }

                    });
                    break;

                case "profitAndLose":
                    adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                        @Override
                        //按照code的大小升序排列
                        public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                            int i = o1.getProfitAndLose().compareTo(o2.getProfitAndLose());
                            if ("ASC".equals(sort)) {
                                return i;
                            } else {
                                return Integer.compare(0, i);
                            }
                        }

                    });
                    break;
                case "allProfitAndLose":
                    adminPositionVOS.sort(new Comparator<AdminPositionVO>() {
                        @Override
                        //按照code的大小升序排列
                        public int compare(AdminPositionVO o1, AdminPositionVO o2) {
                            int i = o1.getAllProfitAndLose().compareTo(o2.getAllProfitAndLose());
                            if ("ASC".equals(sort)) {
                                return i;
                            } else {
                                return Integer.compare(0, i);
                            }
                        }

                    });
                    break;
            }
        }
        //adminPositionVOS 根據
        Pager<AdminPositionVO> pager = Pager.create(adminPositionVOS, pageSize);
        List<AdminPositionVO> page1 = pager.getPagedList(pageNum);

        AdminPositionPageVo adminPositionPageVo = new AdminPositionPageVo();
        adminPositionPageVo.setList(page1);
        adminPositionPageVo.setTotal(adminPositionVOS.size());
        if (sum != 1 && (sortColmn == null || "".equals(sortColmn))) {
            PageInfo pageInfo = new PageInfo(page);
            adminPositionPageVo.setTotal(Integer.valueOf(pageInfo.getTotal() + ""));
        }
        adminPositionPageVo.setPageNum(pageNum);
        adminPositionPageVo.setPageSize(pageSize);
        adminPositionPageVo.setAllProfitAndLose(allProfitAndLose);
        adminPositionPageVo.setProfitAndLose(profitAndLose);

        return ServerResponse.createBySuccess(adminPositionPageVo);
    }


    @Override
    public ServerResponse findMyPositionByCodeAndSpellV2(String stockCode, Integer state, HttpServletRequest request) {
        User user = this.iUserService.getCurrentUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("請先登錄");
        }

        if (state == 0) {
            //查詢目前持倉數據
            List<UserPositionFutures> userPositions = this.baseMapper.findMyPositionByCodeAndSpell(user.getId(), stockCode, state);
            List<UserPositionVO> userPositionVOS = Lists.newArrayList();
            if (userPositions.size() > 0) {
                for (UserPositionFutures position : userPositions) {
                    UserPositionVO userPositionVO = assembleUserPositionVO(position);

                    List<UserPositionItemFutures> userPositionItemList = userPositionItemFuturesMapper.selectList(new QueryWrapper<UserPositionItemFutures>().eq("position_sn", position.getPositionSn()).orderByDesc("id"));

                    for (UserPositionItemFutures userPositionItem : userPositionItemList) {
                        if (userPositionItem.getBuyType() == 0) {

                            //总盈亏 = 盈亏 - 买入手续费 - 预估卖出手续费 - 预估卖出印花税
                            //卖出手续费
                            BigDecimal buy_fee_amt = new BigDecimal(userPositionItem.getOrderNum()).divide(new BigDecimal("1000")).multiply(new BigDecimal("50"));

                            userPositionItem.setOrderFee(buy_fee_amt);
                            //浮動盈虧

                            BigDecimal profitAndLose = getLose(position.getBuyOrderPrice(), userPositionItem.getSellOrderPrice(), new BigDecimal(position.getOrderNum()), position.getOrderLever(), position.getOrderDirection());

                            //盈亏= 卖出市值 - 买入市值
                            userPositionItem.setProfitAndLose(profitAndLose);
                        }

                    }
                    BigDecimal buy_fee_amt = new BigDecimal(position.getOrderNum()).divide(new BigDecimal("1000")).multiply(new BigDecimal("50"));
                    userPositionVO.setOrderFee(buy_fee_amt);
                    userPositionVO.setUserPositionItemFuturesList(userPositionItemList);


                    userPositionVOS.add(userPositionVO);
                }
            }
            return ServerResponse.createBySuccess(userPositionVOS);
        } else {
            QueryWrapper<UserPositionItemFutures> queryWrapper = new QueryWrapper<UserPositionItemFutures>().eq("user_id", user.getId()).eq("order_type", 1);
            if (stockCode != null && (!"".equals(stockCode))) {
                queryWrapper = queryWrapper.like("stock_code", stockCode);
            }

            queryWrapper.orderByDesc("id");
            List<UserPositionItemFutures> userPositionItemList = userPositionItemFuturesMapper.selectList(queryWrapper);
            for (UserPositionItemFutures userPositionItem : userPositionItemList) {

                //总盈亏 = 盈亏 - 买入手续费 - 预估卖出手续费 - 预估卖出印花税
                //卖出手续费
                BigDecimal sell_fee_amt = new BigDecimal(userPositionItem.getOrderNum()).divide(new BigDecimal("1000")).multiply(new BigDecimal("100"));
                userPositionItem.setOrderFee(sell_fee_amt);
                userPositionItem.setOrderTotalPrice(userPositionItem.getBuyOrderPrice().multiply(new BigDecimal(userPositionItem.getOrderNum())));
                userPositionItem.setAllProfitAndLose(userPositionItem.getProfitAndLose().subtract(sell_fee_amt.divide(new BigDecimal("2"))));
            }
            return ServerResponse.createBySuccess(userPositionItemList);
        }
    }

    private AdminPositionVO assembleAdminPositionVO(UserPositionFutures position) {
        AdminPositionVO adminPositionVO = new AdminPositionVO();

        adminPositionVO.setId(position.getId());
        adminPositionVO.setPositionSn(position.getPositionSn());
        adminPositionVO.setPositionType(position.getPositionType());
        adminPositionVO.setUserId(position.getUserId());
        adminPositionVO.setNickName(position.getNickName());
        adminPositionVO.setAgentId(position.getAgentId());
        adminPositionVO.setStockName(position.getStockName());
        adminPositionVO.setStockCode(position.getStockCode());
        adminPositionVO.setStockGid(position.getStockGid());
        adminPositionVO.setStockSpell(position.getStockSpell());
        adminPositionVO.setBuyOrderId(position.getBuyOrderId());
        adminPositionVO.setBuyOrderTime(position.getBuyOrderTime());
        adminPositionVO.setBuyOrderPrice(position.getBuyOrderPrice());
        adminPositionVO.setSellOrderId(position.getSellOrderId());
        adminPositionVO.setSellOrderTime(position.getSellOrderTime());
        adminPositionVO.setSellOrderPrice(position.getSellOrderPrice());
        adminPositionVO.setOrderDirection(position.getOrderDirection());
        adminPositionVO.setOrderNum(position.getOrderNum());
        adminPositionVO.setOrderLever(position.getOrderLever());
        adminPositionVO.setOrderTotalPrice(position.getOrderTotalPrice());
        adminPositionVO.setOrderFee(position.getOrderFee());
        adminPositionVO.setOrderSpread(position.getOrderSpread());
        adminPositionVO.setOrderStayFee(position.getOrderStayFee());
        adminPositionVO.setOrderStayDays(position.getOrderStayDays());
        adminPositionVO.setIsLock(position.getIsLock());
        adminPositionVO.setLockMsg(position.getLockMsg());
        adminPositionVO.setStockPlate(position.getStockPlate());

        if (position.getSellOrderTime() != null) {
            adminPositionVO.setProfitAndLose(position.getProfitAndLose());
            adminPositionVO.setAllProfitAndLose(position.getAllProfitAndLose());
            adminPositionVO.setNow_price(position.getSellOrderPrice().toString());
        } else {

            String nowPriceStr = redisTemplate.opsForValue().get(position.getStockCode());

            PositionProfitVO positionProfitVO = getPositionProfitVO(position,new BigDecimal(nowPriceStr));
            adminPositionVO.setProfitAndLose(positionProfitVO.getProfitAndLose());
            adminPositionVO.setAllProfitAndLose(positionProfitVO.getAllProfitAndLose());
            adminPositionVO.setNow_price(positionProfitVO.getNowPrice());
            adminPositionVO.setSellTotalAmount(positionProfitVO.getSellTotalAmount());
            adminPositionVO.setBuyTotalAmount(positionProfitVO.getBuyTotalAmount());
            adminPositionVO.setOrderFee(positionProfitVO.getBuyFee());
        }
        //付出成本
        adminPositionVO.setOrderTotalPrice(position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())).divide(position.getOrderLever(), 6, RoundingMode.HALF_UP));


        return adminPositionVO;
    }

    private List<AdminPositionVO> assembleAdminPositionVOList(List<UserPositionFutures> userPositions) {


        List<AdminPositionVO> adminPositionVOList = new ArrayList<>();

        List<String> codeList = new ArrayList<>();

        for (UserPositionFutures position : userPositions) {
            codeList.add(position.getStockCode());
        }

        Set<String> set = new HashSet<String>();
        set.addAll(codeList);     // 将list所有元素添加到set中    set集合特性会自动去重复
        codeList.clear();
        codeList.addAll(set);


        log.error("导出持仓数据-后台");
        List<FuturesVO> stockListVOList = new ArrayList<>();
        for (String s : codeList) {
            FuturesVO stockListVO = iStockFuturesService.querySingleMarket(s);
            stockListVOList.add(stockListVO);
        }

        for (UserPositionFutures position : userPositions) {


            AdminPositionVO adminPositionVO = new AdminPositionVO();

            adminPositionVO.setId(position.getId());
            adminPositionVO.setPositionSn(position.getPositionSn());
            adminPositionVO.setPositionType(position.getPositionType());
            adminPositionVO.setUserId(position.getUserId());
            adminPositionVO.setNickName(position.getNickName());
            adminPositionVO.setAgentId(position.getAgentId());
            adminPositionVO.setStockName(position.getStockName());
            adminPositionVO.setStockCode(position.getStockCode());
            adminPositionVO.setStockGid(position.getStockGid());
            adminPositionVO.setStockSpell(position.getStockSpell());
            adminPositionVO.setBuyOrderId(position.getBuyOrderId());
            adminPositionVO.setBuyOrderTime(position.getBuyOrderTime());
            adminPositionVO.setBuyOrderPrice(position.getBuyOrderPrice());
            adminPositionVO.setSellOrderId(position.getSellOrderId());
            adminPositionVO.setSellOrderTime(position.getSellOrderTime());
            adminPositionVO.setSellOrderPrice(position.getSellOrderPrice());
            adminPositionVO.setOrderDirection(position.getOrderDirection());
            adminPositionVO.setOrderNum(position.getOrderNum());
            adminPositionVO.setOrderLever(position.getOrderLever());
            adminPositionVO.setOrderTotalPrice(position.getOrderTotalPrice());
            adminPositionVO.setOrderFee(position.getOrderFee());
            adminPositionVO.setOrderSpread(position.getOrderSpread());
            adminPositionVO.setOrderStayFee(position.getOrderStayFee());
            adminPositionVO.setOrderStayDays(position.getOrderStayDays());
            adminPositionVO.setIsLock(position.getIsLock());
            adminPositionVO.setLockMsg(position.getLockMsg());
            adminPositionVO.setStockPlate(position.getStockPlate());

            if (position.getOrderNum() == null || position.getOrderNum() == 0) {
                adminPositionVO.setProfitAndLose(position.getProfitAndLose());
                adminPositionVO.setAllProfitAndLose(position.getAllProfitAndLose());
                adminPositionVO.setNow_price(position.getSellOrderPrice().toString());
            } else {

                for (FuturesVO stockListVO : stockListVOList) {
                    if (position.getStockCode().equals(stockListVO.getCode())) {
                        PositionProfitVO positionProfitVO = getPositionProfitVOByStockVo(position, stockListVO);
                        adminPositionVO.setProfitAndLose(positionProfitVO.getProfitAndLose());
                        adminPositionVO.setAllProfitAndLose(positionProfitVO.getAllProfitAndLose());
                        adminPositionVO.setNow_price(positionProfitVO.getNowPrice());
                        adminPositionVO.setSellTotalAmount(positionProfitVO.getSellTotalAmount());
                        adminPositionVO.setBuyTotalAmount(positionProfitVO.getBuyTotalAmount());
                        adminPositionVO.setOrderFee(positionProfitVO.getBuyFee());
                    }
                }
            }
            //付出成本
            adminPositionVO.setOrderTotalPrice(position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())).divide(position.getOrderLever(), 6, RoundingMode.HALF_UP));
            adminPositionVOList.add(adminPositionVO);


        }

        return adminPositionVOList;
    }

    private PositionProfitVO getPositionProfitVOByStockVo(UserPositionFutures position, FuturesVO stockListVO) {
        String nowPrice = "";


        List<String> stockCodeList = new ArrayList<>();
        stockCodeList.add(position.getStockCode());
        nowPrice = stockListVO.getNowPrice();
        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();

        PositionProfitVO positionProfitVO = new PositionProfitVO();


        //买入成本
        BigDecimal buyCost = new BigDecimal(position.getOrderNum()).divide(position.getOrderLever(), 6, RoundingMode.HALF_UP);

        //買入市值判斷
        BigDecimal all_buy_amt = new BigDecimal(position.getOrderNum());

        BigDecimal profitAndLose = getLose(position.getBuyOrderPrice(), new BigDecimal(nowPrice), new BigDecimal(position.getOrderNum()), position.getOrderLever(), position.getOrderDirection());

        //浮動盈虧
        //賣出市值判斷
        BigDecimal all_sell_amt = all_buy_amt.add(profitAndLose);


        //卖出市值
        positionProfitVO.setSellTotalAmount(all_sell_amt);
        //买入市值
        positionProfitVO.setBuyTotalAmount(all_buy_amt);
        //盈亏= 卖出市值 - 买入市值
        positionProfitVO.setProfitAndLose(profitAndLose);

        //成本
        positionProfitVO.setOrderTotalPrice(buyCost);


        //总盈亏 = 盈亏 - 买入手续费 - 预估卖出手续费
        //买入手续费
        BigDecimal buy_fee_amt = all_buy_amt.divide(new BigDecimal("1000")).multiply(new BigDecimal("50"));
        //卖出手续费
        BigDecimal sell_fee_amt = all_buy_amt.divide(new BigDecimal("1000")).multiply(new BigDecimal("50"));

        BigDecimal allProfitAndLose = positionProfitVO.getProfitAndLose().subtract(buy_fee_amt).subtract(sell_fee_amt);
        positionProfitVO.setAllProfitAndLose(allProfitAndLose);
        positionProfitVO.setNowPrice(nowPrice);
        positionProfitVO.setBuyFee(buy_fee_amt);

        return positionProfitVO;

    }

    private AdminPositionVO assembleAdminPositionItemVO(UserPositionItemFutures position) {
        AdminPositionVO adminPositionVO = new AdminPositionVO();

        adminPositionVO.setId(position.getId());
        adminPositionVO.setPositionSn(position.getPositionSn());
        adminPositionVO.setPositionType(position.getPositionType());
        adminPositionVO.setUserId(position.getUserId());
        adminPositionVO.setNickName(position.getNickName());
        adminPositionVO.setAgentId(position.getAgentId());
        adminPositionVO.setStockName(position.getStockName());
        adminPositionVO.setStockCode(position.getStockCode());
        adminPositionVO.setStockGid(position.getStockGid());
        adminPositionVO.setStockSpell(position.getStockSpell());
        adminPositionVO.setBuyOrderTime(position.getOrderTime());
        adminPositionVO.setBuyOrderPrice(position.getBuyOrderPrice());
        adminPositionVO.setSellOrderTime(position.getOrderTime());
        adminPositionVO.setSellOrderPrice(position.getSellOrderPrice());
        adminPositionVO.setOrderDirection(position.getOrderDirection());
        adminPositionVO.setOrderNum(position.getOrderNum());
        adminPositionVO.setOrderLever(position.getOrderLever());
        adminPositionVO.setOrderTotalPrice(position.getOrderTotalPrice());
        adminPositionVO.setOrderFee(position.getOrderFee());
        adminPositionVO.setOrderSpread(position.getOrderSpread());
        adminPositionVO.setStockPlate(position.getStockPlate());
        adminPositionVO.setProfitAndLose(position.getProfitAndLose());
        adminPositionVO.setAllProfitAndLose(position.getAllProfitAndLose());
        adminPositionVO.setNow_price(position.getSellOrderPrice().toString());
        //付出成本
        adminPositionVO.setOrderTotalPrice(position.getBuyOrderPrice().multiply(new BigDecimal(position.getOrderNum())).divide(position.getOrderLever(), 6, RoundingMode.HALF_UP));


        return adminPositionVO;
    }

    @Override
    @Transactional
    public ServerResponse buy(Integer stockId, Integer buyNum, BigDecimal lever, HttpServletRequest request, Integer direction) throws Exception {


        // 判斷周末不能買
        Date today = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(today);
        int weekday = c.get(Calendar.DAY_OF_WEEK);
//        if (weekday == 1) {
//            return ServerResponse.createByErrorMsg("非工作日，不可交易！");
//        }
//        if (weekday == 7) {
//            return ServerResponse.createByErrorMsg("非工作日，不可交易！");
//        }
//        String mMdd = DateUtil.format(new Date(), "MMdd");
//        if ("0909".equals(mMdd) || "0910".equals(mMdd)) {
//            return ServerResponse.createByErrorMsg("非工作日，不可交易！");
//        }
//        if ("1010".equals(mMdd)) {
//            return ServerResponse.createByErrorMsg("非工作日，不可交易！");
//        }
//
//        if ("1231".equals(mMdd)) {
//            return ServerResponse.createByErrorMsg("非工作日，不可交易！");
//        }

        /*實名認證開關開啟*/
        SiteProduct siteProduct = iSiteProductService.getProductSetting();
        User user = this.iUserService.getCurrentRefreshUser(request);
        if (user == null) {
            return ServerResponse.createBySuccessMsg("請先登錄");
        }

        if (user.getIsLock() != null && user.getIsLock() == 1) {
            return ServerResponse.createByErrorMsg("您的賬號已被凍結，請聯繫客服");
        }
//        if (siteProduct.getRealNameDisplay() && (StringUtils.isBlank(user.getRealName()) || StringUtils.isBlank(user.getIdCard()))) {
//            return ServerResponse.createByErrorMsg("下單失敗，請先實名認證");
//        }
        BigDecimal user_enable_amt = BigDecimal.ZERO;

//
//        String userLever = user.getLever();
//        if (userLever == null) {
//            return ServerResponse.createByErrorMsg("請先開通國際金賬號");
//        }
//        if (userLever.contains(lever.toString())) {
        user_enable_amt = user.getEnableFutAmt();
//        } else {
//            return ServerResponse.createByErrorMsg("請先開通" + lever + "倍國際金賬號");
//        }
        log.info("用戶 {} 下單，股票id = {} ，數量 = {} , 方向 = {} , 槓桿 = {}", user
                .getId(), stockId, buyNum, 1, lever);
//        if (siteProduct.getRealNameDisplay() && user.getIsLock().intValue() == 1) {
//            return ServerResponse.createByErrorMsg("下單失敗，帳戶已被鎖定");
//        }
//        if (siteProduct.getHolidayDisplay()) {
//            return ServerResponse.createByErrorMsg("非工作日，不可交易！");
//        }

        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("下單出錯，網站設置表不存在");
            return ServerResponse.createByErrorMsg("下單失敗，系統設置錯誤");
        }
//
//        String am_begin = siteSetting.getTransAmBegin();
//        String am_end = siteSetting.getTransAmEnd();
//        boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
//        log.info("是否在上午交易時間 = {}", am_flag);
//        if (!am_flag) {
//            return ServerResponse.createByErrorMsg("下單失敗，不在交易時段內");
//        }

        StockFutures stock = this.stockFuturesMapper.selectById(stockId);

        if (stock == null) {
            return ServerResponse.createByErrorMsg("下單失敗，股票代碼錯誤");
        }


        List dbPosition = findPositionByStockCodeAndTimes(siteSetting.getBuySameTimes().intValue(), stock
                .getFuturesCode(), user.getId());
        if (dbPosition.size() >= siteSetting.getBuySameNums().intValue()) {
            return ServerResponse.createByErrorMsg("頻繁交易," + siteSetting.getBuySameTimes() + "分鐘內同一股票持倉不得超過" + siteSetting
                    .getBuySameNums() + "條");
        }

        Integer transNum = findPositionNumByTimes(siteSetting.getBuyNumTimes().intValue(), user.getId());
        if (transNum.intValue() / 100 >= siteSetting.getBuyNumLots().intValue()) {
            return ServerResponse.createByErrorMsg("頻繁交易," + siteSetting
                    .getBuyNumTimes() + "分鐘內不能超過" + siteSetting.getBuyNumLots() + "手");
        }

        if (buyNum.intValue() < siteSetting.getBuyMinNum().intValue()) {
            return ServerResponse.createByErrorMsg("下單失敗，購買數量小於" + siteSetting
                    .getBuyMinNum() + "股");
        }
        if (buyNum.intValue() > siteSetting.getBuyMaxNum().intValue()) {
            return ServerResponse.createByErrorMsg("下單失敗，購買數量大於" + siteSetting
                    .getBuyMaxNum() + "股");
        }


        System.out.println(stock.getFuturesName());

        FuturesVO stockListVO = iStockFuturesService.querySingleMarket(stock.getFuturesCode());

        boolean buyTime = BuyAndSellUtils.isTransTime("09:00", "09:05");

        BigDecimal now_price
                = new BigDecimal(stockListVO.getNowPrice());

        BigDecimal nowPrice1 = new BigDecimal(stockListVO.getNowPrice());
        if (nowPrice1.compareTo(BigDecimal.ZERO) != 0) {
            now_price = nowPrice1;
        }


        if (now_price.compareTo(new BigDecimal("0")) == 0) {
            return ServerResponse.createByErrorMsg("報價0，請稍後再試");
        }


        //BigDecimal buy_amt = now_price.multiply(new BigDecimal(buyNum.intValue())).divide(new BigDecimal(lever.intValue())).setScale(2, 4);
        BigDecimal buy_amt = new BigDecimal(buyNum);


        //BigDecimal buy_amt_autual = now_price.multiply(new BigDecimal(buyNum.intValue())).divide(new BigDecimal(lever.intValue()), 2, 4);

        BigDecimal buy_amt_autual = buy_amt;

        int compareInt = buy_amt_autual.compareTo(new BigDecimal(siteSetting.getBuyMinAmt().intValue()));
        if (compareInt == -1) {
            return ServerResponse.createByErrorMsg("下單失敗，購買金額小於" + siteSetting
                    .getBuyMinAmt() + "元");
        }

        String s = redisTemplate.opsForValue().get("buyFut" + user.getId());
        if (s != null) {
            return ServerResponse.createByErrorMsg("請勿重複提交");
        }
        redisTemplate.opsForValue().set("buyFut"+user.getId(), "buyFut");




        BigDecimal max_buy_amt = user_enable_amt.multiply(siteSetting.getBuyMaxAmtPercent());
        int compareCwInt = buy_amt_autual.compareTo(max_buy_amt);
        if (compareCwInt == 1) {
            redisTemplate.delete("buyFut"+user.getId());
            return ServerResponse.createByErrorMsg("下單失敗，不能超過可用資金的" + siteSetting
                    .getBuyMaxAmtPercent().multiply(new BigDecimal("100")) + "%");
        }


        int compareUserAmtInt = user_enable_amt.compareTo(buy_amt_autual);
        log.info("用戶可用金額 = {}  實際購買金額 =  {}", user_enable_amt, buy_amt_autual);
        log.info("比較 用戶金額 和 實際 購買金額 =  {}", Integer.valueOf(compareUserAmtInt));
        if (compareUserAmtInt == -1) {
            redisTemplate.delete("buyFut"+user.getId());
            return ServerResponse.createByErrorMsg("下單失敗，國際金可用金額小於" + buy_amt_autual + "元");
        }
        if (user.getUserFutAmt().compareTo(new BigDecimal("0")) == -1) {
            redisTemplate.delete("buyFut"+user.getId());
            return ServerResponse.createByErrorMsg("失敗，國際金總資金小於0");
        }

        UserPositionFutures userPosition = baseMapper.selectOne(new QueryWrapper<UserPositionFutures>().eq("order_direction", direction).eq("user_id", user.getId()).eq("stock_code", stock.getFuturesCode()).eq("order_lever", lever));
        if (userPosition == null) {
            userPosition = new UserPositionFutures();
            userPosition.setPositionType(user.getAccountType());
            userPosition.setUserId(user.getId());
            userPosition.setNickName(user.getRealName());
            userPosition.setAgentId(user.getAgentId());
            userPosition.setStockCode(stock.getFuturesCode());
            userPosition.setStockName(stock.getFuturesName());
            userPosition.setStockGid(stock.getFuturesCode());
            userPosition.setStockSpell(stock.getFuturesName() + stock.getFuturesCode());
            userPosition.setBuyType(1);
            userPosition.setStockPlate(stock.getFuturesName() + stock.getFuturesCode());
            userPosition.setIsLock(0);
            userPosition.setOrderDirection(direction);
            userPosition.setOrderLever(lever);
            userPosition.setOrderStayDays(0);
            userPosition.setOrderStayFee(new BigDecimal("0"));

            userPosition.setBuyOrderPrice(BigDecimal.ZERO);
            userPosition.setOrderNum(0);
            userPosition.setOrderTotalPrice(BigDecimal.ZERO);
            this.baseMapper.insert(userPosition);
        }

        if (userPosition.getOrderNum() == null || userPosition.getOrderNum() == 0) {
            userPosition.setBuyOrderTime(new Date());
            userPosition.setMarginAdd(BigDecimal.ZERO);
            userPosition.setPositionSn(KeyUtils.getUniqueKey());
        }

        BigDecimal buyOrderPrice1 = userPosition.getBuyOrderPrice().multiply(new BigDecimal(userPosition.getOrderNum()));
        BigDecimal buyOrderPrice2 = now_price.multiply(new BigDecimal(buyNum));
        BigDecimal buyOrderPrice = buyOrderPrice1.add(buyOrderPrice2);
        buyOrderPrice = buyOrderPrice.divide(new BigDecimal(userPosition.getOrderNum() + buyNum), 6, RoundingMode.HALF_UP);
        userPosition.setBuyOrderPrice(buyOrderPrice);
        userPosition.setOrderNum(userPosition.getOrderNum() + buyNum);

        BigDecimal buy_fee_amt = new BigDecimal(buyNum / 1000 * 50);
        log.info("用戶購買手續費（配資后總資金 * 百分比） = {}", buy_fee_amt);
//        BigDecimal buy_yhs_amt = buy_amt.multiply(siteSetting.getDutyFee()).setScale(2, 4);
//        log.info("用戶購買印花稅（配資后總資金 * 百分比） = {}", buy_yhs_amt);
//        userPosition.setOrderSpread(buy_yhs_amt);
        BigDecimal profit_and_lose = new BigDecimal("0");
        userPosition.setProfitAndLose(profit_and_lose);

        userPosition.setBuyOrderTime(new Date());
        BigDecimal all_profit_and_lose = profit_and_lose.subtract(buy_fee_amt);
        userPosition.setAllProfitAndLose(all_profit_and_lose);
        int insertPositionCount = 0;
        this.baseMapper.updateById(userPosition);
        UserPositionItemFutures userPositionItem = new UserPositionItemFutures();
        userPositionItem.setPositionType(userPosition.getPositionType());
        userPositionItem.setPositionSn(userPosition.getPositionSn());
        userPositionItem.setUserId(userPosition.getUserId());
        userPositionItem.setNickName(userPosition.getNickName());
        userPositionItem.setAgentId(userPosition.getAgentId());
        userPositionItem.setStockName(userPosition.getStockName());
        userPositionItem.setStockCode(userPosition.getStockCode());
        userPositionItem.setStockGid(userPosition.getStockGid());
        userPositionItem.setStockSpell(userPosition.getStockSpell());
        userPositionItem.setBuyType(userPosition.getBuyType());
        userPositionItem.setStockPlate(userPosition.getStockPlate());
        userPositionItem.setOrderTime(new Date());
        userPositionItem.setBuyOrderPrice(now_price);
        userPositionItem.setSellOrderPrice(new BigDecimal("0"));
        userPositionItem.setOrderDirection(userPosition.getOrderDirection());
        userPositionItem.setOrderNum(buyNum);
        userPositionItem.setOrderLever(lever);
        userPositionItem.setOrderTotalPrice(buy_amt);
        userPositionItem.setOrderFee(buy_fee_amt);
        userPositionItem.setOrderType(0);
        userPositionItemFuturesMapper.insert(userPositionItem);

        insertPositionCount = userPosition.getId();
        if (insertPositionCount > 0) {
            user.setEnableFutAmt(user.getEnableFutAmt().subtract(new BigDecimal(buyNum)).subtract(buy_fee_amt));
            user.setUserFutAmt(user.getUserFutAmt().subtract(new BigDecimal(buyNum)).subtract(buy_fee_amt));
            int updateUserCount = this.userMapper.updateById(user);

            UserCashDetail ucd = new UserCashDetail();
            ucd.setPositionId(userPosition.getId());
            ucd.setAgentId(user.getAgentId());
            ucd.setAgentName(user.getAgentName());
            ucd.setUserId(user.getId());
            ucd.setUserName(user.getRealName());
            ucd.setDeType("開倉合約(" + (userPosition.getBuyType() == 0 ? "現股" : "國際金") + ")");
            ucd.setDeAmt(BigDecimal.ZERO.subtract(new BigDecimal(buyNum).add(buy_fee_amt)));
            ucd.setAddTime(new Date());
            ucd.setIsRead(Integer.valueOf(0));
            int insertSxfCount = this.userCashDetailMapper.insert(ucd);

            if (updateUserCount > 0) {

                log.info("【用戶交易下單】修改用戶金額成功");
            } else {
                redisTemplate.delete("buyFut"+user.getId());

                log.error("用戶交易下單】修改用戶金額出錯");
                throw new Exception("用戶交易下單】修改用戶金額出錯");
            }
            //核算代理收入-入倉手續費
            log.info("【用戶交易下單】保存持倉記錄成功");
        } else {
            redisTemplate.delete("buyFut"+user.getId());

            log.error("用戶交易下單】保存持倉記錄出錯");
            throw new Exception("用戶交易下單】保存持倉記錄出錯");
        }
        redisTemplate.delete("buyFut"+user.getId());

        return ServerResponse.createBySuccess("下單成功");
    }

    @Override
    @Transactional
    public ServerResponse sell(String positionSn, int doType, Integer sellNumber) throws Exception {

        log.info("【用戶交易平倉】 positionSn = {} ， dotype = {}", positionSn, Integer.valueOf(doType));

        SiteSetting siteSetting = this.iSiteSettingService.getSiteSetting();
        if (siteSetting == null) {
            log.error("平倉出錯，網站設置表不存在");
            return ServerResponse.createByErrorMsg("下單失敗，系統設置錯誤");
        }
        UserPositionFutures userPosition = this.baseMapper.selectOne(new QueryWrapper<UserPositionFutures>().eq("position_sn", positionSn));

        if (userPosition == null || userPosition.getOrderNum() == 0) {
            return ServerResponse.createByErrorMsg("平倉失敗，訂單不存在");
        }
        if (doType != 0) {
//            String am_begin = siteSetting.getTransAmBegin();
//            String am_end = siteSetting.getTransAmEnd();
//            boolean am_flag = BuyAndSellUtils.isTransTime(am_begin, am_end);
//            log.info("是否在上午交易時間 = {} ", am_flag);
//            if (!am_flag) {
//                return ServerResponse.createByErrorMsg("平倉失敗，不在交易時段內");
//            }

//            // 判斷周末不能買
//            Date today = new Date();
//            Calendar c = Calendar.getInstance();
//            c.setTime(today);
//            int weekday = c.get(Calendar.DAY_OF_WEEK);
//            if (weekday == 1) {
//                return ServerResponse.createByErrorMsg("非工作日，不可交易！");
//            }
//            if (weekday == 7) {
//                return ServerResponse.createByErrorMsg("非工作日，不可交易！");
//            }
//            String mMdd = DateUtil.format(new Date(), "MMdd");
//            if ("0909".equals(mMdd) || "0910".equals(mMdd)) {
//                return ServerResponse.createByErrorMsg("非工作日，不可交易！");
//            }
//            if ("1010".equals(mMdd)) {
//                return ServerResponse.createByErrorMsg("非工作日，不可交易！");
//            }
//
//            if ("1231".equals(mMdd)) {
//                return ServerResponse.createByErrorMsg("非工作日，不可交易！");
//            }


            List<UserPositionItemFutures> userPositionItemList = userPositionItemFuturesMapper.selectList(new QueryWrapper<UserPositionItemFutures>().eq("position_sn", positionSn));
            int sellNumberOld = 0;
            int buyNumber = 0;
            for (UserPositionItemFutures userPositionItem : userPositionItemList) {
                if (userPositionItem.getOrderType() == 0) {
                    if (DateTimeUtil.sameDate(DateTimeUtil.getCurrentDate(), userPositionItem.getOrderTime()) && !userPositionItem.getStockName().startsWith("新股")) {
                    } else {
                        buyNumber += userPositionItem.getOrderNum();
                    }
                } else {
                    sellNumberOld += userPositionItem.getOrderNum();
                }
            }
//            if (sellNumber > (buyNumber - sellNumberOld)) {
//                return ServerResponse.createByErrorMsg("當天入倉的股票T+1天才能出倉,您當前可平倉" + (buyNumber - sellNumberOld) + "股");
//            }


        }

        if (sellNumber == null || sellNumber == 0 || sellNumber > userPosition.getOrderNum()) {
            sellNumber = userPosition.getOrderNum();
        }

        User user = this.iUserService.getUserById(userPosition.getUserId());
        /*實名認證開關開啟*/

        if (/*siteProduct.getRealNameDisplay() && */user.getIsLock() == 1) {
            return ServerResponse.createByErrorMsg("平倉失敗，用戶已被鎖定");
        }


        if (userPosition.getSellOrderId() != null) {
            return ServerResponse.createByErrorMsg("平倉失敗，此訂單已平倉");
        }

        if (1 == userPosition.getIsLock().intValue()) {
            return ServerResponse.createByErrorMsg("平倉失敗 " + userPosition.getLockMsg());
        }


        List<String> stockCodeList = new ArrayList<>();
        stockCodeList.add(userPosition.getStockCode());

        FuturesVO stockListVO = null;


        stockListVO = iStockFuturesService.querySingleMarket(userPosition.getStockCode());


        BigDecimal now_price = new BigDecimal(stockListVO.getNowPrice());

        Integer buy_num = sellNumber;

        //如果跌幅大于此股票最大涨跌幅。则只跌掉最大涨跌幅
        BigDecimal force_stop_percent = siteSetting.getForceStopPercent();

        //买入成本
        BigDecimal buyCost = new BigDecimal(buy_num);

        //買入市值判斷
        BigDecimal all_buy_amt = new BigDecimal(buy_num);

        //浮動盈虧
        BigDecimal profitAndLose = getLose(userPosition.getBuyOrderPrice(), now_price, new BigDecimal(sellNumber), userPosition.getOrderLever(), userPosition.getOrderDirection());

        //賣出市值判斷
        BigDecimal all_sell_amt = all_buy_amt.add(profitAndLose);

        //当前持仓漲跌幅度
        BigDecimal plRate = profitAndLose.divide(buyCost, 6, RoundingMode.HALF_UP);
        if (plRate.compareTo(BigDecimal.ZERO) < 0 && BigDecimal.ZERO.subtract(plRate).compareTo(force_stop_percent) > 0) {
            all_sell_amt = all_buy_amt.add(profitAndLose);
        }
        BigDecimal profitLoss = new BigDecimal("0");

        log.info("買賣方向：{}", "跌");
        profitLoss = profitAndLose;
        log.info("買入總金額 = {} , 賣出總金額 = {} , 盈虧 = {}", all_buy_amt, all_sell_amt, profitLoss);

        BigDecimal user_all_amt = user.getUserFutAmt();
        BigDecimal user_enable_amt = user.getEnableFutAmt();

        log.info("用戶原本總資金 = {} , 可用 = {}", user_all_amt, user_enable_amt);

        BigDecimal buy_fee_amt = BigDecimal.valueOf(sellNumber / 1000 * 50);
        log.info("買入手續費 = {}", buy_fee_amt);


        BigDecimal sell_fee_amt = BigDecimal.valueOf(sellNumber / 1000 * 50);
        log.info("賣出手續費 = {}", sell_fee_amt);

        //總手續費= 買入手續費+賣出手續費+印花稅+遞延費+點差費
        BigDecimal all_fee_amt = buy_fee_amt.add(sell_fee_amt);


        log.info("總的手續費費用 = {}", all_fee_amt);
        BigDecimal all_profit = profitLoss.subtract(all_fee_amt);
        userPosition.setOrderNum(userPosition.getOrderNum() - sellNumber);

        userPosition.setOrderTotalPrice(userPosition.getBuyOrderPrice().multiply(new BigDecimal(userPosition.getOrderNum())));
        int updatePositionCount = this.baseMapper.updateById(userPosition);
        UserPositionItemFutures userPositionItem = new UserPositionItemFutures();
        userPositionItem.setPositionType(userPosition.getPositionType());
        userPositionItem.setPositionSn(userPosition.getPositionSn());
        userPositionItem.setUserId(userPosition.getUserId());
        userPositionItem.setNickName(userPosition.getNickName());
        userPositionItem.setAgentId(userPosition.getAgentId());
        userPositionItem.setStockGid(userPosition.getStockGid());
        userPositionItem.setBuyOrderPrice(userPosition.getBuyOrderPrice());
        userPositionItem.setStockName(userPosition.getStockName());
        userPositionItem.setStockCode(userPosition.getStockCode());
        userPositionItem.setStockSpell(userPosition.getStockSpell());
        userPositionItem.setBuyType(userPosition.getBuyType());
        userPositionItem.setStockPlate(userPosition.getStockPlate());
        userPositionItem.setOrderTime(new Date());
        userPositionItem.setSellOrderPrice(now_price);
        userPositionItem.setOrderDirection(userPosition.getOrderDirection());
        userPositionItem.setOrderNum(sellNumber);
        userPositionItem.setOrderLever(userPosition.getOrderLever());
        userPositionItem.setOrderTotalPrice(all_sell_amt);
        userPositionItem.setProfitAndLose(profitLoss);
        userPositionItem.setAllProfitAndLose(all_profit);
        userPositionItem.setOrderType(1);
        userPositionItemFuturesMapper.insert(userPositionItem);


        if (updatePositionCount > 0) {
            log.info("【用戶平倉】修改浮動盈虧記錄成功");
        } else {
            log.error("用戶平倉】修改浮動盈虧記錄出錯");
            throw new Exception("用戶平倉】修改浮動盈虧記錄出錯");
        }

        //買入消費金額
        BigDecimal orderAmount = all_buy_amt.add(profitLoss).subtract(sell_fee_amt);

        //修改用戶可用餘額=當前可用餘額+總盈虧+買入總金額+追加保證金
        user_enable_amt = user_enable_amt.add(orderAmount);
        user_all_amt = user_all_amt.add(orderAmount);
        user.setEnableFutAmt(user_all_amt);
        user.setUserFutAmt(user_enable_amt);
        int updateUserCount = this.userMapper.updateById(user);
        if (updateUserCount > 0) {
            log.info("【用戶平倉】修改用戶金額成功");
        } else {
            log.error("用戶平倉】修改用戶金額出錯");
            throw new Exception("用戶平倉】修改用戶金額出錯");
        }

        UserCashDetail ucd = new UserCashDetail();
        ucd.setPositionId(userPosition.getId());
        ucd.setAgentId(user.getAgentId());
        ucd.setAgentName(user.getAgentName());
        ucd.setUserId(user.getId());
        ucd.setUserName(user.getRealName());
        ucd.setDeType("平倉合約(" + (userPosition.getBuyType() == 0 ? "現股" : "國際金") + ")");
        ucd.setDeAmt(orderAmount);
        ucd.setDeSummary("平倉合約，" + userPosition.getStockCode() + "/" + userPosition.getStockName() + ",佔用本金：" + sellNumber + ",總手續費：" + all_fee_amt + ",盈虧：" + profitLoss + "，总盈虧：" + all_profit);
        ucd.setAddTime(new Date());
        ucd.setIsRead(0);
        int insertSxfCount = this.userCashDetailMapper.insert(ucd);
        if (insertSxfCount > 0) {
            //核算代理收入-平倉手續費
            log.info("分給代理錢" + userPositionItem.getId());
            log.info("【用戶平倉】保存明細記錄成功");
        } else {
            log.error("用戶平倉】保存明細記錄出錯");
            throw new Exception("用戶平倉】保存明細記錄出錯");
        }

        return ServerResponse.createBySuccessMsg("平倉成功！");
    }

    private UserPositionVO assembleUserPositionVO(UserPositionFutures position) {
        UserPositionVO userPositionVO = new UserPositionVO();

        userPositionVO.setId(position.getId());
        userPositionVO.setPositionType(position.getPositionType());
        userPositionVO.setPositionSn(position.getPositionSn());
        userPositionVO.setUserId(position.getUserId());
        userPositionVO.setNickName(position.getNickName());
        userPositionVO.setAgentId(position.getAgentId());
        userPositionVO.setStockName(position.getStockName());
        userPositionVO.setStockCode(position.getStockCode());
        userPositionVO.setStockGid(position.getStockGid());
        userPositionVO.setStockSpell(position.getStockSpell());
        userPositionVO.setBuyOrderId(position.getBuyOrderId());
        userPositionVO.setBuyOrderTime(position.getBuyOrderTime());
        userPositionVO.setBuyOrderPrice(position.getBuyOrderPrice());
        userPositionVO.setSellOrderId(position.getSellOrderId());
        userPositionVO.setSellOrderTime(position.getSellOrderTime());
        userPositionVO.setSellOrderPrice(position.getSellOrderPrice());
        userPositionVO.setProfitTargetPrice(position.getProfitTargetPrice());
        userPositionVO.setBuyType(position.getBuyType());
        userPositionVO.setStopTargetPrice(position.getStopTargetPrice());
        userPositionVO.setOrderDirection(position.getOrderDirection());
        userPositionVO.setOrderNum(position.getOrderNum());
        userPositionVO.setOrderLever(position.getOrderLever());
        userPositionVO.setOrderFee(position.getOrderFee());
        userPositionVO.setOrderSpread(position.getOrderSpread());
        userPositionVO.setOrderStayFee(position.getOrderStayFee());
        userPositionVO.setOrderStayDays(position.getOrderStayDays());
        userPositionVO.setMarginAdd(position.getMarginAdd());

        userPositionVO.setStockPlate(position.getStockPlate());
        userPositionVO.setSpreadRatePrice(position.getSpreadRatePrice());
        String nowPriceStr = redisTemplate.opsForValue().get(position.getStockCode());
        PositionProfitVO positionProfitVO = getPositionProfitVO(position,new BigDecimal(nowPriceStr));
        userPositionVO.setAllProfitAndLose(positionProfitVO.getAllProfitAndLose());
        userPositionVO.setNow_price(positionProfitVO.getNowPrice());
        //买入市值
        userPositionVO.setBuyTotalAmount(positionProfitVO.getBuyTotalAmount());
        //卖出市值
        userPositionVO.setSellTotalAmount(positionProfitVO.getSellTotalAmount());
        //付出成本
        userPositionVO.setOrderTotalPrice(positionProfitVO.getOrderTotalPrice());
        //浮动盈亏
        userPositionVO.setProfitAndLose(positionProfitVO.getProfitAndLose());
        userPositionVO.setOrderFee(positionProfitVO.getBuyFee().add(positionProfitVO.getSellFee()));

        return userPositionVO;
    }

    @Override
    public PositionProfitVO getPositionProfitVO(UserPositionFutures position,BigDecimal nowPrice) {

        PositionProfitVO positionProfitVO = new PositionProfitVO();
        positionProfitVO.setUserId(position.getUserId());

        //买入成本
        BigDecimal buyCost = new BigDecimal(position.getOrderNum());

        //買入市值判斷
        BigDecimal all_buy_amt = new BigDecimal(position.getOrderNum());

        //浮動盈虧
        BigDecimal profitAndLose = getLose(position.getBuyOrderPrice(), nowPrice, new BigDecimal(position.getOrderNum()), position.getOrderLever(), position.getOrderDirection());
        //賣出市值判斷
        BigDecimal all_sell_amt = all_buy_amt.add(profitAndLose);

        //卖出市值
        positionProfitVO.setSellTotalAmount(all_sell_amt);
        //买入市值
        positionProfitVO.setBuyTotalAmount(all_buy_amt);
        //盈亏= 卖出市值 - 买入市值
        positionProfitVO.setProfitAndLose(profitAndLose);

        //成本
        positionProfitVO.setOrderTotalPrice(buyCost);

        //总盈亏 = 盈亏 - 买入手续费 - 预估卖出手续费 - 预估卖出印花税
        //买入手续费
        BigDecimal buy_fee_amt = BigDecimal.valueOf(position.getOrderNum() / 1000 * 50);

        BigDecimal allProfitAndLose = positionProfitVO.getProfitAndLose().subtract(buy_fee_amt);
        positionProfitVO.setAllProfitAndLose(allProfitAndLose);
        positionProfitVO.setNowPrice(nowPrice.toString());
        positionProfitVO.setPositionSn(position.getPositionSn());
        positionProfitVO.setBuyFee(buy_fee_amt);
        positionProfitVO.setSellFee(buy_fee_amt);

        return positionProfitVO;
    }

    private BigDecimal getLose(BigDecimal buyPrice, BigDecimal sellPrice, BigDecimal orderNum, BigDecimal level, Integer orderDirection) {
        //涨跌幅
        BigDecimal rate = BigDecimal.ZERO;
        if (orderDirection == 1) {
            rate = sellPrice.divide(buyPrice, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE);
        } else {
            rate = sellPrice.divide(buyPrice, 6, RoundingMode.HALF_UP).subtract(BigDecimal.ONE);
            rate = BigDecimal.ZERO.subtract(rate);
        }
        rate = rate.multiply(level);
        return orderNum.multiply(rate);
    }

    @Override
    public List<UserPositionFutures> findPositionByStockCodeAndTimes(int minuteTimes, String stockCode, Integer userId) {
        Date paramTimes = null;
        paramTimes = DateTimeUtil.parseToDateByMinute(minuteTimes);
        return this.baseMapper.findPositionByStockCodeAndTimes(paramTimes, stockCode, userId);
    }

    @Override
    public Integer findPositionNumByTimes(int minuteTimes, Integer userId) {
        Date beginDate = DateTimeUtil.parseToDateByMinute(minuteTimes);
        Integer transNum = this.baseMapper.findPositionNumByTimes(beginDate, userId);
        log.info("用戶 {} 在 {} 分鐘之內 交易手數 {}", new Object[]{userId, Integer.valueOf(minuteTimes), transNum});
        return transNum;
    }

    @Override
    public List<Integer> findDistinctUserIdListAndRz() {
        return this.baseMapper.findDistinctUserIdListAndRz();
    }

    @Override
    public List<UserPositionFutures> findPositionByUserIdAndSellIdIsNullWhereRzAndAll() {
        return this.baseMapper.findPositionByUserIdAndSellIdIsNullWhereRzAndAll();
    }


    @Override
    public List<PositionProfitVO> getPositionProfitVOList(List<UserPositionFutures> userPositions,BigDecimal trqPrice,BigDecimal trxPrice) {
        List<PositionProfitVO> positionProfitVOList = new ArrayList();
        for (UserPositionFutures userPosition : userPositions) {
            BigDecimal nowPrice = trqPrice;
            if (userPosition.getStockCode().equals("109_NG_YZCF00X")){
                nowPrice = trxPrice;
            }
            PositionProfitVO positionProfitVO = getPositionProfitVO(userPosition, nowPrice);
            positionProfitVO.setUserPositionFutures(userPosition);
            positionProfitVOList.add(positionProfitVO);
        }
        return positionProfitVOList;
    }

    @Override
    public void sellByAll(List<PositionProfitVO> positionProfitVOList,BigDecimal trqPrice,BigDecimal trxPrice) {
        User user = userMapper.selectById(positionProfitVOList.get(0).getUserId());

        //拿出所有的PositionSn
        List<String> positionSnList = positionProfitVOList.stream().map(PositionProfitVO::getPositionSn).collect(Collectors.toList());
        //用戶強制平倉
        for (String positionSn : positionSnList) {
            log.info("强制平倉 = {}", positionSn);


            UserPositionFutures userPosition = this.baseMapper.selectOne(new QueryWrapper<UserPositionFutures>().eq("position_sn", positionSn));
            Integer sellNumber = userPosition.getOrderNum();


            List<String> stockCodeList = new ArrayList<>();
            stockCodeList.add(userPosition.getStockCode());

            BigDecimal now_price = trqPrice;
            if (userPosition.getStockCode().equals("109_NG_YZCF00X")){
                now_price = trxPrice;
            }

            //買入市值判斷
            BigDecimal all_buy_amt = new BigDecimal(sellNumber);

            //浮動盈虧
            BigDecimal profitAndLose = getLose(userPosition.getBuyOrderPrice(), now_price, new BigDecimal(sellNumber), userPosition.getOrderLever(), userPosition.getOrderDirection());

            //賣出市值判斷
            BigDecimal all_sell_amt = all_buy_amt.add(profitAndLose);

            BigDecimal profitLoss = new BigDecimal("0");
            profitLoss = profitAndLose;
            log.info("買入總金額 = {} , 賣出總金額 = {} , 盈虧 = {}", all_buy_amt, all_sell_amt, profitLoss);


            BigDecimal buy_fee_amt = BigDecimal.valueOf(sellNumber / 1000 * 50);
            log.info("買入手續費 = {}", buy_fee_amt);


            BigDecimal sell_fee_amt = BigDecimal.valueOf(sellNumber / 1000 * 50);
            log.info("賣出手續費 = {}", sell_fee_amt);

            //總手續費= 買入手續費+賣出手續費+印花稅+遞延費+點差費
            BigDecimal all_fee_amt = buy_fee_amt.add(sell_fee_amt);


            log.info("總的手續費費用 = {}", all_fee_amt);
            BigDecimal all_profit = profitLoss.subtract(all_fee_amt);
            userPosition.setOrderNum(userPosition.getOrderNum() - sellNumber);

            userPosition.setOrderTotalPrice(userPosition.getBuyOrderPrice().multiply(new BigDecimal(userPosition.getOrderNum())));
            int updatePositionCount = this.baseMapper.updateById(userPosition);
            UserPositionItemFutures userPositionItem = new UserPositionItemFutures();
            userPositionItem.setPositionType(userPosition.getPositionType());
            userPositionItem.setPositionSn(userPosition.getPositionSn());
            userPositionItem.setUserId(userPosition.getUserId());
            userPositionItem.setNickName(userPosition.getNickName());
            userPositionItem.setAgentId(userPosition.getAgentId());
            userPositionItem.setStockGid(userPosition.getStockGid());
            userPositionItem.setBuyOrderPrice(userPosition.getBuyOrderPrice());
            userPositionItem.setStockName(userPosition.getStockName());
            userPositionItem.setStockCode(userPosition.getStockCode());
            userPositionItem.setStockSpell(userPosition.getStockSpell());
            userPositionItem.setBuyType(userPosition.getBuyType());
            userPositionItem.setStockPlate(userPosition.getStockPlate());
            userPositionItem.setOrderTime(new Date());
            userPositionItem.setSellOrderPrice(now_price);
            userPositionItem.setOrderDirection(userPosition.getOrderDirection());
            userPositionItem.setOrderNum(sellNumber);
            userPositionItem.setOrderLever(userPosition.getOrderLever());
            userPositionItem.setOrderTotalPrice(all_sell_amt);
            userPositionItem.setProfitAndLose(profitLoss);
            userPositionItem.setAllProfitAndLose(all_profit);
            userPositionItem.setOrderType(1);
            userPositionItemFuturesMapper.insert(userPositionItem);


            if (updatePositionCount > 0) {
                log.info("【用戶平倉】修改浮動盈虧記錄成功");
            } else {
                log.error("用戶平倉】修改浮動盈虧記錄出錯");
                throw new RuntimeException("用戶平倉】修改浮動盈虧記錄出錯");
            }

            //買入消費金額
            BigDecimal orderAmount = all_buy_amt.add(profitLoss).subtract(sell_fee_amt);


            user.setEnableFutAmt(user.getUserFutAmt().add(orderAmount));
            user.setUserFutAmt(user.getUserFutAmt().add(orderAmount));
            //穿倉
            if (user.getUserFutAmt().compareTo(BigDecimal.ZERO) < 0) {
                user.setUserAmt(user.getUserAmt().add(user.getUserFutAmt()));
                user.setUserFutAmt(BigDecimal.ZERO);
                user.setEnableAmt(user.getEnableAmt().add(user.getEnableFutAmt()));
                user.setEnableFutAmt(BigDecimal.ZERO);
            }
            int updateUserCount = this.userMapper.updateById(user);
            if (updateUserCount > 0) {
                log.info("【用戶平倉】修改用戶金額成功");
            } else {
                log.error("用戶平倉】修改用戶金額出錯");
                throw new RuntimeException("用戶平倉】修改用戶金額出錯");
            }

            UserCashDetail ucd = new UserCashDetail();
            ucd.setPositionId(userPosition.getId());
            ucd.setAgentId(user.getAgentId());
            ucd.setAgentName(user.getAgentName());
            ucd.setUserId(user.getId());
            ucd.setUserName(user.getRealName());
            ucd.setDeType("平倉合約(" + (userPosition.getBuyType() == 0 ? "現股" : "國際金") + ")");
            ucd.setDeAmt(orderAmount);
            ucd.setDeSummary("强制平倉-平倉合約，" + userPosition.getStockCode() + "/" + userPosition.getStockName() + ",佔用本金：" + sellNumber + ",總手續費：" + all_fee_amt + ",盈虧：" + profitLoss + "，总盈虧：" + all_profit);
            ucd.setAddTime(new Date());
            ucd.setIsRead(0);
            int insertSxfCount = this.userCashDetailMapper.insert(ucd);
            if (insertSxfCount > 0) {
                log.info("【用戶平倉】保存明細記錄成功");
            } else {
                log.error("用戶平倉】保存明細記錄出錯");
                throw new RuntimeException("用戶平倉】保存明細記錄出錯");
            }
        }
    }

    @Override
    public List<UserPositionFutures> findPositionByUserIdAndSellIdIsNullWhereRz(Integer userId) {
        return this.baseMapper.findPositionByUserIdAndSellIdIsNullWhereRz(userId);

    }

}


