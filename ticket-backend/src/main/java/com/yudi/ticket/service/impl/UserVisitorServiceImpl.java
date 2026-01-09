package com.yudi.ticket.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.common.ThrowUtils;
import com.yudi.ticket.exception.BusinessException;
import com.yudi.ticket.mapper.UserVisitorMapper;
import com.yudi.ticket.model.dto.UserVisitorAddRequest;
import com.yudi.ticket.model.dto.UserVisitorUpdateRequest;
import com.yudi.ticket.model.entity.UserVisitor;
import com.yudi.ticket.model.vo.UserVisitorVO;
import com.yudi.ticket.service.UserVisitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户游客信息服务实现
 */
@Service
@Slf4j
public class UserVisitorServiceImpl extends ServiceImpl<UserVisitorMapper, UserVisitor> implements UserVisitorService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addUserVisitor(UserVisitorAddRequest request, Long userId) {
        // 参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(request.getVisitorName()), ErrorCode.PARAM_ERROR, "游客姓名不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(request.getIdCard()), ErrorCode.PARAM_ERROR, "身份证号不能为空");
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAM_ERROR, "用户ID不能为空");

        // 检查是否已存在相同的身份证号
        LambdaQueryWrapper<UserVisitor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserVisitor::getUserId, userId)
                .eq(UserVisitor::getIdCard, request.getIdCard())
                .eq(UserVisitor::getIsDeleted, 0);
        long count = this.count(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAM_ERROR, "该身份证号已存在");

        // 如果设置为默认，先取消其他默认设置
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            this.cancelDefaultVisitors(userId);
        }

        // 创建游客信息
        UserVisitor visitor = new UserVisitor();
        BeanUtil.copyProperties(request, visitor);
        visitor.setUserId(userId);
        visitor.setDefaultTicketType(request.getDefaultTicketType() != null ? request.getDefaultTicketType() : 1);
        visitor.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : 0);

        boolean result = this.save(visitor);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "添加游客信息失败");

        log.info("用户 {} 添加游客信息成功，游客姓名: {}", userId, request.getVisitorName());
        return visitor.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserVisitor(UserVisitorUpdateRequest request, Long userId) {
        // 参数校验
        ThrowUtils.throwIf(request == null || request.getId() == null, ErrorCode.PARAM_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(request.getVisitorName()), ErrorCode.PARAM_ERROR, "游客姓名不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(request.getIdCard()), ErrorCode.PARAM_ERROR, "身份证号不能为空");
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAM_ERROR, "用户ID不能为空");

        // 检查游客信息是否存在且属于当前用户
        UserVisitor existingVisitor = this.getById(request.getId());
        ThrowUtils.throwIf(existingVisitor == null || existingVisitor.getIsDeleted() == 1,
                ErrorCode.NOT_FOUND_ERROR, "游客信息不存在");
        ThrowUtils.throwIf(!existingVisitor.getUserId().equals(userId),
                ErrorCode.FORBIDDEN, "无权修改此游客信息");

        // 检查是否已存在相同的身份证号（排除当前记录）
        if (!existingVisitor.getIdCard().equals(request.getIdCard())) {
            LambdaQueryWrapper<UserVisitor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserVisitor::getUserId, userId)
                    .eq(UserVisitor::getIdCard, request.getIdCard())
                    .ne(UserVisitor::getId, request.getId())
                    .eq(UserVisitor::getIsDeleted, 0);
            long count = this.count(queryWrapper);
            ThrowUtils.throwIf(count > 0, ErrorCode.PARAM_ERROR, "该身份证号已存在");
        }

        // 如果设置为默认，先取消其他默认设置
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            this.cancelDefaultVisitors(userId);
        }

        // 更新游客信息
        UserVisitor visitor = new UserVisitor();
        BeanUtil.copyProperties(request, visitor);
        visitor.setDefaultTicketType(request.getDefaultTicketType() != null ? request.getDefaultTicketType() : existingVisitor.getDefaultTicketType());
        visitor.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : 0);

        boolean result = this.updateById(visitor);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新游客信息失败");

        log.info("用户 {} 更新游客信息成功，游客ID: {}", userId, request.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserVisitor(Long visitorId, Long userId) {
        // 参数校验
        ThrowUtils.throwIf(visitorId == null || userId == null, ErrorCode.PARAM_ERROR);

        // 检查游客信息是否存在且属于当前用户
        UserVisitor visitor = this.getById(visitorId);
        ThrowUtils.throwIf(visitor == null || visitor.getIsDeleted() == 1,
                ErrorCode.NOT_FOUND_ERROR, "游客信息不存在");
        ThrowUtils.throwIf(!visitor.getUserId().equals(userId),
                ErrorCode.FORBIDDEN, "无权删除此游客信息");

        boolean result = this.removeById(visitorId);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "删除游客信息失败");

        log.info("用户 {} 删除游客信息成功，游客ID: {}", userId, visitorId);
        return true;
    }

    @Override
    public List<UserVisitorVO> listUserVisitors(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAM_ERROR, "用户ID不能为空");

        LambdaQueryWrapper<UserVisitor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserVisitor::getUserId, userId)
                .eq(UserVisitor::getIsDeleted, 0)
                .orderByDesc(UserVisitor::getIsDefault) // 默认游客排在前面
                .orderByDesc(UserVisitor::getCreateTime);

        List<UserVisitor> visitors = this.list(queryWrapper);

        return visitors.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public UserVisitorVO getUserVisitorById(Long visitorId, Long userId) {
        ThrowUtils.throwIf(visitorId == null || userId == null, ErrorCode.PARAM_ERROR);

        UserVisitor visitor = this.getById(visitorId);
        ThrowUtils.throwIf(visitor == null || visitor.getIsDeleted() == 1,
                ErrorCode.NOT_FOUND_ERROR, "游客信息不存在");
        ThrowUtils.throwIf(!visitor.getUserId().equals(userId),
                ErrorCode.FORBIDDEN, "无权查看此游客信息");

        return convertToVO(visitor);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultVisitor(Long visitorId, Long userId) {
        ThrowUtils.throwIf(visitorId == null || userId == null, ErrorCode.PARAM_ERROR);

        // 检查游客信息是否存在且属于当前用户
        UserVisitor visitor = this.getById(visitorId);
        ThrowUtils.throwIf(visitor == null || visitor.getIsDeleted() == 1,
                ErrorCode.NOT_FOUND_ERROR, "游客信息不存在");
        ThrowUtils.throwIf(!visitor.getUserId().equals(userId),
                ErrorCode.FORBIDDEN, "无权修改此游客信息");

        // 取消其他默认设置
        this.cancelDefaultVisitors(userId);

        // 设置为默认
        LambdaUpdateWrapper<UserVisitor> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserVisitor::getId, visitorId)
                .set(UserVisitor::getIsDefault, 1);

        boolean result = this.update(updateWrapper);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "设置默认游客失败");

        log.info("用户 {} 设置默认游客成功，游客ID: {}", userId, visitorId);
        return true;
    }

    @Override
    public UserVisitorVO getDefaultVisitor(Long userId) {
        ThrowUtils.throwIf(userId == null, ErrorCode.PARAM_ERROR, "用户ID不能为空");

        LambdaQueryWrapper<UserVisitor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserVisitor::getUserId, userId)
                .eq(UserVisitor::getIsDefault, 1)
                .eq(UserVisitor::getIsDeleted, 0);

        UserVisitor visitor = this.getOne(queryWrapper);
        return visitor != null ? convertToVO(visitor) : null;
    }

    /**
     * 取消用户的默认游客设置
     */
    private void cancelDefaultVisitors(Long userId) {
        LambdaUpdateWrapper<UserVisitor> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserVisitor::getUserId, userId)
                .set(UserVisitor::getIsDefault, 0);

        this.update(updateWrapper);
    }

    /**
     * 转换为VO对象
     */
    private UserVisitorVO convertToVO(UserVisitor visitor) {
        UserVisitorVO vo = new UserVisitorVO();
        BeanUtil.copyProperties(visitor, vo);

        // 设置票种类型名称
        switch (visitor.getDefaultTicketType()) {
            case 1:
                vo.setTicketTypeName("原价票");
                break;
            case 2:
                vo.setTicketTypeName("半价票");
                break;
            case 3:
                vo.setTicketTypeName("免票");
                break;
            default:
                vo.setTicketTypeName("原价票");
        }

        return vo;
    }
}