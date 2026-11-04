package com.dormitory.exchange.service;

import com.dormitory.exchange.entity.ExchangeFavorite;
import com.dormitory.exchange.entity.ExchangeItem;
import com.dormitory.exchange.repository.ExchangeFavoriteRepository;
import com.dormitory.exchange.repository.ExchangeItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExchangeFavoriteService {

    private final ExchangeFavoriteRepository favoriteRepository;
    private final ExchangeItemRepository itemRepository;

    public ExchangeFavoriteService(ExchangeFavoriteRepository favoriteRepository,
                                   ExchangeItemRepository itemRepository) {
        this.favoriteRepository = favoriteRepository;
        this.itemRepository = itemRepository;
    }

    /** Đang lưu thì bỏ lưu, chưa lưu thì lưu. Trả về true nếu sau thao tác là "đã lưu". */
    @Transactional
    public boolean toggle(Long userId, Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new IllegalArgumentException("Tin đăng không tồn tại.");
        }
        Optional<ExchangeFavorite> existing = favoriteRepository.findByUserIdAndItemId(userId, itemId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false;
        }
        ExchangeFavorite fav = new ExchangeFavorite();
        fav.setUserId(userId);
        fav.setItemId(itemId);
        favoriteRepository.save(fav);
        return true;
    }

    /** Danh sách tin đã lưu, mới lưu nằm trên cùng. */
    public List<ExchangeItem> getFavoriteItems(Long userId) {
        List<Long> ids = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(ExchangeFavorite::getItemId).toList();
        Map<Long, ExchangeItem> byId = itemRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(ExchangeItem::getId, i -> i));
        return ids.stream().map(byId::get).filter(Objects::nonNull).toList();
    }
}