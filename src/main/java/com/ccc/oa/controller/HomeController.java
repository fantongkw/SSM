package com.ccc.oa.controller;

import com.ccc.oa.Exception.CustomException;
import com.ccc.oa.model.Member;
import com.ccc.oa.model.Notice;
import com.ccc.oa.security.CurrentUser;
import com.ccc.oa.service.NoticeService;
import com.ccc.oa.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;

@Controller
public class HomeController {
    private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);

    private final UserService userService;
    private final NoticeService noticeService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public HomeController(UserService userService, NoticeService noticeService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.noticeService = noticeService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping(value = {"/", "/home"})
    public String index(){
        return "/home";
    }

    @GetMapping(value = "/login")
    public String login(@CurrentUser User user){
        if (user != null) {
            return "redirect:/home";
        }
        return "/login";
    }

    @GetMapping(value = "/login-error")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "/login";
    }

    @GetMapping(value = "/login-logout")
    public String logout(Model model) {
        model.addAttribute("logout", true);
        return "/login";
    }

    @GetMapping(value = "/notice")
    public String notice(Model model){
        List<Notice> res = noticeService.getAll();
        res.sort(Comparator.comparingLong(Notice::getCreated));
        model.addAttribute("notices", res);
        return "/notice";
    }

    @GetMapping(value = "/register")
    public String register(@CurrentUser User user) {
        if (user != null) {
            return "redirect:/home";
        }
        return "/register";
    }

    @PostMapping(value = "/registered")
    public String registered(@Validated Member member, BindingResult result, Model model, HttpServletRequest request) {
        if (result.hasErrors()) {
            List<ObjectError> errorList = result.getAllErrors();
            throw new CustomException(errorList.toString());
        }
        String password = member.getPassword();
        int success = userService.insert(member);
        try{
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(member.getUsername(), password);
            token.setDetails(new WebAuthenticationDetails(request));
            Authentication authentication = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
        } catch(AuthenticationException e){
            LOG.error("Authentication exception!",e);
            model.addAttribute("error", true);
            model.addAttribute("errorMsg", "服务器繁忙，请稍后重试");
            return "/register";
        }
        if (success == 1) {
            return "redirect:/home";
        } else {
            model.addAttribute("error", true);
            model.addAttribute("errorMsg", "用户已经存在");
            return "/register";
        }
    }

    @GetMapping(value = "/reset_password")
    public String resetPassword() {
        return "/reset_password";
    }

    @GetMapping(value = "test")
    public String test() { return "/test"; }

}
