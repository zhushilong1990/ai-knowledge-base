package com.aikb.repository;

import com.aikb.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface UserRepository extends BaseMapper<User> {

    Optional<User> findByEmail(String email);
}
