package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.dto.DemandRequest;
import com.campus.entity.Demand;
import com.campus.service.DemandService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 约拍需求接口
 */
@RestController
@RequestMapping("/api/demand")
public class DemandController {

    private final DemandService demandService;

    public DemandController(DemandService demandService) {
        this.demandService = demandService;
    }

    /**
     * 发布需求
     */
    @PostMapping
    public Result<Demand> create(@Valid @RequestBody DemandRequest request, Authentication auth) {
        return Result.success(demandService.create((Long) auth.getPrincipal(), request));
    }

    /**
     * 需求列表（分页）
     */
    @GetMapping
    public Result<PageResult<DemandService.DemandVO>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String styleTag,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location) {
        var page = demandService.page(current, size, status, styleTag, keyword, location);
        List<DemandService.DemandVO> vos = page.getRecords().stream()
                .map(demandService::toVO).toList();
        PageResult<DemandService.DemandVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setRecords(vos);
        return Result.success(result);
    }

    /**
     * 需求详情
     */
    @GetMapping("/{id}")
    public Result<DemandService.DemandVO> detail(@PathVariable Long id) {
        return Result.success(demandService.toVO(demandService.getById(id)));
    }

    /**
     * 编辑需求
     */
    @PutMapping("/{id}")
    public Result<Demand> update(@PathVariable Long id,
                                 @Valid @RequestBody DemandRequest request,
                                 Authentication auth) {
        return Result.success(demandService.update((Long) auth.getPrincipal(), id, request));
    }

    /**
     * 关闭需求
     */
    @PostMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id, Authentication auth) {
        demandService.close((Long) auth.getPrincipal(), id);
        return Result.success();
    }

    /**
     * 删除需求
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        demandService.delete((Long) auth.getPrincipal(), id);
        return Result.success();
    }
}
