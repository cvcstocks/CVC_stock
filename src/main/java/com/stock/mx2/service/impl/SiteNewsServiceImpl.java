package com.stock.mx2.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.SiteNewsMapper;
import com.stock.mx2.pojo.SiteNews;
import com.stock.mx2.service.ISiteNewsService;
import com.stock.mx2.utils.DateTimeUtil;
import com.stock.mx2.utils.HttpRequest;
import com.stock.mx2.utils.PropertiesUtil;
import com.stock.mx2.utils.StringUtils;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 新聞資訊
 *
 * @author lr
 * @date 2020/07/24
 */
@Service("ISiteNewsService")
public class SiteNewsServiceImpl implements ISiteNewsService {

    private static final Logger log = LoggerFactory.getLogger(SiteNewsServiceImpl.class);


    @Resource
    private SiteNewsMapper siteNewsMapper;


    @Override
    public int insert(SiteNews model) {
        int ret = 0;
        if (model == null) {
            return 0;
        }
        ret = siteNewsMapper.insert(model);
        return ret;
    }

    @Override
    public int update(SiteNews model) {
        int ret = siteNewsMapper.update(model);
        return ret > 0 ? ret : 0;
    }

    /**
     * 新聞資訊-保存
     */
    @Override
    public ServerResponse save(SiteNews model) {
        int ret = 0;
        if (model != null && model.getId() > 0) {
            ret = siteNewsMapper.update(model);
        } else {
            ret = siteNewsMapper.insert(model);
        }
        if (ret > 0) {
            return ServerResponse.createBySuccessMsg("操作成功");
        }
        return ServerResponse.createByErrorMsg("操作失敗");
    }

    /*新聞資訊-查詢列表*/
    @Override
    public ServerResponse<PageInfo> getList(int pageNum, int pageSize, Integer type, String sort, String keyword, HttpServletRequest request) {
        PageHelper.startPage(pageNum, pageSize);
        List<SiteNews> listData = this.siteNewsMapper.pageList(pageNum, pageSize, type, sort, keyword);
        PageInfo pageInfo = new PageInfo(listData);
        pageInfo.setList(listData);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /*新聞資訊-查詢詳情*/
    @Override
    public ServerResponse getDetail(int id) {
        return ServerResponse.createBySuccess(this.siteNewsMapper.load(id));
    }

    /*新聞資訊-修改新聞瀏覽量*/
    @Override
    public ServerResponse updateViews(Integer id) {
        return ServerResponse.createBySuccess(this.siteNewsMapper.updateViews(id));
    }

    /*新聞資訊-top最新新聞資訊*/
    @Override
    public ServerResponse getTopNewsList(int pageSize) {
        List<SiteNews> listData = this.siteNewsMapper.getTopNewsList(pageSize);
        PageInfo pageInfo = new PageInfo();
        pageInfo.setList(listData);
        return ServerResponse.createBySuccess(pageInfo);
    }

    @Override
    public ServerResponse getNewsById(String id) {
        String str = HttpUtil.get("https://news.cnyes.com/news/id/" + id);

        int i = str.indexOf(",\"items\":{\"newsId\":" + id);
        int i1 = str.indexOf(",\"recommended\":[{");
        str = str.substring(i, i1);

        str =    str.replaceAll(",\"items\":","");
        str = str+"}";

        System.out.println(str);

        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(str);

        return ServerResponse.createBySuccess(jsonObject);

    }

    /*新聞資訊-抓取*/
    @Override
    public int grabNews() {
        int ret = 0;
        //新聞類型：1、財經要聞，2、經濟數據，3、全球股市，4、7*24全球，5、商品資訊，6、上市公司，7、全球央行
        ret = addNews(1, PropertiesUtil.getProperty("news.main.url") + "/pc_news/FastNews/GetImportantNewsList");
        log.info("財經要聞-抓取條數：" + ret);

        ret = addNews(2, PropertiesUtil.getProperty("news.main.url") + "/pc_news/FastNews/GetInfoList?code=125&pageNumber=1&pagesize=20&condition=&r=");
        log.info("經濟數據-抓取條數：" + ret);

        ret = addNews(3, PropertiesUtil.getProperty("news.main.url") + "/pc_news/FastNews/GetInfoList?code=105&pageNumber=1&pagesize=20&condition=&r=");
        log.info("全球股市-抓取條數：" + ret);

        ret = addNews(4, PropertiesUtil.getProperty("news.main.url") + "/pc_news/FastNews/GetInfoList?code=100&pageNumber=1&pagesize=20&condition=&r=");
        log.info("7*24全球-抓取條數：" + ret);

        ret = addNews(5, PropertiesUtil.getProperty("news.main.url") + "/pc_news/FastNews/GetInfoList?code=106&pageNumber=1&pagesize=20&condition=&r=");
        log.info("商品資訊-抓取條數：" + ret);

        ret = addNews(6, PropertiesUtil.getProperty("news.main.url") + "/pc_news/FastNews/GetInfoList?code=103&pageNumber=1&pagesize=20&condition=&r=");
        log.info("上市公司-抓取條數：" + ret);

        ret = addNews(7, PropertiesUtil.getProperty("news.main.url") + "/pc_news/FastNews/GetInfoList?code=118&pageNumber=1&pagesize=20&condition=&r=");
        log.info("全球央行-抓取條數：" + ret);



        return ret;
    }

    /*
     *抓取新聞專用
     * type：新聞類型：1、財經要聞，2、經濟數據，3、全球股市，4、7*24全球，5、商品資訊，6、上市公司，7、全球央行
     * */
    private int addNews(Integer type, String url) {
        int k = 0;
        try {
            String newlist = HttpRequest.doGrabGet(url);
            JSONObject json = JSONObject.fromObject(newlist);
            if (json != null && json.getJSONArray("items") != null && json.getJSONArray("items").size() > 0) {
                for (int i = 0; i < json.getJSONArray("items").size(); i++) {
                    JSONObject model = JSONObject.fromObject(json.getJSONArray("items").getString(i));
                    String newsId = model.getString("code");
                    String imgUrl = null;
                    if (model.has("imgUrl")) {
                        imgUrl = model.getString("imgUrl");
                    }
                    //新聞不存在則添加
                    if (siteNewsMapper.getNewsBySourceIdCount(newsId) == 0) {
                        //獲取新聞詳情
                        String newdata = HttpRequest.doGrabGet(PropertiesUtil.getProperty("news.main.url") + "/PC_News/Detail/GetDetailContent?id=" + newsId + "&type=1");
                        newdata = newdata.substring(1, newdata.length() - 1).replace("\\\\\\\"", "\"");
                        newdata = newdata.replace("\\\"", "\"");
                        newdata = StringUtils.UnicodeToCN(newdata);
                        newdata = StringUtils.delHTMLTag(newdata);

                        JSONObject jsonnew = JSONObject.fromObject(newdata);
                        if (jsonnew != null && jsonnew.get("data") != null) {
                            JSONObject news = JSONObject.fromObject(jsonnew.get("data"));
                            SiteNews siteNews = new SiteNews();
                            siteNews.setSourceId(newsId);
                            siteNews.setSourceName(news.getString("source"));
                            siteNews.setTitle(news.getString("title"));
                            String showTime = news.getString("showTime");
                            siteNews.setShowTime(DateTimeUtil.strToDate(showTime));
                            siteNews.setImgurl(imgUrl);
                            siteNews.setDescription(news.getString("description"));
                            siteNews.setContent(news.getString("content"));
                            siteNews.setStatus(1);
                            siteNews.setType(type);
                            siteNewsMapper.insert(siteNews);
                            k++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return k;
    }

}
