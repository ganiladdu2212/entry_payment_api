package com.enty.payment.user.controller;
import com.enty.payment.customer.response.CustomerApiResponse;
import com.enty.payment.user.request.SaveUserSubscriptionsRequest;
import com.enty.payment.user.response.UserSubscriptionResponse;
import com.enty.payment.user.service.UserSubscriptionService;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/users")
public class UserSubscriptionController {
    private final UserSubscriptionService service;
    public UserSubscriptionController(UserSubscriptionService service) { this.service=service; }
    @PostMapping("/saveUserSubscriptions")
    public CustomerApiResponse<UserSubscriptionResponse> save(@RequestBody SaveUserSubscriptionsRequest request, Principal principal) {
        var result=service.save(request,principal.getName());
        return CustomerApiResponse.success("User subscription "+
            ("CREATE".equalsIgnoreCase(request.getTypeOfMode().trim()) ? "created" : "updated")+" successfully",result);
    }
    @GetMapping("/getUser/{custId}")
    public CustomerApiResponse<java.util.List<UserSubscriptionResponse>> getByCustomer(@PathVariable Long custId, Principal principal) {
        return CustomerApiResponse.success("Users fetched successfully", service.getByCustomer(custId, principal.getName()));
    }
    @GetMapping("/getUserSubscription/{userId}")
    public CustomerApiResponse<UserSubscriptionResponse> get(@PathVariable Long userId,Principal principal) {
        return CustomerApiResponse.success("User subscription fetched successfully",service.get(userId,principal.getName()));
    }
}
