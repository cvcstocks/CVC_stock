package com.stock.mx2.dao;

import com.stock.mx2.pojo.FundsTradingAccount;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 配資交易帳戶
 * @author lr
 * @date 2020/07/24
 */
@Mapper
@Repository
public interface FundsTradingAccountMapper {

    /**
     * [新增]
     * @author lr
     * @date 2020/07/24
     **/
    int insert(FundsTradingAccount fundsTradingAccount);

    /**
     * [刪除]
     * @author lr
     * @date 2020/07/24
     **/
    int delete(int id);

    /**
     * [更新]
     * @author lr
     * @date 2020/07/24
     **/
    int update(FundsTradingAccount fundsTradingAccount);

    /**
     * [查詢] 根據主鍵 id 查詢
     * @author lr
     * @date 2020/07/24
     **/
    FundsTradingAccount load(int id);

    /**
     * [查詢] 分页查詢 count
     * @author lr
     * @date 2020/07/24
     **/
    int pageListCount(int offset,int pagesize);

    /**
     * [查詢] 分页查詢
     * @author lr
     * @date 2020/07/24
     **/
    List<FundsTradingAccount> pageList(@Param("pageNum") int pageNum,@Param("pageSize") int pageSize,@Param("keyword") String keyword,@Param("status") Integer status);

    /**
     * [查詢最新交易帳戶編號]
     * @author lr
     * @date 2020/07/24
     **/
    int getMaxNumber();

    /**
     * [查詢] 根据子账户编号查詢详细信息
     * @author lr
     * @date 2020/07/24
     **/
    FundsTradingAccount getAccountByNumber(@Param("subaccountNumber") Integer subaccountNumber);

}
