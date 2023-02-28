package com.stock.mx2.service;

import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.SiteMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * 站內消息
 * @author lr
 * @date 2020/07/16
 */
public interface ISiteMessageService {
    /**
     * 新增
     */
    int insert(SiteMessage siteMessage);

    /**
     * 刪除
     */
    int delete(int id);

    /**
     * 更新
     */
    int update(SiteMessage siteMessage);


    /*查詢用戶站內消息列表*/
    ServerResponse<PageInfo> getSiteMessageByUserIdList(int pageNum, int pageSize, int userId, HttpServletRequest request);

    /**
     * 今天該類型的站內消息是否推送過
     */
    int getIsDayCount(Integer userId, String typeName);

    /**
     * [用戶站內消息狀態變已讀]
     * @author lr
     * @date 2020/07/16
     **/
    int updateMessageStatus(HttpServletRequest request);

    /**
     * [查詢用戶未讀消息數]
     * @author lr
     * @date 2020/07/16
     **/
    int getUnreadCount(HttpServletRequest request);

    ServerResponse del(Integer id, HttpServletRequest request);

}
