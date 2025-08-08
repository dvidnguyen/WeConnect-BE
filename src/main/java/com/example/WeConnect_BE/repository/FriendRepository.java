package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.Friend;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    @Query("""
      select f from Friend f
      where f.requester.userId = :requesterId and f.addressee.userId = :addresseeId
    """)
    Optional<Friend> lockByPair(@Param("requesterId") String requesterId,
                                @Param("addresseeId") String addresseeId);
}
