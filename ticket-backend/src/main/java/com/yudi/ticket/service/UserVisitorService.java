package com.yudi.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yudi.ticket.model.dto.UserVisitorAddRequest;
import com.yudi.ticket.model.dto.UserVisitorQueryRequest;
import com.yudi.ticket.model.dto.UserVisitorUpdateRequest;
import com.yudi.ticket.model.entity.UserVisitor;
import com.yudi.ticket.model.vo.UserVisitorVO;

import java.util.List;

/**
 * 用户游客信息服务
 */
public interface UserVisitorService extends IService<UserVisitor> {

    /**
     * 添加游客信息
     *
     * @param request 添加请求
     * @param userId 用户ID
     * @return 游客信息ID
     */
    Long addUserVisitor(UserVisitorAddRequest request, Long userId);

    /**
     * 更新游客信息
     *
     * @param request 更新请求
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean updateUserVisitor(UserVisitorUpdateRequest request, Long userId);

    /**
     * 删除游客信息
     *
     * @param visitorId 游客信息ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUserVisitor(Long visitorId, Long userId);

    /**
     * 查询用户的所有游客信息
     *
     * @param userId 用户ID
     * @return 游客信息列表
     */
    List<UserVisitorVO> listUserVisitors(Long userId);

    /**
     * 根据ID查询游客信息详情
     *
     * @param visitorId 游客信息ID
     * @param userId 用户ID
     * @return 游客信息VO
     */
    UserVisitorVO getUserVisitorById(Long visitorId, Long userId);

    /**
     * 设置默认游客
     *
     * @param visitorId 游客信息ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean setDefaultVisitor(Long visitorId, Long userId);

    /**
     * 获取用户的默认游客信息
     *
     * @param userId 用户ID
     * @return 默认游客信息
     */
    UserVisitorVO getDefaultVisitor(Long userId);
}