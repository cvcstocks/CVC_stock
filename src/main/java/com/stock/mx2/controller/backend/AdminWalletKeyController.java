package com.stock.mx2.controller.backend;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.UserWalletKeyMapper;
import com.stock.mx2.pojo.UserWalletKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Controller
@RequestMapping({"/admin/wallet/key/"})
public class AdminWalletKeyController  {

    @Autowired
    private UserWalletKeyMapper userWalletKeyMapper;


    //分頁查詢資金管理 充值列表信息及模糊查詢
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "level", required = false) String level,@RequestParam(value = "useStatus", required = false) String useStatus,@RequestParam(value = "key", required = false) String key,@RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "realName", required = false) String realName,@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,  HttpServletRequest request) {
        QueryWrapper<UserWalletKey> queryWrapper = new QueryWrapper<>();
        if (phone != null && !phone.equals("")) {
            queryWrapper.like("user_phone", phone);
        }

        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }

        if (realName != null && !realName.equals("")) {
            queryWrapper.like("user_name", realName);
        }

        if (useStatus != null&& !useStatus.equals("")) {
            queryWrapper.eq("use_status", useStatus);
        }

        if (key != null&& !key.equals("")) {
            queryWrapper.like("wallet_key", key);
        }


        if (level != null&& !level.equals("")) {
            queryWrapper.eq("level", level);
        }

        queryWrapper.orderByDesc("id");

        PageHelper.startPage(pageNum, pageSize);
        PageInfo pageInfo = new PageInfo(this.userWalletKeyMapper.selectList(queryWrapper));
        return ServerResponse.createBySuccess(pageInfo);
    }

    //生成key
    @RequestMapping({"add.do"})
    @ResponseBody
    public ServerResponse add(Integer level) {
        UserWalletKey userWalletKey = new UserWalletKey();
        userWalletKey.setWalletKey( UUID.randomUUID().toString().split("-")[UUID.randomUUID().toString().split("-").length - 1]);
        userWalletKey.setLevel(level);
        userWalletKey.setUseStatus(0);
        userWalletKeyMapper.insert(userWalletKey);
        return ServerResponse.createBySuccess();
    }


    //生成key
    @RequestMapping({"remove.do"})
    @ResponseBody
    public ServerResponse remove(Integer id) {
        userWalletKeyMapper.deleteById(id);
        return ServerResponse.createBySuccess();
    }

}
