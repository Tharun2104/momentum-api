package com.mttauto.momentum_api.friends.repository;

import com.mttauto.momentum_api.friends.entity.FriendRequest;
import com.mttauto.momentum_api.friends.entity.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    @Query("""
            select count(fr) > 0
            from FriendRequest fr
            where fr.status = :status
              and (
                (fr.sender.id = :leftUserId and fr.receiver.id = :rightUserId)
                or (fr.sender.id = :rightUserId and fr.receiver.id = :leftUserId)
              )
            """)
    boolean existsBetweenUsersWithStatus(
            @Param("leftUserId") Long leftUserId,
            @Param("rightUserId") Long rightUserId,
            @Param("status") FriendRequestStatus status
    );

    List<FriendRequest> findByReceiver_IdAndStatusOrderByCreatedAtDesc(
            Long receiverId,
            FriendRequestStatus status
    );

    List<FriendRequest> findBySender_IdAndStatusOrderByCreatedAtDesc(
            Long senderId,
            FriendRequestStatus status
    );

    Optional<FriendRequest> findByIdAndReceiver_Id(Long id, Long receiverId);

    @Query("""
            select fr
            from FriendRequest fr
            where fr.status = com.mttauto.momentum_api.friends.entity.FriendRequestStatus.ACCEPTED
              and (
                (fr.sender.id = :leftUserId and fr.receiver.id = :rightUserId)
                or (fr.sender.id = :rightUserId and fr.receiver.id = :leftUserId)
              )
            """)
    Optional<FriendRequest> findAcceptedFriendship(
            @Param("leftUserId") Long leftUserId,
            @Param("rightUserId") Long rightUserId
    );

    @Query("""
            select fr
            from FriendRequest fr
            where fr.status = com.mttauto.momentum_api.friends.entity.FriendRequestStatus.ACCEPTED
              and (fr.sender.id = :userId or fr.receiver.id = :userId)
            order by fr.updatedAt desc
            """)
    List<FriendRequest> findAcceptedFriendships(@Param("userId") Long userId);
}
