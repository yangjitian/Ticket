package com.yudi.ticket.controller;

import cn.hutool.core.util.StrUtil;
import com.yudi.ticket.aop.RequireAdmin;
import com.yudi.ticket.aop.RequireLogin;
import com.yudi.ticket.common.BaseResponse;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.common.ThrowUtils;
import com.yudi.ticket.model.dto.UserVisitorAddRequest;
import com.yudi.ticket.model.dto.UserVisitorDeleteRequest;
import com.yudi.ticket.model.dto.UserVisitorUpdateRequest;
import com.yudi.ticket.model.vo.UserVisitorVO;
import com.yudi.ticket.service.UserVisitorService;
import com.yudi.ticket.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户游客信息接口
 */
@RestController
@RequestMapping("/user-visitor")
@Slf4j
public class UserVisitorController {

    @Resource
    private UserVisitorService userVisitorService;

    /**
     * 添加游客信息
     */
    @PostMapping("/add")
    @RequireLogin
    public BaseResponse<Long> addUserVisitor(@RequestBody UserVisitorAddRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        Long userId = getUserId(httpRequest);

        Long visitorId = userVisitorService.addUserVisitor(request, userId);
        return BaseResponse.success(visitorId);
    }

    /**
     * 更新游客信息
     */
    @PostMapping("/update")
    @RequireLogin
    public BaseResponse<Boolean> updateUserVisitor(@RequestBody UserVisitorUpdateRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        Long userId = getUserId(httpRequest);

        boolean result = userVisitorService.updateUserVisitor(request, userId);
        return BaseResponse.success(result);
    }

    /**
     * 删除游客信息
     */
    @PostMapping("/delete")
    @RequireLogin
    public BaseResponse<Boolean> deleteUserVisitor(@RequestBody UserVisitorDeleteRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        Long userId = getUserId(httpRequest);

        boolean result = userVisitorService.deleteUserVisitor(request.getId(), userId);
        return BaseResponse.success(result);
    }

    /**
     * 查询用户的游客信息列表
     */
    @GetMapping("/list")
    @RequireLogin
    public BaseResponse<List<UserVisitorVO>> listUserVisitors(HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);

        List<UserVisitorVO> visitors = userVisitorService.listUserVisitors(userId);
        return BaseResponse.success(visitors);
    }

    /**
     * 根据ID查询游客信息详情
     */
    @GetMapping("/get")
    @RequireLogin
    public BaseResponse<UserVisitorVO> getUserVisitorById(@RequestParam Long id, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAM_ERROR, "游客信息ID不能为空");
        Long userId = getUserId(httpRequest);

        UserVisitorVO visitor = userVisitorService.getUserVisitorById(id, userId);
        return BaseResponse.success(visitor);
    }

    /**
     * 设置默认游客
     */
    @PostMapping("/set-default")
    @RequireLogin
    public BaseResponse<Boolean> setDefaultVisitor(@RequestParam Long visitorId, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(visitorId == null, ErrorCode.PARAM_ERROR, "游客信息ID不能为空");
        Long userId = getUserId(httpRequest);

        boolean result = userVisitorService.setDefaultVisitor(visitorId, userId);
        return BaseResponse.success(result);
    }

    /**
     * 获取用户的默认游客信息
     */
    @GetMapping("/default")
    @RequireLogin
    public BaseResponse<UserVisitorVO> getDefaultVisitor(HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);

        UserVisitorVO visitor = userVisitorService.getDefaultVisitor(userId);
        return BaseResponse.success(visitor);
    }

    /**
     * 获取当前用户ID（AOP已确保登录状态）
     */
    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return JwtUtils.getUserId(token);
    }
}
