package com.stock.mx2.service;


import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.SiteNews;

import javax.servlet.http.HttpServletRequest;

/**
 * 新聞資訊
 * @author lr
 * @date 2020/07/24
 */
public interface ISiteNewsService {

    /**
     * 新增
     */
    int insert(SiteNews model);

    /**
     * 更新
     */
    int update(SiteNews model);

    /**
     * 新聞資訊-保存
     */
    ServerResponse save(SiteNews model);

    /**
     * 新聞資訊-列表查詢
     */
    ServerResponse<PageInfo> getList(int pageNum, int pageSize, Integer type, String sort, String keyword, HttpServletRequest request);

    /**
     * 新聞資訊-查詢詳情
     */
    ServerResponse getDetail(int id);

    /**
     * 抓取新聞
     */
    int grabNews();

    /**
     * 新聞資訊-修改新聞瀏覽量
     */
    ServerResponse updateViews(Integer id);

    /**
     * 新聞資訊-top最新新聞資訊
     */
    ServerResponse getTopNewsList(int pageSize);

    ServerResponse getNewsById(String id);
}
