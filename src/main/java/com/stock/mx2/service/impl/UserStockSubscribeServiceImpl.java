package com.stock.mx2.service.impl;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.UserMapper;
import com.stock.mx2.dao.UserStockSubscribeMapper;
import com.stock.mx2.pojo.SiteAdmin;
import com.stock.mx2.pojo.SiteMessage;
import com.stock.mx2.pojo.User;
import com.stock.mx2.pojo.UserStockSubscribe;
import com.stock.mx2.service.ISiteMessageService;
import com.stock.mx2.service.IUserStockSubscribeService;
import com.stock.mx2.utils.DateTimeUtil;
import com.stock.mx2.utils.PropertiesUtil;
import com.stock.mx2.utils.redis.CookieUtils;
import com.stock.mx2.utils.redis.JsonUtil;
import com.stock.mx2.utils.redis.RedisShardedPoolUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 新股申購
 * @author lr
 * @date 2020/07/24
 */
@Service("IUserStockSubscribeService")
public class UserStockSubscribeServiceImpl implements IUserStockSubscribeService {

    @Resource
    private UserStockSubscribeMapper userStockSubscribeMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ISiteMessageService iSiteMessageService;


    @Override
    public int insert(UserStockSubscribe model) {
        int ret = 0;
        if (model == null) {
            return 0;
        }
        ret = userStockSubscribeMapper.insert(model);
        return ret;
    }

    @Override
    public int update(UserStockSubscribe model) {
        int ret = userStockSubscribeMapper.update(model);
        return ret>0 ? ret: 0;
    }

    /**
     * 新股申購-保存
     */
    @Override
    public ServerResponse save(UserStockSubscribe model, HttpServletRequest request) {
        int ret = 0;
        if(model!=null && model.getId()>0){
            model.setEndTime(DateTimeUtil.getCurrentDate());
            ret = userStockSubscribeMapper.update(model);
            UserStockSubscribe userStockSubscribe = userStockSubscribeMapper.load(model.getId());
            if(model.getSubmitAmount() != null && model.getSubmitAmount().intValue() >0){
                //客服修改提交金額
            } else {
                if(ret>0 && model.getStatus() == 3){
                    //給達到消息強平提醒用戶推送消息
                    SiteMessage siteMessage = new SiteMessage();
                    siteMessage.setUserId(userStockSubscribe.getUserId());
                    siteMessage.setUserName(userStockSubscribe.getRealName());
                    siteMessage.setTypeName("新股申購");
                    siteMessage.setStatus(1);
                    siteMessage.setContent("【新股申購中籤】恭喜您，新股申購中籤成功，申購金額："+ userStockSubscribe.getSubmitAmount() +"，請及時關注哦。");
                    siteMessage.setAddTime(DateTimeUtil.getCurrentDate());
                    iSiteMessageService.insert(siteMessage);
                }
                if(ret>0 && model.getStatus() == 4){
                    //給達到消息強平提醒用戶推送消息
                    SiteMessage siteMessage = new SiteMessage();
                    siteMessage.setUserId(userStockSubscribe.getUserId());
                    siteMessage.setUserName(userStockSubscribe.getRealName());
                    siteMessage.setTypeName("新股申購");
                    siteMessage.setStatus(1);
                    siteMessage.setContent("【新股申購未中籤】很遺憾，您的新股申購本次未簽，申購金額："+ userStockSubscribe.getSubmitAmount() +"。");
                    siteMessage.setAddTime(DateTimeUtil.getCurrentDate());
                    iSiteMessageService.insert(siteMessage);
                }
            }

        } else{
            if(model.getUserId() != null){
                User user = userMapper.selectByPrimaryKey(model.getUserId());
                model.setRealName(user.getRealName());
                model.setPhone(user.getPhone());
            }
            SiteAdmin siteAdmin = null;
            String loginToken = request.getHeader(PropertiesUtil.getProperty("admin.cookie.name"));
            if (StringUtils.isNotEmpty(loginToken)) {
                String adminJsonStr = RedisShardedPoolUtils.get(loginToken);
                siteAdmin = (SiteAdmin) JsonUtil.string2Obj(adminJsonStr, SiteAdmin.class);
            }
            model.setAdminId(siteAdmin==null?1:siteAdmin.getId());
            model.setAdminName(siteAdmin.getAdminName());
            model.setAddTime(DateTimeUtil.getCurrentDate());
            model.setStatus(1);
            ret = userStockSubscribeMapper.insert(model);
        }
        if(ret>0){
            return ServerResponse.createBySuccessMsg("操作成功");
        }
        return ServerResponse.createByErrorMsg("操作失敗");
    }

