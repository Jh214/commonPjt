package common.commonpjt.user;

import common.commonpjt.user.dto.JoinRequest;
import common.commonpjt.user.dto.LoginRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("loginRequest") LoginRequest loginRequest, HttpSession session, BindingResult bindingResult) {
        String url = userService.login(loginRequest, session, bindingResult);
        return url;
    }

    @GetMapping("/register")
    public String registerForm(Model model, JoinRequest joinRequest) {
        model.addAttribute("joinRequest", joinRequest);
        return "register";
    }

    @PostMapping("/register")
    public String join(@ModelAttribute("joinRequest") JoinRequest request, BindingResult bindingResult) {
        String url = userService.join(request, bindingResult);
        return url;
    }
}
