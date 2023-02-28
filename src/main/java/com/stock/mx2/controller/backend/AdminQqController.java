package com.stock.mx2.controller.backend;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.QqMapper;
import com.stock.mx2.pojo.Qq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Random;

@Controller
@RequestMapping({"/admin/qq/"})
public class AdminQqController {

    @Autowired
    private QqMapper qqMapper;


    //查詢系統基本設置 所有公告設置信息
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list() {
        return ServerResponse.createBySuccess(qqMapper.selectList(new QueryWrapper<Qq>().orderByDesc("id")));
    }

    //添加系統基本設置 公共信息
    @RequestMapping({"add.do"})
    @ResponseBody
    public ServerResponse add(Qq qq) {
        if (qq.getId()!=null){
            this.qqMapper.updateById(qq);
        }else{
            this.qqMapper.insert(qq);
        }

        return ServerResponse.createBySuccess();
    }
    //添加系統基本設置 公共信息
    @RequestMapping({"gender.do"})
    @ResponseBody
    public ServerResponse gender() {

        for (int i = 0; i < 10; i++) {
            Qq qq1 = new Qq();

            qq1.setQq(getRandomNickname(10));
            qq1.setName("客服");
            qq1.setStart(5);

            this.qqMapper.insert(qq1);
        }
        return ServerResponse.createBySuccess();
    }

    //生成10位随机数字

    public static String getRandomNickname(int length) {
        String val = "";
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            val += String.valueOf(random.nextInt(10));
        }
        return val;
    }





    //添加系統基本設置 公共信息
    @RequestMapping({"remove.do"})
    @ResponseBody
    public ServerResponse add(Long id) {
        return ServerResponse.createBySuccess(this.qqMapper.deleteById(id));

    }
}
