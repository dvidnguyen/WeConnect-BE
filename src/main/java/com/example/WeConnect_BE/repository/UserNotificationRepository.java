package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.dto.response.NotificationRaw;
import com.example.WeConnect_BE.dto.response.NotificationResponse;
import com.example.WeConnect_BE.entity.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotification,String> {
    // Đếm chưa đọc của user
    long countByUser_UserIdAndIsRead(String userId, int isRead);

    @Query("""
        select new com.example.WeConnect_BE.dto.response.NotificationRaw(
            n.id, n.title, n.body, cast(n.type as string), n.relatedId,
            n.createdAt,
            case when un.isRead = 1 then true else false end
        )
        from UserNotification un
        join un.notification n
        where un.user.userId = :uid
        order by n.createdAt desc
    """)
    List<NotificationRaw> findAllByUser(@Param("uid") String userId);

    @EntityGraph(attributePaths = {"notification"})
    UserNotification findByUser_UserIdAndNotification_Id(String userId, String notificationId);

    @Modifying
    @Query("update UserNotification un set un.isRead = 1 where un.user.userId = :uid and un.isRead = 0")
    int markAllRead(@Param("uid") String userId);
}
