package com.enty.payment.user.repository;
import com.enty.payment.user.entity.MemberUser;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MemberUserRepository extends JpaRepository<MemberUser, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths={"customer", "subscription", "subscription.membershipPlan", "subscription.trainingPlan"})
    java.util.List<MemberUser> findByCustomerCustIdOrderByCreatedDateDescUserIdDesc(Long custId);
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths={"customer", "subscription", "subscription.membershipPlan", "subscription.trainingPlan"})
    java.util.List<MemberUser> findAllByCustomerCustIdAndMobileNumber(Long custId, String mobileNumber);
}
