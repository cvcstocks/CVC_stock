package com.stock.mx2.dao;

import com.stock.mx2.pojo.SiteMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  站內消息
 * @author lr 2020-07-16
 */
public interface SiteMessageMapper {
    /**
     * [新增]
     * @author lr
     * @date 2020/07/16
     **/
    int insert(SiteMessage siteMessage);

    /**
     * [刪除]
     * @author lr
     * @date 2020/07/16
     **/
    int delete(int id);

    /**
     * [更新]
     * @author lr
     * @date 2020/07/16
     **/
    int update(SiteMessage siteMessage);

    /**
     * [查詢] 根據主鍵 id 查詢
     * @author lr
     * @date 2020/07/16
     **/
    SiteMessage load(int id);


    /*查詢用戶站內消息列表*/
    List getSiteMessageByUserIdList(@Param("userId") Integer userId);

    /**
     * [今天該類型的站內消息是否推送過]
     * @author lr
     * @date 2020/07/16
     **/
    int getIsDayCount(@Param("userId") Integer userId,@Param("typeName") String typeName);

    /**
     * [用戶站內消息狀態變已讀]
     * @author lr
     * @date 2020/07/16
     **/
    int updateMessageStatus(@Param("userId") Integer userId);

    /**
     * [查詢用戶未讀消息數]
     * @author lr
     * @date 2020/07/16
     **/
    int getUnreadCount(@Param("userId") Integer userId);

}
