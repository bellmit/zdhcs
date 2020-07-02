package net.northking.atp.util;

/**
 * Created by Administrator on 2019/7/30 0030.
 */

import java.util.ArrayList;
import java.util.List;

/**
 * 常量定义
 */
public class Constant {
    /** 调试_自定义projectId**/
    public static final String interDebug = "DEBUG01";
    /** 组件标识-0：基础组件*/
    public static final String comFlag_basic = "0";
    /** 组件标识-1：业务组件*/
    public static final String comFlag_Business = "1";
    /** 组件标识-2：默认组件*/
    public static final String comFlag_default = "2";
    /** 组件标识-0：接口组件*/
    public static final String comFlag_interface = "3";
    /** 组件标识-0：智能接口*/
    public static final String comFlag_smartPort = "4";
    /** 智能接口默认组件库*/
    //public static final String smartPortLib = "NK.SmartPort";
    public static final String smartPortLib = "NK.HttpRequest";


    /** 调试_相关表名集合 **/
    public static final List<String> tableList = new ArrayList<String>(){
        {
            //组件MD表 0-2
            add("MD_COMPONENT_INFO");     //no.0
            add("MD_COMPONENT_PACKAGE");  //no.1
            add("MD_COMPONENT_PARAMETER");//no.2
            //组件RE表 3-5
            add("RE_COMPONENT_INFO");     //no.3
            add("RE_COMPONENT_PACKAGE");  //no.4
            add("RE_COMPONENT_PARAMETER");//NO.5
            //案例MD表 6-9
            add("MD_CASE_DESIGN_INFO");  //NO.6
            add("MD_CASE_STEP");         //NO.7
            add("MD_COMPONENT_STEP");    //NO.8
            add("MD_STEP_PARAMETER");    //NO.9
            //案例RE表 10-13
            add("RE_CASE_DESIGN_INFO");  //NO.10
            add("RE_CASE_STEP");         //NO.11
            add("RE_COMPONENT_STEP");    //NO.12
            add("RE_STEP_PARAMETER");    //NO.13
            //接口MD表 14-15
            add("MD_INTERFACE_INFO");    //NO.14
            add("MD_INTERFACE_DATA");    //NO.15
            //案例集RE表 16-17
            add("RE_CASE_SET");         //NO.16
            add("RE_CASE_SET_LINK");    //no.17
        }
    };
}
