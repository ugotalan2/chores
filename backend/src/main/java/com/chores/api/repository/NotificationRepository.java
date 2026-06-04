package com.chores.api.repository;

import com.chores.api.entity.Notification;
import com.chores.api.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // all unread notifications
    List<Notification> findByReadAtIsNull();

    // find by notification type
    List<Notification> findByNotificationType(NotificationType notificationType);
}
