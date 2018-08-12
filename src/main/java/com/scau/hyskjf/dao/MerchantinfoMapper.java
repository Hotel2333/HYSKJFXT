package com.scau.hyskjf.dao;

import com.scau.hyskjf.pojo.Merchantinfo;
import com.scau.hyskjf.pojo.MerchantinfoWithBLOBs;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantinfoMapper {
    int deleteByPrimaryKey(Integer merid);

    int insert(MerchantinfoWithBLOBs record);

    int insertSelective(MerchantinfoWithBLOBs record);

    MerchantinfoWithBLOBs selectByPrimaryKey(Integer merid);

    Merchantinfo findById(@Param("id") Integer merid);

    int updateByPrimaryKeySelective(MerchantinfoWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(MerchantinfoWithBLOBs record);

    int updateByPrimaryKey(Merchantinfo record);
}