package com.stock.mx2.dao;
import com.stock.mx2.pojo.AgentAgencyFee;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * agent_agency_fee
 * @author gg
 * @date 2020/06/06
 */
@Mapper
@Repository
public interface AgentAgencyFeeMapper {

    /**
     * [新增]
     * @author gg
     * @date 2020/06/06
     **/
    int insert(AgentAgencyFee agentAgencyFee);

    /**
     * [刪除]
     * @author gg
     * @date 2020/06/06
     **/
    int delete(int id);

    /**
     * [更新]
     * @author gg
     * @date 2020/06/06
     **/
    int update(AgentAgencyFee agentAgencyFee);

    /**
     * [查詢] 根據主鍵 id 查詢
     * @author gg
     * @date 2020/06/06
     **/
    AgentAgencyFee load(int id);

    /**
     * [查詢] 分页查詢
     * @author gg
     * @date 2020/06/06
     **/
    List<AgentAgencyFee> pageList(int offset, int pagesize);

    /**
     * [查詢] 分页查詢 count
     * @author gg
     * @date 2020/06/06
     **/
    int pageListCount(int offset,int pagesize);

    List getAgentAgencyFeeList(Integer paramInteger);

}
