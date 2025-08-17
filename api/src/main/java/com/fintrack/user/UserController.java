// package com.fintrack.user;

// import com.fintrack.auth.CurrentUser;
// import com.fintrack.auth.UserPrincipal;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RestController;

// import java.util.List;

// @RestController
// public class UserController {

//     @GetMapping("/api/me")
//     public UserMeResponse getCurrentUser(@CurrentUser UserPrincipal user) {
//         List<String> roleNames = user.getAuthorities()
//                 .stream()
//                 .map(granted -> granted.getAuthority())
//                 .toList();

//         return new UserMeResponse(
//                 user.getId(),
//                 user.getFullName(),
//                 user.getEmail(),
//                 roleNames);
//     }
// }

package com.fintrack.user;

import com.fintrack.auth.CurrentUserResolver;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    private final CurrentUserResolver current;

    public UserController(CurrentUserResolver current) {
        this.current = current;
    }

    @GetMapping("/me")
    public CurrentUserResponse getMe(NativeWebRequest web) {
        User user = current.resolve(web);

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        return new CurrentUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                roles);
    }
}
