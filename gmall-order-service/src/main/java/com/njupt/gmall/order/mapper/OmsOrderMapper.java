package com.njupt.gmall.order.mapper;

import com.njupt.gmall.bean.OmsOrder;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface OmsOrderMapper extends Mapper<OmsOrder> {
    List<OmsOrder> getMyOrderListByMemberId(@Param("memberId") String memberId);


}
