package com.yudi.ticket.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yudi.ticket.model.dto.ScenicSpotAddRequest;
import com.yudi.ticket.model.dto.ScenicSpotQueryRequest;
import com.yudi.ticket.model.dto.ScenicSpotUpdateRequest;
import com.yudi.ticket.model.entity.ScenicSpot;
import com.yudi.ticket.model.vo.ScenicSpotVO;

/**
 * 景点服务接口
 * @author yudi
 * @createDate 2025-12-26
 */
public interface ScenicSpotService extends IService<ScenicSpot> {

    /**
     * 添加景点
     * @param request 添加请求
     * @return 景点ID
     */
    Long addScenicSpot(ScenicSpotAddRequest request);

    /**
     * 更新景点
     * @param request 更新请求
     * @return 是否成功
     */
    boolean updateScenicSpot(ScenicSpotUpdateRequest request);

    /**
     * 删除景点
     * @param id 景点ID
     * @return 是否成功
     */
    boolean deleteScenicSpot(Long id);

    /**
     * 分页查询景点
     * @param request 查询请求
     * @return 分页结果
     */
    IPage<ScenicSpotVO> queryScenicSpotPage(ScenicSpotQueryRequest request);

    /**
     * 根据ID获取景点详情
     * @param id 景点ID
     * @return 景点详情
     */
    ScenicSpotVO getScenicSpotById(Long id);
}
