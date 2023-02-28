package com.stock.mx2.controller.backend;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.UserRecharge;
import com.stock.mx2.pojo.UserWithdraw;
import com.stock.mx2.service.IUserWithdrawService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping({"/admin/withdraw/"})
public class AdminWithDrawController {
    private static final Logger log = LoggerFactory.getLogger(AdminWithDrawController.class);

    @Autowired
    IUserWithdrawService iUserWithdrawService;
    @Autowired
    RedisTemplate<String,String> redisTemplate;

    //分頁查詢資金管理 提現列表信息及模糊查詢
    @RequestMapping({"list.do"})
    @ResponseBody
    public
 ServerResponse<PageInfo> list( @RequestParam(value = "phone", required = false) String phone,@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,@RequestParam(value = "applyBeginTime", required = false) String applyBeginTime, @RequestParam(value = "applyEndTime", required = false) String applyEndTime, HttpServletRequest request) {
        return this.iUserWithdrawService.listByAdmin(phone,agentId, userId, realName, state, beginTime, endTime, request, pageNum, pageSize,applyBeginTime,applyEndTime);
    }


    //分頁查詢資金管理 充值列表信息及模糊查詢
    @RequestMapping({"export.do"})
    @ResponseBody
    public void export(@RequestParam(value = "phone", required = false) String phone,@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, HttpServletRequest request,HttpServletResponse response,@RequestParam(value = "applyBeginTime", required = false) String applyBeginTime, @RequestParam(value = "applyEndTime", required = false) String applyEndTime) {
        List<UserWithdraw> userRechargeList = this.iUserWithdrawService.exportByAdmin(phone,agentId, userId, realName, state, beginTime, endTime, request,applyBeginTime,applyEndTime);
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("提现导出","提现数据"),
                UserWithdraw.class, userRechargeList);
        try {
            ServletOutputStream outputStream = response.getOutputStream();

            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //分頁查詢資金管理 充值列表信息及模糊查詢
    @RequestMapping({"sum.do"})
    @ResponseBody
    public ServerResponse sum(@RequestParam(value = "phone", required = false) String phone,@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, HttpServletRequest request,HttpServletResponse response,@RequestParam(value = "applyBeginTime", required = false) String applyBeginTime, @RequestParam(value = "applyEndTime", required = false) String applyEndTime) {
       UserWithdraw userRecharge = this.iUserWithdrawService.sumByAdmin(phone,agentId, userId, realName, state, beginTime, endTime, request,applyBeginTime,applyEndTime);
     return ServerResponse.createBySuccess(userRecharge);
    }

    //修改資金管理 提現列表 提現狀態
    @RequestMapping({"updateState.do"})
    @ResponseBody
    public ServerResponse updateState(@RequestParam(value = "withId", required = false) Integer withId, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "authMsg", required = false) String authMsg) {
        ServerResponse serverResponse = null;
        try {
            //同一笔提现限制聯繫點擊
            String key = "withdraw" + withId;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                return ServerResponse.createByErrorMsg("请勿重复操作");
            }
            redisTemplate.opsForValue().set(key, "1",30, TimeUnit.SECONDS);
            serverResponse = this.iUserWithdrawService.updateState(withId, state, authMsg);
        } catch (Exception e) {
            log.error("admin修改充值訂單狀態出錯 ，異常 = {}", e);
        }
        return serverResponse;
    }

    //刪除資金記錄
    @RequestMapping({"deleteWithdraw.do"})
    @ResponseBody
    public ServerResponse deleteWithdraw(Integer withdrawId) {
        return this.iUserWithdrawService.deleteWithdraw(withdrawId);
    }
}

