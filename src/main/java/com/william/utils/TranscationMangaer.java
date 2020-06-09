package com.william.utils;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * 和事务相关的工具类，  包含了：开启事务，提交事务，回滚事务，释放连接
 */
@Component("transcationMangaer")
@Aspect
public class TranscationMangaer {
    @Pointcut("execution(* com.william.service.impl.*.*(..))")
    private void ptl(){}

    @Autowired
    private ConnectionUtil connectionUtil;


    /**
     * 开启事务
     */
//    @Before("ptl()")
    public void beginTranscation(){
        try {
            connectionUtil.getThreadConnection().setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 提交事务
     */
//    @AfterReturning("ptl()")
    public void commit(){
        try {
            connectionUtil.getThreadConnection().commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 回滚事务
     */
//    @AfterThrowing("ptl()")
    public void rollback(){
        try {
            connectionUtil.getThreadConnection().rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放资源
     */
//    @After("ptl()")
    public void release(){
        try {
            connectionUtil.getThreadConnection().close();
            connectionUtil.removeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Around("ptl()")
    public Object arroundAdvice(ProceedingJoinPoint pjp) {
        Object returnValue = null;
        try {
            //1.获取参数
            Object[] arges = pjp.getArgs();
            //2.开启事务
            this.beginTranscation();
            //3.执行方法
            returnValue = pjp.proceed(arges);
            //4.提交事务
            this.commit();
            return returnValue;
        } catch (Throwable t) {
            //5.回滚事务
            this.rollback();
            throw new RuntimeException(t);
        } finally {
            //6.释放资源
            this.release();
        }
    }
}
