package com.campus.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.exception.BusinessException;
import com.campus.entity.Favorite;
import com.campus.entity.Work;
import com.campus.mapper.FavoriteMapper;
import com.campus.mapper.WorkMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收藏服务
 */
@Service
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final WorkMapper workMapper;

    public FavoriteService(FavoriteMapper favoriteMapper, WorkMapper workMapper) {
        this.favoriteMapper = favoriteMapper;
        this.workMapper = workMapper;
    }

    /**
     * 收藏/取消收藏（切换）
     * @return true=已收藏，false=已取消
     */
    public boolean toggle(Long userId, Long workId) {
        Work work = workMapper.selectById(workId);
        if (work == null) {
            throw new BusinessException("作品不存在");
        }
        Favorite existing = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getWorkId, workId));
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return false;
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setWorkId(workId);
        favorite.setCreateTime(LocalDateTime.now());
        favoriteMapper.insert(favorite);
        return true;
    }

    /**
     * 是否已收藏
     */
    public boolean isFavorite(Long userId, Long workId) {
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getWorkId, workId));
        return count > 0;
    }

    /**
     * 我的收藏列表（返回作品 id 列表）
     */
    public List<Long> myFavoriteWorkIds(Long userId) {
        return favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime))
                .stream().map(Favorite::getWorkId).toList();
    }

    /**
     * 我的收藏作品列表（返回完整作品）
     */
    public List<Work> myFavorites(Long userId) {
        List<Long> workIds = myFavoriteWorkIds(userId);
        if (workIds.isEmpty()) {
            return List.of();
        }
        return workMapper.selectList(new LambdaQueryWrapper<Work>().in(Work::getId, workIds));
    }
}
