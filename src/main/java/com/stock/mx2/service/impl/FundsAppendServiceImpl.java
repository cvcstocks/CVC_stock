package com.stock.mx2.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.FundsAppendMapper;
import com.stock.mx2.dao.FundsApplyMapper;
import com.stock.mx2.dao.FundsLeverMapper;
import com.stock.mx2.pojo.FundsAppend;
import com.stock.mx2.pojo.FundsApply;
import com.stock.mx2.pojo.FundsLever;
import com.stock.mx2.pojo.User;
import com.stock.mx2.service.IFundsAppendService;
import com.stock.mx2.service.IFundsApplyService;
import com.stock.mx2.service.IUserService;
import com.stock.mx2.utils.DateTimeUtil;
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
 * 配資追加申請
 * @author lr
 * @date 2020/07/24
 */
@Service("IFundsAppendService")
public class FundsAppendServiceImpl implements IFundsAppendService {

    @Resource
    private FundsAppendMapper fundsAppendMapper;

    @Autowired
    IUserService iUserService;

    @Autowired
    FundsApplyMapper fundsApplyMapper;

    @Autowired
    FundsLeverMapper fundsLeverMapper;


    @Override
    public int insert(FundsAppend model) {
        int ret = 0;
        if (model == null) {
            return 0;
        }
        ret = fundsAppendMapper.insert(model);
        return ret;
    }

    @Override
    public int update(FundsAppend model) {
        int ret = fundsAppendMapper.update(model);
        return ret>0 ? ret: 0;
    }

    /**
     * 配資追加申請-保存
     */
    @Transactional
    public ServerResponse save(FundsAppend model, HttpServletRequest request) {
        int ret = 0;
        if(model.getApplyId() == null || model.getApplyId() == 0){
            return ServerResponse.createByErrorMsg("操作異常，請稍後再試");
        }
        FundsApply fundsApply = fundsApplyMapper.load(model.getApplyId());
        if(fundsApply == null){
            return ServerResponse.createByErrorMsg("子帳戶不存在，請稍後再試");
        }
        User user = this.iUserService.getCurrentRefreshUser(request);
        if(user == null){
            return ServerResponse.createBySuccessMsg("請登錄");
        }
        //修改+審核
        if(model!=null && model.getId()>0){
            FundsAppend fundsAppend = fundsAppendMapper.load(model.getId());
            model.setAuditTime(DateTimeUtil.getCurrentDate());
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//設置日期格式
            // 追加類型：1、擴大配資，2、追加保證金，3、續期，4、終止操盤，5、提前盈利
            if(fundsAppend.getAppendType() == 1){

            } else if(fundsAppend.getAppendType() == 3){
                String endtime = df.format(fundsApply.getEndTime());
                Date endDate = DateTimeUtil.strToDate(endtime);
                endDate = DateTimeUtil.addDay(endDate, fundsAppend.getAppendCycle());
                model.setEndTime(endDate);
                model.setTradersCycle(fundsAppend.getTradersCycle() + fundsAppend.getAppendCycle());
                //修改子帳戶結束時間
                fundsApply.setEndTime(endDate);
                fundsApplyMapper.update(fundsApply);
                //修改用戶餘額，待完善

            } else if(fundsAppend.getAppendType() == 4){
                //修改子帳戶狀態
                if(model.getStatus() == 1){
                    fundsApply.setStatus(4);
                    fundsApplyMapper.update(fundsApply);
                }
                //修改用戶餘額，待完善

            }
            ret = fundsAppendMapper.update(model);
        } else{// 提交申請
            model.setUserId(user.getId());
            model.setUserName(user.getRealName());
            model.setUserPhone(user.getPhone());
            model.setMargin(fundsApply.getMargin());
            model.setFundsAmount(fundsApply.getFundsAmount());
            model.setLever(fundsApply.getLever());
            model.setManageFee(fundsApply.getManageFee());
            model.setTotalTradingAmount(fundsApply.getTotalTradingAmount());
            model.setLineWarning(fundsApply.getLineWarning());
            model.setLineUnwind(fundsApply.getLineUnwind());
            model.setRatioWarning(fundsApply.getRatioWarning());
            model.setRatioUnwind(fundsApply.getRatioUnwind());
            model.setEndTime(fundsApply.getEndTime());

            // 追加類型：1、擴大配資，2、追加保證金，3、續期，4、終止操盤，5、提前盈利
            if(model.getAppendType() == 1){
                model.setTradersCycle(fundsApply.getTradersCycle());
                FundsLever fundsLever = fundsLeverMapper.getLeverRateInfo(1, fundsApply.getLever());
                BigDecimal appendServiceFee = model.getAppendMargin().multiply(fundsLever.getManageRate());
                model.setAppendServiceFee(appendServiceFee);
                model.setPayAmount(model.getAppendMargin());
            } else if(model.getAppendType() == 2) {
                model.setTradersCycle(fundsApply.getTradersCycle());
                model.setPayAmount(model.getAppendMargin());
            } else if(model.getAppendType() == 3){
                model.setTradersCycle(model.getAppendCycle());
                model.setPayAmount(model.getAppendServiceFee());
            } else if(model.getAppendType() == 4){
                int isAppendEnd = fundsAppendMapper.isAppendEnd(model.getApplyId());
                if(isAppendEnd>0){
                    return ServerResponse.createByErrorMsg("申請已提交，無須重複操作！");
                }
                model.setTradersCycle(model.getAppendCycle());
                model.setPayAmount(model.getAppendServiceFee());
            }
            ret = fundsAppendMapper.insert(model);
        }
        if(ret>0){
            return ServerResponse.createBySuccessMsg("操作成功");
        }
        return ServerResponse.createByErrorMsg("操作失敗");
    }

    /*配資追加申請-查詢列表*/
    @Override
    public ServerResponse<PageInfo> getList(int pageNum, int pageSize, String keyword, Integer status, Integer userId, Integer appendType, HttpServletRequest request){
        PageHelper.startPage(pageNum, pageSize);
        List<FundsAppend> listData = this.fundsAppendMapper.pageList(pageNum, pageSize, keyword, status, userId, appendType);
        PageInfo pageInfo = new PageInfo(listData);
        pageInfo.setList(listData);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /*配資追加申請-查詢詳情*/
    @Override
    public ServerResponse getDetail(int id) {
        return ServerResponse.createBySuccess(this.fundsAppendMapper.load(id));
    }

}
