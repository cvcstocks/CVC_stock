package com.stock.mx2.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.QqMapper;
import com.stock.mx2.pojo.Qq;
import com.stock.mx2.service.ISiteArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping({"/api/qq/"})
public class QQController {
    private static final Logger log = LoggerFactory.getLogger(QQController.class);

    @Autowired
    QqMapper qqMapper;

    //查詢企業公告信息
    @RequestMapping({"have.do"})
    @ResponseBody
    public ServerResponse have(@RequestParam(value = "qq", required = false) String qq) {
        List<Qq> qqList = qqMapper.selectList(new QueryWrapper<Qq>().eq("qq", qq));
        if (qqList.size()>0){

            return ServerResponse.createBySuccess(qqList.get(0));
        }else{
            return ServerResponse.createByError();

        }
    }


}
