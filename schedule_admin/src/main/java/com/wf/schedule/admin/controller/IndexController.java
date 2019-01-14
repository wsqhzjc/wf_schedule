package com.wf.schedule.admin.controller;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.util.AssertionHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by pdl on 2017/6/27.
 */
@Controller
public class IndexController {
    @RequestMapping("/")
    public String index(HttpServletRequest request, Model model) {
        String loginName = "admin";//AssertionHolder.getAssertion().getPrincipal().getName();
        request.getSession().setAttribute("userName", loginName);
        model.addAttribute("userName", loginName);
        return "default";
    }
}
