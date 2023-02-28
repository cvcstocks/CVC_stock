package com.stock.mx2.dao;


import com.stock.mx2.pojo.SiteArticle;
import java.util.List;

import com.stock.mx2.pojo.SiteNews;
import org.apache.ibatis.annotations.Param;

public interface SiteArticleMapper {
    int deleteByPrimaryKey(Integer paramInteger);

    int insert(SiteArticle paramSiteArticle);

    int insertSelective(SiteArticle paramSiteArticle);

    SiteArticle selectByPrimaryKey(Integer paramInteger);

    int updateByPrimaryKeySelective(SiteArticle paramSiteArticle);

    int updateByPrimaryKey(SiteArticle paramSiteArticle);

    List listByAdmin(@Param("artTitle") String paramString1, @Param("artType") String paramString2);
    List list(@Param("artTitle") String paramString1, @Param("artType") String paramString2 ,@Param("topId") Integer topId);

    /**
     * [查詢] top最新公告
     * @author lr
     * @date 2020/08/05
     **/
    List<SiteNews> getTopArtList(@Param("pageSize") int pageSize,@Param("topId") Integer topId);

    /**
     * [查詢]根據來源id查詢公告数
     * @author lr
     * @date 2020/08/05
     **/
    int getArtBySourceIdCount(@Param("sourceId") String sourceId);

}
