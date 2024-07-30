package com.lelexuetang.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lelexuetang.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    @Select("select * from media_process mp " +
            "where mp.id % #{shardTotal} = #{shardIndex} " +
            "and (mp.status = 1 or mp.status = 3) " +
            "and mp.fail_count < 3 " +
            "limit #{count}")
    List<MediaProcess> selectListByShardIndex(@Param("shardIndex") int shardIndex, @Param("shardTotal") int shardTotal, @Param("count") int count);

    @Update("update media_process set status = 4 " +
            "where id = #{id} " +
            "and (status = 1 or status = 3) " +
            "and fail_count < 3")
    int startTask(@Param("id") Long id);
}
