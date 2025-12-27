package com.yudi.ticket.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yudi.ticket.aop.RequireAdmin;
import com.yudi.ticket.common.BaseResponse;
import com.yudi.ticket.common.ErrorCode;
import com.yudi.ticket.common.ThrowUtils;
import com.yudi.ticket.exception.BusinessException;
import com.yudi.ticket.model.dto.ScenicSpotAddRequest;
import com.yudi.ticket.model.dto.ScenicSpotQueryRequest;
import com.yudi.ticket.model.dto.ScenicSpotUpdateRequest;
import com.yudi.ticket.model.entity.ScenicSpot;
import com.yudi.ticket.model.vo.ScenicSpotVO;
import com.yudi.ticket.service.ScenicSpotService;
import com.yudi.ticket.manager.CosManager;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 景点控制器
 */
@RestController
@RequestMapping("/scenicSpot")
@Slf4j
public class ScenicSpotController {

    @Resource
    private ScenicSpotService scenicSpotService;

    @Resource
    private CosManager cosManager;

    private static final List<String> ALLOWED_IMAGE_SUFFIX = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_IMAGE_SIZE = 4 * 1024 * 1024L;
    private static final String PROJECT_NAME = "ticket";

    @PostMapping("/add")
    @RequireAdmin
    public BaseResponse<Long> addScenicSpot(@RequestBody ScenicSpotAddRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        Long scenicSpotId = scenicSpotService.addScenicSpot(request);
        return BaseResponse.success(scenicSpotId);
    }

    @PostMapping("/update")
    @RequireAdmin
    public BaseResponse<Boolean> updateScenicSpot(@RequestBody ScenicSpotUpdateRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAM_ERROR);
        boolean result = scenicSpotService.updateScenicSpot(request);
        return BaseResponse.success(result);
    }

    @PostMapping("/delete")
    @RequireAdmin
    public BaseResponse<Boolean> deleteScenicSpot(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAM_ERROR);
        boolean result = scenicSpotService.deleteScenicSpot(id);
        return BaseResponse.success(result);
    }

    @PostMapping("/list")
    public BaseResponse<IPage<ScenicSpotVO>> listScenicSpots(@RequestBody ScenicSpotQueryRequest request) {
        IPage<ScenicSpotVO> page = scenicSpotService.queryScenicSpotPage(request);
        return BaseResponse.success(page);
    }

    @GetMapping("/get")
    public BaseResponse<ScenicSpotVO> getScenicSpotById(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAM_ERROR);
        ScenicSpotVO scenicSpot = scenicSpotService.getScenicSpotById(id);
        return BaseResponse.success(scenicSpot);
    }

    @PostMapping("/upload/image")
    @RequireAdmin
    public BaseResponse<String> uploadScenicSpotImage(
            @RequestPart("file") MultipartFile multipartFile,
            @RequestParam("scenicSpotId") Long scenicSpotId) {
        // 1. 校验
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAM_ERROR, "文件不能为空");
        ThrowUtils.throwIf(multipartFile.getSize() > MAX_IMAGE_SIZE, ErrorCode.PARAM_ERROR, "文件大小不能超过 4MB");

        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!ALLOWED_IMAGE_SUFFIX.contains(suffix), ErrorCode.PARAM_ERROR, "不支持的文件格式");

        // 2. 上传到COS
        String filename = IdUtil.fastSimpleUUID() + "." + suffix;
        String key = String.format("%s/scenic_spot/%s/%s", PROJECT_NAME, "images", filename);
        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(filename, null);
            multipartFile.transferTo(file);
            cosManager.putObject(key, file);
            String url = cosManager.getFileUrl(key);

            // 3. 获取当前景区的图片列表
            ScenicSpot existingSpot = scenicSpotService.getById(scenicSpotId);
            if (existingSpot == null) {
                log.error("景区不存在: {}", scenicSpotId);
                throw new BusinessException(ErrorCode.PARAM_ERROR, "景区不存在");
            }

            List<String> imageUrls = new ArrayList<>();
            if (StrUtil.isNotBlank(existingSpot.getImageUrls())) {
                try {
                    imageUrls = JSONUtil.toList(existingSpot.getImageUrls(), String.class);
                } catch (Exception e) {
                    log.warn("解析现有图片URL失败: {}", existingSpot.getImageUrls());
                    imageUrls = new ArrayList<>();
                }
            }

            // 4. 添加新图片URL到列表
            imageUrls.add(url);

            // 5. 更新数据库中的图片URL列表
            ScenicSpot scenicSpot = new ScenicSpot();
            scenicSpot.setId(scenicSpotId);
            scenicSpot.setImageUrls(JSONUtil.toJsonStr(imageUrls));
            boolean updateResult = scenicSpotService.updateById(scenicSpot);

            if (!updateResult) {
                log.warn("景区图片URL列表更新数据库失败, scenicSpotId: {}, urls: {}", scenicSpotId, imageUrls);
            } else {
                log.info("景区图片上传并更新数据库成功: scenicSpotId={}, 新增url={}, 总数量={}",
                        scenicSpotId, url, imageUrls.size());
            }

            return BaseResponse.success(url);
        } catch (Exception e) {
            log.error("景区图片上传失败", e);
            throw new RuntimeException("上传失败");
        } finally {
            // 删除临时文件
            if (file != null && file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    log.warn("删除临时文件失败: {}", file.getAbsolutePath());
                }
            }
        }
    }
}
