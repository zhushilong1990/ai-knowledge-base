package com.aikb.mapper;

import com.aikb.entity.Document;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

    @Select("SELECT COUNT(*) FROM documents WHERE knowledge_base_id = #{kbId}")
    int countByKbId(@Param("kbId") Long kbId);
}
