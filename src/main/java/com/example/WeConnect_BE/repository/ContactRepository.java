package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface ContactRepository extends JpaRepository<Contact, String> {

    @Query("SELECT c FROM Contact c WHERE c.requesterUser.userId = :requesterId AND c.addresseeUser.userId = :addresseeId")
    Optional<Contact> findByRequesterUserIdAndAddresseeUserId(String requesterId, String addresseeId);


    boolean existsByRequesterUser_UserIdAndAddresseeUser_UserId(String requesterId, String addresseeId);

    @Modifying
    @Query("""
      delete from Contact c
      where (c.requesterUser.userId = :u1 and c.addresseeUser.userId = :u2)
         or (c.requesterUser.userId = :u2 and c.addresseeUser.userId = :u1)
    """)
    int deleteByPair(@Param("u1") String u1, @Param("u2") String u2);

    @Query("""
        select c from Contact c
        where c.requesterUser.userId = :userId
           or c.addresseeUser.userId = :userId
    """)
    List<Contact> findAllByUserId(@Param("userId") String userId);

    @Query("""
        select (count(c) > 0) from Contact c
        where c.requesterUser.userId = :a and c.addresseeUser.userId = :b
    """)
    boolean existsPair(@Param("a") String a, @Param("b") String b);

    // Tìm quan hệ giữa 2 user bất kể chiều
    @Query("""
        select c from Contact c
        where (c.requesterUser.userId = :u1 and c.addresseeUser.userId = :u2)
           or (c.requesterUser.userId = :u2 and c.addresseeUser.userId = :u1)
    """)
    Optional<Contact> findBetween(@Param("u1") String u1, @Param("u2") String u2);

    // Xóa quan hệ giữa 2 user bất kể chiều
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from Contact c
        where (c.requesterUser.userId = :u1 and c.addresseeUser.userId = :u2)
           or (c.requesterUser.userId = :u2 and c.addresseeUser.userId = :u1)
    """)
    int deleteBetween(@Param("u1") String u1, @Param("u2") String u2);

    @Query("""
        select case
                 when c.requesterUser.userId = :currentUserId then c.addresseeUser.userId
                 else c.requesterUser.userId
               end
        from Contact c
        where (c.requesterUser.userId = :currentUserId and c.addresseeUser.userId in :otherIds)
           or (c.addresseeUser.userId = :currentUserId and c.requesterUser.userId in :otherIds)
    """)
    List<String> findContactUserIdsWith(@Param("currentUserId") String currentUserId,
                                        @Param("otherIds") List<String> otherIds);
}
