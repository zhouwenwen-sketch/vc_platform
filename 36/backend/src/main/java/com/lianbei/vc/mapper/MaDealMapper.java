package com.lianbei.vc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lianbei.vc.entity.MaDeal;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface MaDealMapper extends BaseMapper<MaDeal> {

  IPage<MaDeal> selectDealPage(
      Page<MaDeal> page,
      @Param("keyword") String keyword,
      @Param("category") String category);

  @Update("UPDATE ma_deal SET view_count = view_count + 1 WHERE id = #{id}")
  void incrementViewCount(@Param("id") Long id);

  @Update("UPDATE ma_deal SET appointment_count = appointment_count + 1 WHERE id = #{id}")
  void incrementAppointmentCount(@Param("id") Long id);

  @Update("UPDATE ma_deal SET share_count = share_count + 1 WHERE id = #{id}")
  void incrementShareCount(@Param("id") Long id);
}
