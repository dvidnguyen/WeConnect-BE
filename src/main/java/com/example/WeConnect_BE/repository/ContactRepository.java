package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
