package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.Util.TypeNotification;
import com.example.WeConnect_BE.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    // Tìm noti theo type + user nhận + khoảng thời gian (join qua UserNotification)
    @Query("""
        select n from Notification n
        join UserNotification un on un.notification = n
        where un.user.userId = :toUserId
          and n.type = :type
          and n.createdAt >= :from
        order by n.createdAt desc
    """)
    List<Notification> findByTypeForUserFromTime(
            @Param("toUserId") String toUserId,
            @Param("type") TypeNotification type,
            @Param("from") Date fromTime);
}