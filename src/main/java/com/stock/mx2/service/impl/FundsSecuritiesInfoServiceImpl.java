package com.stock.mx2.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.FundsSecuritiesInfoMapper;
import com.stock.mx2.pojo.FundsSecuritiesInfo;
import com.stock.mx2.service.IFundsSecuritiesInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 配資證券信息
 * @author lr
 * @date 2020/07/24
 */
@Service("IFundsSecuritiesInfoService")
public class FundsSecuritiesInfoServiceImpl implements IFundsSecuritiesInfoService {

    @Resource
    private FundsSecuritiesInfoMapper fundsSecuritiesInfoMapper;


    @Override
    public int insert(FundsSecuritiesInfo model) {
        int ret = 0;
        if (model == null) {
            return 0;
        }
        ret = fundsSecuritiesInfoMapper.insert(model);
        return ret;
    }

    @Override
    public int update(FundsSecuritiesInfo model) {
        int ret = fundsSecuritiesInfoMapper.update(model);
        return ret>0 ? ret: 0;
    }

    /**
     * 配資證券信息-保存
     */
    @Override
    public ServerResponse save(FundsSecuritiesInfo model) {
        int ret = 0;
        if(model!=null && model.getId()>0){
            ret = fundsSecuritiesInfoMapper.update(model);
        } else{
            ret = fundsSecuritiesInfoMapper.insert(model);
        }
        if(ret>0){
            return ServerResponse.createBySuccessMsg("操作成功");
        }
        return ServerResponse.createByErrorMsg("操作失敗");
    }

    /*配資證券信息-查詢列表*/
    @Override
    public ServerResponse<PageInfo> getList(int pageNum, int pageSize, String keyword, HttpServletRequest request){
        Page<FundsSecuritiesInfo> page = PageHelper.startPage(pageNum, pageSize);
        List<FundsSecuritiesInfo> listData = this.fundsSecuritiesInfoMapper.pageList(pageNum, pageSize, keyword);
        PageInfo pageInfo = new PageInfo(page);
        pageInfo.setList(listData);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /*配資證券信息-查詢詳情*/
    @Override
    public ServerResponse getDetail(int id) {
        return ServerResponse.createBySuccess(this.fundsSecuritiesInfoMapper.load(id));
    }

    /*配資證券信息-查詢可用的證券信息*/
    @Override
    public ServerResponse<PageInfo> getEnabledList(){
        List<FundsSecuritiesInfo> listData = this.fundsSecuritiesInfoMapper.getEnabledList();
        PageInfo pageInfo = new PageInfo();
        pageInfo.setList(listData);
        return ServerResponse.createBySuccess(pageInfo);
    }

}
