package net.northking.cloudtest.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.northking.cloudtest.domain.user.CltRole;
import net.northking.cloudtest.dto.report.UserCountDtoReprot;
import net.northking.cloudtest.dto.user.UserCount;
import net.northking.cloudtest.feign.user.RoleFeignClient;
import net.northking.cloudtest.result.ResultInfo;
import net.northking.cloudtest.service.GenTestTeamService;
import net.northking.cloudtest.utils.WordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:zwy
 * @Despriction:
 * @Date:Create in 14:48 2018/5/21
 * @Modify By:
 */
@Service
public class GenTestTeamServiceImpl implements GenTestTeamService {
    @Autowired
    private RoleFeignClient roleFeignClient;
    @Value("${report.chart.prepath}")
    private String preFilePath;

    @Value("${report.chart.outputJsPath}")
    private String outputJsPath;

    @Override
    public int genTestTeam(String proId) {

        Runtime run = Runtime.getRuntime();

        File dirFile = new File(preFilePath + proId + "/json");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        int num = 0;
        CltRole cltRole = new CltRole();
        cltRole.setProId(proId);
        ResultInfo<UserCount> result= roleFeignClient.selectRoleCount(cltRole);
        UserCount userCount = result.getData();
        if (userCount != null) {
            WordUtil wordUtil = new WordUtil();
            UserCountDtoReprot userCountDtoReprot = get(userCount);
            //放到集合里是因为模板前后有[ ]
            List<UserCountDtoReprot> list = new ArrayList<>();
            list.add(userCountDtoReprot);
            //把集合转成json
            String json = JSONObject.toJSONString(list);
            Map dataMap = new HashMap();
            dataMap.put("xAxis", json);
            wordUtil.createWord("team.json", preFilePath + proId + "/json/testTeam.json", dataMap);
            String progressCmd = "node " + outputJsPath + " " + preFilePath + proId + "/json/testTeam.json " + preFilePath +proId + "/images/testTeam.png";
            try {
                Process p = run.exec(progressCmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            num++;
        }
        return num;
    }


    public UserCountDtoReprot get(UserCount userCount) {
        UserCountDtoReprot userCountDtoReprot = new UserCountDtoReprot();
        String roleName = userCount.getRoleName();
        int count = userCount.getCount();
        userCountDtoReprot.setName(roleName+"("+count+")");
        List<UserCount> children =  userCount.getChildren();
        List<UserCountDtoReprot> list = new ArrayList<>();
        //如果存在children
        if(children.size()>0){
            for (UserCount userCount1:children){
                UserCountDtoReprot userCountDtoReprot1 = get(userCount1);
                list.add(userCountDtoReprot1);
            }

            userCountDtoReprot.setChildren(list);

        }
        return userCountDtoReprot;
    }
}
