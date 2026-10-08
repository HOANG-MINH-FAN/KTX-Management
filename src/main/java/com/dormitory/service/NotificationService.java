
package com.dormitory.service;

import com.dormitory.entity.Notification;
import com.dormitory.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Lấy danh sách thông báo của một người dùng
    public List<Notification> getNotifications(Long userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Đếm thông báo chưa đọc
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    // Tạo thông báo mới
    public void createNotification(Long userId, String type,
                                   String title, String message,
                                   String relatedType, Long relatedId) {
        Notification notification = new Notification();

        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);
        notification.setRead(false);

        notificationRepository.save(notification);
    }

    // Đánh dấu một thông báo đã đọc
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            // Chỉ chủ sở hữu mới được đánh dấu đã đọc
            if (notification.getUserId().equals(userId)) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
    }
}