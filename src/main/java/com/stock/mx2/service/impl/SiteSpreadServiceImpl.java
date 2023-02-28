package com.stock.mx2.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.SiteSpreadMapper;
import com.stock.mx2.pojo.AgentAgencyFee;
import com.stock.mx2.pojo.AgentUser;
import com.stock.mx2.pojo.SiteSpread;
import com.stock.mx2.service.ISiteSpreadService;
import com.stock.mx2.vo.agent.AgentAgencyFeeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * site_spread
 * @author lr
 * @date 2020/07/01
 */
@Service("ISiteSpreadService")
public class SiteSpreadServiceImpl implements ISiteSpreadService {

    @Autowired
    private SiteSpreadMapper siteSpreadMapper;


    @Override
    public int insert(SiteSpread siteSpread) {
        int ret = 0;
        // valid
        if (siteSpread == null) {
            return 0;
        }

        ret = siteSpreadMapper.insert(siteSpread);
        return ret;

    }


    @Override
    public int delete(int id) {
        int ret = 0;
        // valid
        if (id == 0) {
            return 0;
        }

        ret = siteSpreadMapper.delete(id);
        return ret;
    }


    @Override
    public int update(SiteSpread siteSpread) {
        int ret = 0;
        // valid
        if (siteSpread == null) {
            return 0;
        }

        ret = siteSpreadMapper.update(siteSpread);
        return ret;
    }


    @Override
    public SiteSpread load(int id) {
        return siteSpreadMapper.load(id);
    }


    @Override
    public ServerResponse<PageInfo> pageList(int pageNum, int pageSize, String typeName) {
        Page<AgentAgencyFeeVO> page = PageHelper.startPage(pageNum, pageSize);
        List<SiteSpread> list = this.siteSpreadMapper.pageList(pageNum, pageSize, typeName);
        PageInfo pageInfo = new PageInfo(page);
        pageInfo.setList(list);
        return ServerResponse.createBySuccess(pageInfo);

    }

    /**
     * 查詢點差費率
     * @author lr
     * @date 2020/07/01
     * applies：漲跌幅
     * turnover：成交額
     * code:股票代碼
     * unitprice：股票單價
     **/
    @Override
    public SiteSpread findSpreadRateOne(BigDecimal applies, BigDecimal turnover, String code, BigDecimal unitprice) {
        return siteSpreadMapper.findSpreadRateOne(applies,turnover,code,unitprice);
    }

}
