package com.campus.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.dto.WorkRequest;
import com.campus.entity.Work;
import com.campus.service.WorkService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作品接口
 */
@RestController
@RequestMapping("/api/work")
public class WorkController {

    private final WorkService workService;

    public WorkController(WorkService workService) {
        this.workService = workService;
    }

    /**
     * 发布作品
     */
    @PostMapping
    public Result<Work> create(@Valid @RequestBody WorkRequest request, Authentication auth) {
        return Result.success(workService.create((Long) auth.getPrincipal(), request));
    }

    /**
     * 作品列表（分页）
     */
    @GetMapping
    public Result<PageResult<WorkService.WorkVO>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String styleTag,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        var page = workService.page(current, size, styleTag, keyword, status);
        List<WorkService.WorkVO> vos = page.getRecords().stream().map(workService::toVO).toList();
        PageResult<WorkService.WorkVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setRecords(vos);
        return Result.success(result);
    }

    /**
     * 作品详情
     */
    @GetMapping("/{id}")
    public Result<WorkService.WorkVO> detail(@PathVariable Long id) {
        return Result.success(workService.toVO(workService.getById(id)));
    }

    /**
     * 编辑作品
     */
    @PutMapping("/{id}")
    public Result<Work> update(@PathVariable Long id,
                               @Valid @RequestBody WorkRequest request,
                               Authentication auth) {
        return Result.success(workService.update((Long) auth.getPrincipal(), id, request));
    }

    /**
     * 删除作品
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        workService.delete((Long) auth.getPrincipal(), id);
        return Result.success();
    }

    /**
     * 点赞作品
     */
    @PostMapping("/{id}/like")
    public Result<Work> like(@PathVariable Long id) {
        return Result.success(workService.like(id));
    }

    /**
     * 某摄影师的作品列表
     */
    @GetMapping("/photographer/{photographerId}")
    public Result<List<Work>> listByPhotographer(@PathVariable Long photographerId) {
        return Result.success(workService.listByPhotographer(photographerId));
    }
}
