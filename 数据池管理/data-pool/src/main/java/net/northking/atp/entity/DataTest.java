package net.northking.atp.entity;

import net.northking.atp.commons.http.ResultWrapper;
import net.northking.atp.element.core.Library;
import net.northking.atp.rule.core.rule.RuleArgument;
import net.northking.atp.rule.core.rule.RuleInfo;
import net.northking.atp.rule.core.rule.RuleReturn;
import net.northking.atp.rule.core.type.DataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2019/7/16 0016.
 */
public class DataTest {
    public void getData(List<RuleInfo> keyList){

        RuleInfo one = new RuleInfo();
        one.setDeprecated(true);
        one.setRuleDocumentation("规则一：随机数生成");
        one.setRuleAlias("随机数");
        one.setRuleName("random");
        //输出
        RuleReturn reOne = new RuleReturn();
        reOne.setName("money");
        reOne.setComment("金额");
        reOne.setDefaultValue("0");
        reOne.setRequired(true);
        reOne.setType(DataType.STRING);
        one.setRuleReturn(reOne);
        int i = 0;
        //输入
        Object[] oneStr1 = {"min","最低","0"};
        Object[] oneStr2 = {"max","最高"};
        List<Object[]> stList1 = new ArrayList<Object[]>();
        stList1.add(oneStr1);stList1.add(oneStr2);
        List<RuleArgument> inList1 = getParam(stList1);
        one.setRuleArguments(inList1);


        RuleInfo two = new RuleInfo();
        two.setDeprecated(true);
        two.setRuleDocumentation("规则二：账号生成");
        two.setRuleAlias("账号");
        two.setRuleName("getUser");
        //输出
        RuleReturn reTwo = new RuleReturn();
        reTwo.setName("userName");
        reTwo.setComment("账号");
        reTwo.setRequired(false);
        reTwo.setType(DataType.STRING);
        two.setRuleReturn(reTwo);
        //输入
        Object[] twoStr1 = {"length","长度","6"};
        Object[] twoStr2 = {"content","数字/字母/混合","1"};
        List<Object[]> stList2 = new ArrayList<Object[]>();
        stList2.add(twoStr1);stList2.add(twoStr2);
        List<RuleArgument> inList2 = getParam(stList2);
        two.setRuleArguments(inList2);
        keyList.add(one);
        keyList.add(two);
    }


    private List<RuleArgument> getParam(List<Object[]> arg){
        List<RuleArgument> inList = new ArrayList<RuleArgument>();
        for(Object[] one : arg){
            RuleArgument key = new RuleArgument();
            key.setName(one[0]+"");
            key.setComment(one[1]+"");
            if(one.length>2){
                key.setDefaultValue(one[2]+"");
                key.setRequired(true);
            }else{
                key.setRequired(false);
            }
            key.setType(DataType.STRING);
            inList.add(key);
        }
        return inList;
    }
}
