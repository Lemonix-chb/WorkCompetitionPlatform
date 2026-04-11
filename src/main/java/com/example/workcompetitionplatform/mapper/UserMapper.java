package com.example.workcompetitionplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.workcompetitionplatform.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 *
 * @author 陈海波
 * @since 2026-01-19
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名（学号/工号）
     * @return 用户实体
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱地址
     * @return 用户实体
     */
    User selectByEmail(@Param("email") String email);

    /**
     * 根据学号查询用户
     *
     * @param studentNo 学号
     * @return 用户实体
     */
    User selectByStudentNo(@Param("studentNo") String studentNo);

    /**
     * 根据工号查询用户（教师工号）
     *
     * @param teacherNo 工号
     * @return 用户实体
     */
    User selectByTeacherNo(@Param("teacherNo") String teacherNo);

    /**
     * 分页查询指定角色的用户（通过user_role表JOIN查询）
     *
     * @param page 分页对象
     * @param roleCode 角色代码（STUDENT/JUDGE/ADMIN）
     * @param keyword 搜索关键词
     * @param status 用户状态
     * @return 分页用户列表
     */
    Page<User> selectUsersByRole(Page<User> page,
                                  @Param("roleCode") String roleCode,
                                  @Param("keyword") String keyword,
                                  @Param("status") String status);

    /**
     * 查询指定角色的所有用户
     *
     * @param roleCode 角色代码（STUDENT/JUDGE/ADMIN）
     * @return 用户列表
     */
    java.util.List<User> selectByRole(@Param("roleCode") String roleCode);
}