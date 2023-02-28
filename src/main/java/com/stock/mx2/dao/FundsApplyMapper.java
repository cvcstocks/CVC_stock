package com.stock.mx2.dao;

import com.stock.mx2.pojo.FundsApply;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * funds_apply
 * @author lr
 * @date 2020/07/25
 */
@Mapper
@Repository
public interface FundsApplyMapper {

    /**
     * [新增]
     * @author lr
     * @date 2020/07/25
     **/
    int insert(FundsApply fundsApply);

    /**
     * [刪除]
     * @author lr
     * @date 2020/07/25
     **/
    int delete(int id);

    /**
     * [更新]
     * @author lr
     * @date 2020/07/25
     **/
    int update(FundsApply fundsApply);

    /**
     * [查詢] 根據主鍵 id 查詢
     * @author lr
     * @date 2020/07/25
     **/
    FundsApply load(int id);

    /**
     * [查詢] 分页查詢
     * @author lr
     * @date 2020/07/24
     **/
    List<FundsApply> pageList(@Param("pageNum") int pageNum,@Param("pageSize") int pageSize,@Param("keyword") String keyword,@Param("status") Integer status);


    /**
     * [查詢] 分页查詢 count
     * @author lr
     * @date 2020/07/25
     **/
    int pageListCount(int offset,int pagesize);

    /**
     * [查詢] 用戶配資列表
     * @author lr
     * @date 2020/07/27
     **/
    List<FundsApply> getUserApplyList(@Param("pageNum") int pageNum,@Param("pageSize") int pageSize,@Param("userId") int userId);

    /**
     * [查詢] 配資申請-用戶操盤中子帳戶
     * @author lr
     * @date 2020/07/27
     **/
    List<FundsApply> getUserEnabledSubaccount(@Param("userId") int userId);

    /**
     * [查詢] 配資申請-查詢用户分仓保证金
     * @author lr
     * @date 2020/07/31
     **/
    List<FundsApply> getUserMarginList(@Param("userId") int userId);

}
