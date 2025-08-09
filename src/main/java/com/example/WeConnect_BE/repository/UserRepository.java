package com.example.WeConnect_BE.repository;

import com.example.WeConnect_BE.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface UserRepository extends JpaRepository<User, String> {

   Optional<User> findByEmail(String email);

   boolean existsByEmail(String email);

   Optional<User> findByEmailIgnoreCase(String email);

   @Query("select u from User u where u.phone = :phoneNormalized")
   Optional<User> findByPhoneNormalized(@Param("phoneNormalized") String phoneNormalized);

   @Query("""
   select u from User u
   where lower(u.email) like lower(concat(:term, '%'))
      or u.phone like concat(:phoneNorm, '%')
   """)
   List<User> searchLoose(@Param("term") String term,
                          @Param("phoneNorm") String phoneNorm);

   boolean existsByPhone(String phone);

   @Query("select case when count(u) > 0 then true else false end " +
           "from User u where u.phone = :phone and u.userId <> :userId")
   boolean existsByPhoneAndNotUserId(@Param("phone") String phone, @Param("userId") String userId);
}
