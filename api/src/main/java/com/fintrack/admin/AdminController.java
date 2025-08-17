package com.fintrack.admin;

import com.fintrack.admin.dto.AdminOverview;
import com.fintrack.auth.CurrentUserResolver;
import com.fintrack.user.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CurrentUserResolver current;
    private final AdminService service;

    public AdminController(CurrentUserResolver current, AdminService service) {
        this.current = current;
        this.service = service;
    }

    @GetMapping("/overview")
    public AdminOverview overview(NativeWebRequest web) {
        User me = current.resolve(web);
        return service.getOverview(me);
    }
}
