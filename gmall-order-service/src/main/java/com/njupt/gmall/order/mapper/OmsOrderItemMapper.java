package com.njupt.gmall.order.mapper;

import com.njupt.gmall.bean.OmsOrderItem;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.ArrayList;

public interface OmsOrderItemMapper extends Mapper<OmsOrderItem> {
    ArrayList<OmsOrderItem> getMyOrderItemListByOrderSn(@Param("orderSn") String orderSn);

}