    /**
     * 發送站內信
     */
    @Override
    public ServerResponse sendMsg(UserStockSubscribe model, HttpServletRequest request) {
        int ret = 0;

        if(model!=null){
            //所有人發站內信
            if(model.getUserId() == 0){
                List<User> users = this.userMapper.listByAdmin(null,null,null, null, null, null,null);
                for(int k=0;k<users.size();k++){
                    User user = users.get(k);
                    SiteMessage siteMessage = new SiteMessage();
                    siteMessage.setUserId(user.getId());
                    siteMessage.setUserName(user.getRealName());
                    siteMessage.setTypeName("站內消息");
                    siteMessage.setStatus(1);
                    siteMessage.setContent("【站內消息】"+ model.getRemarks() );
                    siteMessage.setAddTime(DateTimeUtil.getCurrentDate());
                    ret = iSiteMessageService.insert(siteMessage);
                }
            } else {
                //指定用戶發站內信
                User user = userMapper.selectByPrimaryKey(model.getUserId());
                SiteMessage siteMessage = new SiteMessage();
                siteMessage.setUserId(user.getId());
                siteMessage.setUserName(user.getRealName());
                siteMessage.setTypeName("站內消息");
                siteMessage.setStatus(1);
                siteMessage.setContent("【站內消息】"+ model.getRemarks() );
                siteMessage.setAddTime(DateTimeUtil.getCurrentDate());
                ret = iSiteMessageService.insert(siteMessage);
            }
        }
        if(ret>0){
            return ServerResponse.createBySuccessMsg("操作成功");
        }
        return ServerResponse.createByErrorMsg("操作失敗");
    }


    /*新股申購-查詢列表*/
    @Override
    public ServerResponse<PageInfo> getList(int pageNum, int pageSize, String keyword, HttpServletRequest request){
        PageHelper.startPage(pageNum, pageSize);
        List<UserStockSubscribe> listData = this.userStockSubscribeMapper.pageList(pageNum, pageSize, keyword);
        PageInfo pageInfo = new PageInfo(listData);
        pageInfo.setList(listData);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /*新股申購-查詢詳情*/
    @Override
    public ServerResponse getDetail(int id) {
        return ServerResponse.createBySuccess(this.userStockSubscribeMapper.load(id));
    }

    /*新股申購-查询用户最新新股申購数据*/
    @Override
    public ServerResponse getOneSubscribeByUserId(Integer userId, HttpServletRequest request) {
        return ServerResponse.createBySuccess(this.userStockSubscribeMapper.getOneSubscribeByUserId(userId));
    }

    /**
     * 新股申購-用戶提交金額
     */
    @Override
    public ServerResponse userSubmit(UserStockSubscribe model, HttpServletRequest request) {
        int ret = 0;
        if(model!=null && model.getId()>0){
            UserStockSubscribe userStockSubscribe = userStockSubscribeMapper.load(model.getId());
            if(userStockSubscribe != null){
                model.setSubmitTime(DateTimeUtil.getCurrentDate());
                model.setStatus(2);
                ret = userStockSubscribeMapper.update(model);
            } else {
                return ServerResponse.createByErrorMsg("新股申購訂單不存在！");
            }

        }
        if(ret>0){
            return ServerResponse.createBySuccessMsg("操作成功");
        }
        return ServerResponse.createByErrorMsg("操作失敗");
    }

}
