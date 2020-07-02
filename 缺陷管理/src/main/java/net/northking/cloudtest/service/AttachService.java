package net.northking.cloudtest.service;

import net.northking.cloudtest.domain.attach.CltAttach;
import net.northking.cloudtest.query.attach.AttachQuery;

import java.util.List;

/**
 * @Title:
 * @Description: attach实现层
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-03-16 11:09
 * @UpdateUser:
 * @Version:0.1
 */
public interface AttachService {

    //添加附件表
    Integer addCltAttachInfo(AttachQuery attachQuery)throws Exception;

    //查询附件表
    List<CltAttach> queryAllCltAttachList(AttachQuery attachQuery);

    //根据缺陷Id批量删除附件记录表
    Integer deleteCltAttachInfoByBugId(List<AttachQuery> AttachQuery)throws Exception;

    //根据executeId和StepId查询附件表
    List<CltAttach> queryAllCltAttachListByExecuteIdAndStepId(AttachQuery attachQuery) throws Exception;

    //根据executeId查询所有的附件
    List<CltAttach> queryAllCltAttachListByExecuteId(AttachQuery attachQuery) throws Exception;

    //根据executeId和StepId和bugId查询附件表
    List<CltAttach> queryAllCltAttachListByExecuteIdAndStepIdAndBugId(AttachQuery attachQuery) throws Exception;

    // 补全附件路径信息
    List<CltAttach> completeAttachPath(List<CltAttach> attaches);

    /**
     * 条件查询附件
     *
     * @param query
     * @return
     */
    List<CltAttach> queryAttachOnCondition(AttachQuery query);

    /**
     * 添加附件信息
     * @param query
     * @return
     */
    CltAttach addAttach(AttachQuery query);

    /**
     * 条件删除
     * @param query
     * @return 成功删除的记录数量
     */
    Integer deleteAttach(AttachQuery query);
}
