package com.stock.mx2.dao;

import com.stock.mx2.pojo.SiteNews;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 新聞資訊
 * @author lr
 * @date 2020/08/05
 */
@Mapper
@Repository
public interface SiteNewsMapper {

    /**
     * [新增]
     * @author lr
     * @date 2020/08/05
     **/
    int insert(SiteNews siteNews);

    /**
     * [刪除]
     * @author lr
     * @date 2020/08/05
     **/
    int delete(int id);

    /**
     * [更新]
     * @author lr
     * @date 2020/08/05
     **/
    int update(SiteNews siteNews);

    /**
     * [查詢] 根據主鍵 id 查詢
     * @author lr
     * @date 2020/08/05
     **/
    SiteNews load(int id);

    /**
     * [查詢] 分页查詢
     * @author lr
     * @date 2020/08/05
     **/
    List<SiteNews> pageList(@Param("pageNum") int pageNum,@Param("pageSize") int pageSize,@Param("type") Integer type,@Param("sort") String sort,@Param("keyword") String keyword);


    /**
     * [查詢] 分页查詢 count
     * @author lr
     * @date 2020/08/05
     **/
    int pageListCount(int offset,int pagesize);

    /**
     * [查詢]根據來源id查詢新闻数
     * @author lr
     * @date 2020/08/05
     **/
    int getNewsBySourceIdCount(@Param("sourceId") String sourceId);

    /**
     * [修改]修改新聞瀏覽量
     * @author lr
     * @date 2020/08/05
     **/
    int updateViews(@Param("id") Integer id);

    /**
     * [查詢] top最新新聞資訊
     * @author lr
     * @date 2020/08/05
     **/
    List<SiteNews> getTopNewsList(@Param("pageSize") int pageSize);

}
