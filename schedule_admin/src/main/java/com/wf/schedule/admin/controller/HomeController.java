package com.wf.schedule.admin.controller;


import com.alibaba.fastjson.JSONObject;
import com.wf.schedule.admin.model.MenuModel;
import com.wf.schedule.ehcache.EhcacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


@CrossOrigin(maxAge = 3600)
@Controller
@RequestMapping("/schedule/private/home")
public class HomeController extends BaseController {

    @Value("${mp.url}")
    private String mpUrl;
    @Autowired
    private EhcacheManager ehcacheManager;




    @RequestMapping("/loginout")
    @ResponseBody
    public Object loginout(HttpServletRequest request) {
        request.getSession().invalidate();
        return true;
    }

    /**
     * 左侧一级菜单
     *
     * @return
     */
    @RequestMapping("/listModule")
    @ResponseBody
    public Object listModule(@RequestBody JSONObject root) {
        return getMenu(root.getString("root"));
    }

    private Object getMenu(String menuParCode) {
        String longName = "admin";//AssertionHolder.getAssertion().getPrincipal().getName();
        String menuCacheKey = "admin" + "-" + menuParCode;
        String json = null;
//        Object menuJson = ehcacheManager.get(menuCacheKey);
//        if (menuJson != null && StringUtils.isNotBlank(menuJson.toString())) {
//            json = menuJson.toString();
//        } else {
//            json = HttpUtil.sendPost(mpUrl, "loginName=" + "admin" + "&parentMenuCode=" + menuParCode);
//            if (StringUtils.isNotBlank(json)) {
//                ehcacheManager.set(menuCacheKey, json);
//            }
//        }
      //  List<TreeModel> list = GfJsonUtil.parseArray(json, TreeModel.class);
       List<MenuModel> maps = new ArrayList<>();
//        for (TreeModel tm : list) {
//            MenuModel mm = new MenuModel();
//            mm.setId(tm.getId().intValue());
//            mm.setCode(tm.getCode());
//            mm.setText(tm.getText());
//            mm.setExpanded(tm.isExpanded());
//            mm.setIcon(tm.getIcon());
//            mm.setLeaf(tm.isLeaf());
//            mm.setModuleLink(tm.getModuleLink());
//            maps.add(mm);
//        }
        // 写一个自己的菜单，看一下

        MenuModel mm = new MenuModel();
        mm.setId(1);
        mm.setCode("123");
        mm.setText("任务管理");
        mm.setExpanded(true);
        mm.setIcon(null);
        mm.setLeaf(true);
        mm.setModuleLink("JDD.schedule.scheduleMain");
        maps.add(mm);

        //
        return maps;
    }

    /**
     * 左侧二级菜单树
     *
     * @return
     */
    @RequestMapping("/listMenu")
    @ResponseBody
    public Object listMenu(String code) {
        return getMenu(code);
    }

}
