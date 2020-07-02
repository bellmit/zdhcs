package net.northking.atp.util;

import net.northking.atp.db.persistent.*;
import net.northking.atp.entity.InterfaceComponentInfo;
import net.northking.atp.entity.InterfaceInterfaceInfo;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2019/6/5 0005.
 */
public class TargetTransformTools {
    /**
     * md表组件参数转换为re表参数
     * @param paramList
     * @return
     */
    public List<ReComponentParameter> transModifyToFormal(List<MdComponentParameter> paramList){
        List<ReComponentParameter> result =  new ArrayList<ReComponentParameter>();
        for(MdComponentParameter one : paramList){
            ReComponentParameter reParam = new ReComponentParameter();
            reParam.setId(one.getId());
            reParam.setComponentId(one.getComponentId());
            reParam.setParameterType(one.getParameterType());
            reParam.setParameterFlag(one.getParameterFlag());
            reParam.setParameterOrder(one.getParameterOrder());
            reParam.setElementType(one.getElementType());
            reParam.setModifyStaff(one.getModifyStaff());
            reParam.setModifyTime(one.getModifyTime());
            reParam.setProjectId(one.getProjectId());
            reParam.setParameterName(one.getParameterName());
            reParam.setParameterComment(one.getParameterComment());
            reParam.setDefaultValue(one.getDefaultValue());
            reParam.setRequired(one.getRequired());
            reParam.setRunComponentId(one.getRunComponentId());
            reParam.setRunComponentOrder(one.getRunComponentOrder());
            reParam.setInOrOut(one.getInOrOut());
            result.add(reParam);
        }
        return result;
    }

    /**
     * md接口信息对象转换为自定义接口信息对象
     * @param info
     * @return
     */
    public InterfaceInterfaceInfo transMdInterfaceToInterface(MdInterfaceInfo info){
        InterfaceInterfaceInfo result = new InterfaceInterfaceInfo();
        if(info != null){
            result.setId(info.getId());
            result.setInterfaceName(info.getInterfaceName());
            result.setMethod(info.getMethod());
            result.setUrl(info.getUrl());
            result.setDescription(info.getDescription());
            result.setProjectId(info.getProjectId());
            result.setVersion(info.getVersion());
            result.setCreateTime(info.getCreateTime());
            result.setCreateStaff(info.getCreateStaff());
        }
        return result;
    }

    /**
     * 历史组件参数信息转换为md组件参数对象
     * @param hisParam
     * @return
     */
    public ReComponentParameter transHisParamToRe(HisComponentParameter hisParam){
        ReComponentParameter reParam = new ReComponentParameter();
        if(hisParam != null){
            reParam.setId(hisParam.getReId());
            reParam.setComponentId(hisParam.getComponentId());
            reParam.setParameterType(hisParam.getParameterType());
            reParam.setParameterFlag(hisParam.getParameterFlag());
            reParam.setParameterOrder(hisParam.getParameterOrder());
            reParam.setElementType(hisParam.getElementType());
            reParam.setModifyStaff(hisParam.getModifyStaff());
            reParam.setModifyTime(hisParam.getModifyTime());
            reParam.setProjectId(hisParam.getProjectId());
            reParam.setParameterName(hisParam.getParameterName());
            reParam.setParameterComment(hisParam.getParameterComment());
            reParam.setDefaultValue(hisParam.getDefaultValue());
            reParam.setRequired(hisParam.getRequired());
            reParam.setRunComponentId(hisParam.getRunComponentId());
            reParam.setRunComponentOrder(hisParam.getRunComponentOrder());
            reParam.setInOrOut(hisParam.getInOrOut());
        }
        return reParam;
    }

    /**
     * MD组件参数信息转换为rE组件参数对象
     * @param mdParam
     * @return
     */
    public ReComponentParameter transMdParamToRe(MdComponentParameter mdParam){
        ReComponentParameter reParam = new ReComponentParameter();
        if(mdParam != null){
            reParam.setId(mdParam.getId());
            reParam.setComponentId(mdParam.getComponentId());
            reParam.setParameterType(mdParam.getParameterType());
            reParam.setParameterFlag(mdParam.getParameterFlag());
            reParam.setParameterOrder(mdParam.getParameterOrder());
            reParam.setElementType(mdParam.getElementType());
            reParam.setModifyStaff(mdParam.getModifyStaff());
            reParam.setModifyTime(mdParam.getModifyTime());
            reParam.setProjectId(mdParam.getProjectId());
            reParam.setParameterName(mdParam.getParameterName());
            reParam.setParameterComment(mdParam.getParameterComment());
            reParam.setDefaultValue(mdParam.getDefaultValue());
            reParam.setRequired(mdParam.getRequired());
            reParam.setRunComponentId(mdParam.getRunComponentId());
            reParam.setRunComponentOrder(mdParam.getRunComponentOrder());
            reParam.setInOrOut(mdParam.getInOrOut());
        }
        return reParam;
    }

    /**
     * md组件信息对象转换为自定义组件信息对象
     * @param mdInfo
     * @return
     */
    public InterfaceComponentInfo tranMdComInfoToInter(MdComponentInfo mdInfo){
        InterfaceComponentInfo info = new InterfaceComponentInfo();
        if(mdInfo != null){
            info.setId(mdInfo.getId());
            info.setComponentNo(mdInfo.getComponentNo());
            info.setComponentName(mdInfo.getComponentName());
            info.setDataName(mdInfo.getDataName());
            info.setCaseType(mdInfo.getCaseType());
            info.setDescription(mdInfo.getDescription());
            info.setProjectId(mdInfo.getProjectId());
            info.setVersion(mdInfo.getVersion());
            info.setComponentClass(mdInfo.getComponentClass());
            info.setComponentFlag(mdInfo.getComponentFlag());
            info.setLibraryId(mdInfo.getLibraryId());
            info.setCreateStaff(mdInfo.getCreateStaff());
            info.setCreateTime(mdInfo.getCreateTime());
            info.setModifyStaff(mdInfo.getModifyStaff());
            info.setModifyTime(mdInfo.getModifyTime());
            info.setIsValid(mdInfo.getIsValid());
            info.setParameterNumber(mdInfo.getParameterNumber());
            info.setIsDeprecated(mdInfo.getIsDeprecated());
            info.setComplexity(mdInfo.getComplexity());
        }
        return info;
    }

    /**
     * md组件信息对象转换为自定义组件信息对象
     * @param hisInfo
     * @return
     */
    public InterfaceInterfaceInfo transHisInterfaceToInterface(HisInterfaceInfo hisInfo){
        InterfaceInterfaceInfo info =new InterfaceInterfaceInfo();
        if(hisInfo != null){
            info.setId(hisInfo.getId());
            info.setInterfaceName(hisInfo.getInterfaceName());
            info.setMethod(hisInfo.getMethod());
            info.setUrl(hisInfo.getUrl());
            info.setDescription(hisInfo.getDescription());
            info.setProjectId(hisInfo.getProjectId());
            info.setVersion(hisInfo.getVersion());
            info.setCreateTime(hisInfo.getCreateTime());
            info.setCreateStaff(hisInfo.getCreateStaff());
        }
        return info;
    }
}
