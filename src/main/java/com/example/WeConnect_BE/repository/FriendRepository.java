package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.Util.TypeNotification;
import com.example.WeConnect_BE.entity.Friend;
import com.example.WeConnect_BE.entity.Notification;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface FriendRepository extends JpaRepository<Friend, String> {
    boolean existsByRequesterUserIdAndAddresseeUserId(String from, String to);

    Optional<Friend> findByRequesterUserIdAndAddresseeUserId(String requesterId, String addresseeId);

    List<Friend> findByAddressee_UserIdAndStatus(String addresseeUserId, Friend.FriendStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE) // SELECT ... FOR UPDATE
    @Query("""
      select f from Friend f
      where f.requester.userId = :requesterId and f.addressee.userId = :addresseeId
    """)
    Optional<Friend> lockByRequesterAndAddressee(String requesterId, String addresseeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE) // SELECT ... FOR UPDATE
    @Query("select f from Friend f where f.id = :id")
    Optional<Friend> lockById(@Param("id") String id);

    Optional<Friend> findByIdAndRequester_UserId(String id, String requesterId);
    // Tìm noti theo type + user nhận + khoảng thời gian (join qua UserNotification)

}
