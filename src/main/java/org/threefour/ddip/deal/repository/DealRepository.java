package org.threefour.ddip.deal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.threefour.ddip.deal.domain.Deal;
import org.threefour.ddip.deal.domain.DealStatus;
import org.threefour.ddip.member.domain.Member;
import org.threefour.ddip.product.domain.Product;

import java.util.List;
import java.util.Optional;

public interface DealRepository extends JpaRepository<Deal, Long> {
  int countByProductAndSellerAndDeleteYnFalse(Product product, Member buyer);

  Optional<Deal> findByProductIdAndBuyerIdAndDeleteYnFalse(Long productId, Long memberId);

  int countByProductIdAndDeleteYnFalse(Long productId);

  @Query("SELECT DISTINCT CASE WHEN d.buyer.id = :memberId THEN d.product.id " +
          "WHEN d.seller.id = :memberId THEN d.product.id " +
          "ELSE p.id END " +
          "FROM Deal d " +
          "RIGHT JOIN Product p ON d.product.id = p.id " +
          "WHERE d.buyer.id = :memberId OR d.seller.id = :memberId OR p.seller.id = :memberId")
  List<Long> findProductIdsForUserChats(@Param("memberId") Long memberId);

  Optional<Deal> findByProductIdAndDealStatusAndDeleteYnFalse(Long buyerId, DealStatus dealStatus);

  @Query("select d from Deal d where d.product.id =:productId and d.dealStatus = 'BEFORE_DEAL' order by d.createdAt asc")
  Deal findNextWaitingDeal(@Param("productId") Long productId);

  @Query("SELECT d FROM Deal d WHERE (d.buyer.id = :userId OR d.seller.id = :userId) AND d.dealStatus IN ('IN_PROGRESS', 'PAID')")
  List<Deal> findActiveDealsForUser(@Param("userId") Long userId);
}

