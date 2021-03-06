package tacos.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import tacos.Order;
import tacos.configuration.OrderProps;
import tacos.data.OrderJPARepository;
import tacos.data.OrderRepository;
import tacos.security.User;
import tacos.security.UserRepository;

import javax.validation.Valid;
import java.awt.print.Pageable;
import java.security.Principal;

@Slf4j
@Controller
@RequestMapping("/orders")
@SessionAttributes("order")
public class OrderController {

//    private OrderRepository orderRepo;
    private OrderJPARepository orderRepo;
    private UserRepository userRepository;
    private OrderProps orderProps;

//    public OrderController(OrderRepository orderRepo) {
//        this.orderRepo = orderRepo;
//    }
    public OrderController(OrderJPARepository orderRepo,
                           UserRepository userRepository,
                           OrderProps orderProps) {
        this.orderRepo = orderRepo;
        this.userRepository = userRepository;
        this.orderProps = orderProps;
    }

    @GetMapping
    public String ordersForUser( @AuthenticationPrincipal User user, Model model) {
        var pageable = PageRequest.of(0, orderProps.getPageSize());

        model.addAttribute("orders",
                orderRepo.findByUserOrderByPlacedAtDesc(user, pageable));

        return "orderList";
    }

    @GetMapping("/current")
    public String orderForm(Model model){
        model.addAttribute("order", new Order());
        return "orderForm";
    }

    @PostMapping
    public String processOrder(@Valid Order order, Errors errors,
                               SessionStatus sessionStatus,
                               Principal principal,
                               Authentication authentication,
                               @AuthenticationPrincipal User user) {
        if (errors.hasErrors())
            return "orderForm";

//        var user = userRepository.findByUsername(principal.getName());
//        user = (User) authentication.getPrincipal();
//        Authentication authentication1 = SecurityContextHolder.getContext().getAuthentication();
//        user = (User) authentication1.getPrincipal();
        order.setUser(user);

        orderRepo.save(order);
        log.info("Order submitted: " + order);
        sessionStatus.setComplete();
        return "redirect:/";
    }
}
