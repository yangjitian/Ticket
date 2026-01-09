package com.yudi.ticket.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.exception.BusinessException;
import com.yudi.ticket.model.dto.ScenicSpotAddRequest;
import com.yudi.ticket.model.dto.ScenicSpotQueryRequest;
import com.yudi.ticket.model.dto.ScenicSpotUpdateRequest;
import com.yudi.ticket.model.entity.ScenicSpot;
import com.yudi.ticket.model.vo.ScenicSpotVO;
import com.yudi.ticket.service.ScenicSpotService;
import com.yudi.ticket.mapper.ScenicSpotMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 景点服务实现
 * @author yudi
 * @createDate 2025-12-26
 */
@Service
@Slf4j
public class ScenicSpotServiceImpl extends ServiceImpl<ScenicSpotMapper, ScenicSpot>
    implements ScenicSpotService {

    @Override
    public Long addScenicSpot(ScenicSpotAddRequest request) {
        // 参数校验
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数不能为空");
        }
        if (StrUtil.isBlank(request.getSpotName())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "景点名称不能为空");
        }

        log.info("添加景区请求 - spotName: {}, imageUrls: {}", request.getSpotName(), request.getImageUrls());

        // 创建景点对象（不包含图片URL，图片由专门的上传接口处理）
        ScenicSpot scenicSpot = new ScenicSpot();
        BeanUtil.copyProperties(request, scenicSpot, "imageUrls"); // 排除imageUrls字段

        // 设置默认状态
        if (scenicSpot.getSpotStatus() == null) {
            scenicSpot.setSpotStatus(1); // 默认开放
        }

        // 保存到数据库
        boolean result = this.save(scenicSpot);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加景点失败");
        }

        log.info("景点添加成功: {}", scenicSpot.getSpotName());
        return scenicSpot.getId();
    }

    @Override
    public boolean updateScenicSpot(ScenicSpotUpdateRequest request) {
        // 参数校验
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求参数或景点ID不能为空");
        }
        log.info("前端传输的景区Id: {}, imageUrls: {}", request.getId(), request.getImageUrls());

        // 检查景点是否存在
        ScenicSpot existingSpot = this.getById(request.getId());
        if (existingSpot == null || existingSpot.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.SCENIC_SPOT_NOT_EXIST, "景点不存在");
        }
        // 更新景点信息（不包含图片URL，图片URL由上传接口单独处理）
        ScenicSpot scenicSpot = new ScenicSpot();
        scenicSpot.setId(request.getId());

        // 复制所有字段，但排除id和imageUrls（图片由专门的上传接口处理）
        BeanUtil.copyProperties(request, scenicSpot, "id", "imageUrls");

        boolean result = this.updateById(scenicSpot);
        if (result) {
            log.info("景点信息更新成功: id={}, name={}", request.getId(), request.getSpotName());
        }
        return result;
    }

    @Override
    public boolean deleteScenicSpot(Long id) {
        // 参数校验
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "景点ID不能为空");
        }

        // 检查景点是否存在
        ScenicSpot existingSpot = this.getById(id);
        if (existingSpot == null || existingSpot.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.SCENIC_SPOT_NOT_EXIST, "景点不存在");
        }

        // 逻辑删除
        boolean result = this.removeById(id);
        if (result) {
            log.info("景点删除成功: id={}, name={}", id, existingSpot.getSpotName());
        }
        return result;
    }

    @Override
    public IPage<ScenicSpotVO> queryScenicSpotPage(ScenicSpotQueryRequest request) {
        if (request == null) {
            request = new ScenicSpotQueryRequest();
        }

        // 构建查询条件
        LambdaQueryWrapper<ScenicSpot> queryWrapper = new LambdaQueryWrapper<>();

        // ID查询
        if (request.getId() != null) {
            queryWrapper.eq(ScenicSpot::getId, request.getId());
        }

        // 名称模糊查询
        if (StrUtil.isNotBlank(request.getSpotName())) {
            queryWrapper.like(ScenicSpot::getSpotName, request.getSpotName());
        }

        // 等级精确查询
        if (StrUtil.isNotBlank(request.getLevel())) {
            queryWrapper.eq(ScenicSpot::getLevel, request.getLevel());
        }

        // 省份查询（使用模糊匹配，兼容带/不带“省/市/自治区”等后缀的写法）
        if (StrUtil.isNotBlank(request.getProvince())) {
            // 使用 like 匹配，避免存储时省份名称带不带后缀导致无法匹配
            queryWrapper.like(ScenicSpot::getProvince, request.getProvince());
        }

        // 地址模糊查询
        if (StrUtil.isNotBlank(request.getAddress())) {
            queryWrapper.like(ScenicSpot::getAddress, request.getAddress());
        }

        // 状态查询
        if (request.getSpotStatus() != null) {
            queryWrapper.eq(ScenicSpot::getSpotStatus, request.getSpotStatus());
        }

        // 只查询未删除的记录
        queryWrapper.eq(ScenicSpot::getIsDeleted, 0);

        // 按创建时间倒序
        queryWrapper.orderByDesc(ScenicSpot::getCreateTime);

        // 分页查询
        Page<ScenicSpot> page = new Page<>(request.getCurrent(), request.getPageSize());
        IPage<ScenicSpot> scenicSpotPage = this.page(page, queryWrapper);

        // 转换为VO
        return scenicSpotPage.convert(this::convertToVO);
    }

    @Override
    public ScenicSpotVO getScenicSpotById(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "景点ID不能为空");
        }

        ScenicSpot scenicSpot = this.getById(id);
        if (scenicSpot == null || scenicSpot.getIsDeleted() == 1) {
            return null;
        }

        return convertToVO(scenicSpot);
    }

    /**
     * 转换为VO对象
     */
    private ScenicSpotVO convertToVO(ScenicSpot scenicSpot) {
        ScenicSpotVO vo = new ScenicSpotVO();
        BeanUtil.copyProperties(scenicSpot, vo);

        // 处理图片URL列表
        if (scenicSpot.getImageUrls() != null) {
            try {
                List<String> imageUrls = JSONUtil.toList(scenicSpot.getImageUrls(), String.class);
                vo.setImageUrls(imageUrls);
            } catch (Exception e) {
                log.warn("解析图片URL列表失败: {}", scenicSpot.getImageUrls());
                vo.setImageUrls(CollUtil.newArrayList());
            }
        } else {
            vo.setImageUrls(CollUtil.newArrayList());
        }

        return vo;
    }

    /**
     * 将Object类型转换为Long类型
     * @param obj 要转换的对象
     * @return Long类型的值
     */
    private Long convertToLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof String) {
            try {
                return Long.valueOf((String) obj);
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "ID格式不正确，无法转换为Long类型");
            }
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        throw new BusinessException(ErrorCode.PARAM_ERROR, "ID类型不支持转换为Long类型");
    }
}




