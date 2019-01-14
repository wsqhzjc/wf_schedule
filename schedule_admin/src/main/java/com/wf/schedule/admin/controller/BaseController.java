package com.wf.schedule.admin.controller;

import com.wf.schedule.admin.model.ExtModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @description：基础 controller
 * @author：pdl
 * @date：2015/10/1 14:51
 */
public abstract class BaseController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @InitBinder
    public void initBinder(ServletRequestDataBinder binder) {
        /**
         * 自动转换日期类型的字段格式
         */
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), true));
    }


    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public Object handlerThrowable(Throwable tr) {
        ExtModel em = new ExtModel();
        em.setSuccess(false);
        em.setData(tr.getMessage());
        tr.printStackTrace();
        return em;
    }


}
