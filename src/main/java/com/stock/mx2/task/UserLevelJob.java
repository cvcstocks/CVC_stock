package com.stock.mx2.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.stock.mx2.dao.UserMapper;
import com.stock.mx2.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Component
public class UserLevelJob {

    @Autowired
    private UserMapper userMapper;

    /**
     *      * 短信种⼦⾸充套餐：
     *      * 1、【温馨提示】您的xxxx账户有效盈利⾦额xxxx即将过期，
     *      * 请尽快内联系客服激活账户提领。
     *      * 2、【温馨提示】您的xxxx账户有效盈利⾦额xxxx即将过期，
     *      * 请72⼩时内联系客服激活账户提领。
     *      * 3、【温馨提示】您的xxxx账户有效盈利⾦额xxxx即将过期，
     *      * 请48⼩时内联系客服激活账户提领。
     *      * 4、【重要提醒】您的xxxx账户有效盈利⾦额xxxx即将过期，
     *      * 请24⼩时内联系客服激活账户提领，超时未联系客服，您将
     *      * 要损失xxxx元喔！
     *      * 5、【重要提醒】您的xxxx账户有效盈利⾦额xxxx即将过期，
     *      * 请12⼩时内联系客服激活账户提领，超时未联系客服，您将
     *      * 要损失xxxx元喔！
     *      * 6、【重要提醒】您的xxxx账户有效盈利⾦额xxxx即将过期，
     *      * 请2⼩时内联系客服激活账户提领，超时未联系客服，您将要
     *      * 损失xxxx元喔！
     *      * 7、【重要提醒】您的xxxx账户有效盈利⾦额xxxx即将过期，
     *      * 请1⼩时内联系客服激活账户提领，超时未联系客服，您将要
     *      * 损失xxxx元喔！
     *      * +1天【所有的盈利失效清零】
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void userLevelJob() {
        //只有过度期需要发送短信
        List<User> userList = userMapper.selectList(new QueryWrapper<User>().eq("group_level", 2));
        for (User user : userList) {
            Date laseLevelTime = user.getLaseLevelTime();
            Date now = new Date();
            long l = now.getTime() - laseLevelTime.getTime();


            //如果未发送过短信
            if (user.getSmsTimeType() == 0) {
                //发送短信
                //发送成功后修改发送状态
                System.out.println("发送短信" +"【温馨提示】您的"+user.getPhone()+"账户有效盈利⾦额"+user.getProfitWallet()+"即将过期， 请72⼩时内联系客服激活账户提领");
                user.setSmsTimeType(2);
            } else if (user.getSmsTimeType() == 2 && l > 1000 * 60 * 60 * 24) {
                //时间已经过去一天
                //发送短信
                //发送成功后修改发送状态
                System.out.println("发送短信" +"【温馨提示】您的"+user.getPhone()+"账户有效盈利⾦额"+user.getProfitWallet()+"即将过期， 请48⼩时内联系客服激活账户提领");
                user.setSmsTimeType(3);
            }else if (user.getSmsTimeType() == 3 && l > 1000 * 60 * 60 * 48) {
                //时间已经过去2天
                //发送短信
                //发送成功后修改发送状态
                System.out.println("发送短信" +"【温馨提示】您的"+user.getPhone()+"账户有效盈利⾦额"+user.getProfitWallet()+"即将过期， 请24⼩时内联系客服激活账户提领");
                user.setSmsTimeType(4);
            }
            else if (user.getSmsTimeType() == 4 && l > 1000 * 60 * 60 * 60) {
                //时间已经过去2天
                //发送短信
                //发送成功后修改发送状态
                System.out.println("发送短信" +"【温馨提示】您的"+user.getPhone()+"账户有效盈利⾦额"+user.getProfitWallet()+"即将过期， 请12⼩时内联系客服激活账户提领");
                user.setSmsTimeType(5);
            }else if (user.getSmsTimeType() == 5 && l > 1000 * 60 * 60 * 70) {
                //时间已经过去2天
                //发送短信
                //发送成功后修改发送状态
                System.out.println("发送短信" +"【温馨提示】您的"+user.getPhone()+"账户有效盈利⾦额"+user.getProfitWallet()+"即将过期， 请2⼩时内联系客服激活账户提领");
                user.setSmsTimeType(6);
            }else if (user.getSmsTimeType() == 6 && l > 1000 * 60 * 60 * 71) {
                //时间已经过去2天
                //发送短信
                //发送成功后修改发送状态
                System.out.println("发送短信" +"【温馨提示】您的"+user.getPhone()+"账户有效盈利⾦额"+user.getProfitWallet()+"即将过期， 请1⼩时内联系客服激活账户提领");
                user.setSmsTimeType(7);
            }
            else if (user.getSmsTimeType() == 7 && l > 1000 * 60 * 60 * 96) {
                //时间已经过去2天
                //发送短信
                //发送成功后修改发送状态
                user.setSmsTimeType(8);
                user.setProfitWallet(BigDecimal.ZERO);
                user.setWithProfitWallet(BigDecimal.ZERO);
            }
            userMapper.updateById(user);
        }



    }

    public static void main(String[] args) {
        System.out.println(1000 * 60 * 60 * 60);
    }
}
