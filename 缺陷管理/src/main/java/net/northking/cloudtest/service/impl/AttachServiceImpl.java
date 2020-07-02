package net.northking.cloudtest.service.impl;

import net.northking.cloudtest.constants.ErrorConstants;
import net.northking.cloudtest.dao.attach.CltAttachMapper;
import net.northking.cloudtest.domain.attach.CltAttach;
import net.northking.cloudtest.domain.attach.CltAttachExample;
import net.northking.cloudtest.exception.GlobalException;
import net.northking.cloudtest.query.attach.AttachQuery;
import net.northking.cloudtest.service.AttachService;
import net.northking.cloudtest.utils.BeanUtil;
import net.northking.cloudtest.utils.UUIDUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Title:
 * @Description: attach逻辑层
 * @Company: Northking
 * @Author: HBH
 * @CreateDate: 2018-03-16 11:10
 * @UpdateUser:
 * @Version:0.1
 */

@Service
public class AttachServiceImpl implements AttachService {


    private final static Logger logger = LoggerFactory.getLogger(TestBugServiceImpl.class);

    @Autowired
    private CltAttachMapper cltAttachMapper;

    @Value("${defaultXml.host}")
    private String host;

    @Value("${defaultXml.port}")
    private String port;

    @Value("${defaultXml.path}")
    private String path;

    @Value("${atp.attach.url.ip:192.168.0.130}")
    private String fileServerIp;
    @Value("${atp.attach.url.port:9631}")
    private int fileServerPort;
    @Value("${atp.attach.url.path:/download/}")
    private String download;

    //添加附件表
    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public Integer addCltAttachInfo(AttachQuery attachQuery) throws Exception {

        Integer addNum = 0;
        CltAttach cltAttach = new CltAttach();
        BeanUtil.copyProperties(attachQuery, cltAttach);
        try {
            //设置id
            cltAttach.setAttachId(UUIDUtil.getUUID());
            cltAttach.setCreateDate(new Date());
            cltAttach.setOrderSeq(1);//排序默认为1
            addNum = cltAttachMapper.insertSelective(cltAttach);
        } catch (Exception e) {
            logger.info("addCltAttachInfo", e);
            throw new GlobalException(ErrorConstants.ADD_CLT_ATTACH_ERROR_CODE, ErrorConstants.ADD_CLT_ATTACH_ERROR_MESSAGE);
        }
        return addNum;
    }

    /**
     * 根据缺陷id查询所有缺陷所有缺陷
     *
     * @param attachQuery
     * @return
     * @throws Exception
     */
    @Override
    public List<CltAttach> queryAllCltAttachList(AttachQuery attachQuery) {

        String bugId = attachQuery.getBugId();
        List<CltAttach> cltAttaches = null;
        try {
            cltAttaches = cltAttachMapper.queryAllCltAttachListByBugId(bugId);
//            for (int i = 0; i < cltAttaches.size(); i++) {
//                CltAttach cltAttach =  cltAttaches.get(i);
//                String attachPath=cltAttach.getAttachPath();
//               String newAttachPath="/attach/"+attachPath;
//                cltAttach.setAttachPath(newAttachPath);
//            }
            setAttachPath(cltAttaches);
        } catch (Exception e) {
            logger.info("addCltAttachInfo", e);
            throw new GlobalException(ErrorConstants.QUERY_CLT_ATTACH_LIST_ERROR_CODE, ErrorConstants.QUERY_CLT_ATTACH_LIST_ERROR_MESSAGE);
        }
        return cltAttaches;
    }

    //根据缺陷Id批量删除附件记录表
    @Override
    public Integer deleteCltAttachInfoByBugId(List<AttachQuery> attachQueries) throws Exception {


        Integer deleteNum = 0;

        for (int i = 0; i < attachQueries.size(); i++) {

            AttachQuery attachQuery = attachQueries.get(i);


            try {

                Integer deleteCount = cltAttachMapper.deleteCltAttachByBugId(attachQuery.getBugId(), attachQuery.getAttachId());

                if (deleteCount > 0) {

                    deleteNum++;
                }

            } catch (Exception e) {

                logger.info("deleteCltAttachInfoByBugId", e);

                throw new GlobalException(ErrorConstants.DELETE_CLT_ATTACH_LIST_BY_BUG_ID_ERROR_CODE, ErrorConstants.DELETE_CLT_ATTACH_LIST_BY_BUG_ID_ERROR_MESSAGE);

            }

        }


        return deleteNum;
    }

    /**
     * 根据executeId和StepId查询所有的附件列表
     *
     * @param attachQuery
     * @return
     * @throws Exception
     */
    @Override
    public List<CltAttach> queryAllCltAttachListByExecuteIdAndStepId(AttachQuery attachQuery) throws Exception {

        List<CltAttach> cltAttaches = null;
        try {
            cltAttaches = cltAttachMapper.queryAllCltAttachListByExecuteIdAndStepId(attachQuery);
            setAttachPath(cltAttaches);
        } catch (Exception e) {
            logger.info("addCltAttachInfo", e);
            throw new GlobalException(ErrorConstants.QUERY_CLT_ATTACH_LIST_ERROR_CODE, ErrorConstants.QUERY_CLT_ATTACH_LIST_ERROR_MESSAGE);
        }
        return cltAttaches;
    }

    /**
     * 根据executeId查询所有的附件
     *
     * @param attachQuery
     * @return
     * @throws Exception
     */
    @Override
    public List<CltAttach> queryAllCltAttachListByExecuteId(AttachQuery attachQuery) throws Exception {

        //查询附件表
        String executeId = attachQuery.getExecuteId();
        List<CltAttach> cltAttaches = null;
        try {
            cltAttaches = cltAttachMapper.queryAllCltAttachListByExecuteId(executeId);
            setAttachPath(cltAttaches);
        } catch (Exception e) {
            logger.info("addCltAttachInfo", e);
            throw new GlobalException(ErrorConstants.QUERY_CLT_ATTACH_LIST_ERROR_CODE, ErrorConstants.QUERY_CLT_ATTACH_LIST_ERROR_MESSAGE);
        }
        return cltAttaches;
    }


    /**
     * 根据bugId,stepId和bugId查询所有的附件
     *
     * @param attachQuery
     * @return
     * @throws Exception
     */
    @Override
    public List<CltAttach> queryAllCltAttachListByExecuteIdAndStepIdAndBugId(AttachQuery attachQuery) throws Exception {


        String funcId = attachQuery.getFuncId();

        String executeId = attachQuery.getExecuteId();

        String bugId = attachQuery.getBugId();

        List<CltAttach> cltAttaches = null;


        try {

            CltAttachExample cltAttachExample = new CltAttachExample();

            cltAttachExample.setOrderByClause("ORDER_SEQ ASC,CREATE_DATE DESC");

            CltAttachExample.Criteria criteria = cltAttachExample.createCriteria();

            criteria.andFuncIdEqualTo(funcId);

            criteria.andExecuteIdEqualTo(executeId);
            criteria.andBugIdEqualTo(bugId);

            cltAttaches = cltAttachMapper.selectByExample(cltAttachExample);

            setAttachPath(cltAttaches);

        } catch (Exception e) {

            logger.info("addCltAttachInfo", e);
            throw new GlobalException(ErrorConstants.QUERY_CLT_ATTACH_LIST_ERROR_CODE, ErrorConstants.QUERY_CLT_ATTACH_LIST_ERROR_MESSAGE);

        }


        return cltAttaches;
    }

    /**
     * 补全附件路径信息
     *
     * @param attaches
     * @return
     */
    @Override
    public List<CltAttach> completeAttachPath(List<CltAttach> attaches) {
        setAttachPath(attaches);
        return attaches;
    }

    /**
     * 条件查询附件
     *
     * @param query
     * @return
     */
    @Override
    public List<CltAttach> queryAttachOnCondition(AttachQuery query) {
        // stepId转化为funcId
        if (!StringUtils.isEmpty(query.getStepId())) {
            query.setFuncId(query.getStepId());
        }
        List<CltAttach> attaches = cltAttachMapper.queryAttachOnCondition(query);
        setAttachPath(attaches);
        return attaches;
    }

    /**
     * 添加附件信息
     *
     * @param query
     * @return
     */
    @Override
    @Transactional
    public CltAttach addAttach(AttachQuery query) {
        CltAttach attach = new CltAttach();
        try {
            BeanUtils.copyProperties(query, attach);
            logger.info("query.funcId ====>【{}】，attach.funcId =====> 【{}】", query.getFuncId(), attach.getFuncId());
            attach.setCreateDate(new Date());
            attach.setAttachId(UUIDUtil.getUUID());
            cltAttachMapper.insert(attach);
        } catch (Exception e) {
            throw new GlobalException("CLT0000004", "添加附件信息失败");
        }
        String url = String.format("http://%s:%s%s%s", fileServerIp, fileServerPort, download, attach.getAttachPath());
        attach.setAttachPath(url);
        return attach;
    }

    /**
     * 条件删除
     *
     * @param query attachId | executeId | funcId | bugId
     * @return 成功删除的记录数量
     */
    @Override
    @Transactional
    public Integer deleteAttach(AttachQuery query) {
        CltAttachExample example = new CltAttachExample();
        CltAttachExample.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(query.getAttachId())) {
            // 根据attachId
            criteria.andAttachIdEqualTo(query.getAttachId());
        } else if (!StringUtils.isEmpty(query.getBugId())) {
            // 根据bugId删除
            criteria.andBugIdEqualTo(query.getBugId());
        } else if (!StringUtils.isEmpty(query.getExecuteId()) && !StringUtils.isEmpty(query.getStepId())) {
            // 根据 executeId 和 stepId 删除
            criteria.andExecuteIdEqualTo(query.getExecuteId());
            criteria.andFuncIdEqualTo(query.getStepId());
        } else if (!StringUtils.isEmpty(query.getBugId()) && !StringUtils.isEmpty(query.getFuncId())) {
            // 项目附件 projectId -> bugId
            criteria.andBugIdEqualTo(query.getBugId());
            criteria.andFuncIdEqualTo(query.getFuncId());
        } else if (!StringUtils.isEmpty(query.getExecuteId())) {
            // 根据 executeId 删除
            criteria.andExecuteIdEqualTo(query.getExecuteId());
        } else if (query.getIds() != null && query.getIds().size() > 0) {
            // 批量删除
            criteria.andAttachIdIn(query.getIds());
        } else {
            throw new GlobalException("CLT00000004", "缺少条件字段，删除失败");
        }
        int delNum = cltAttachMapper.deleteByExample(example);
        return delNum;
    }

    /**
     * 补全信息
     *
     * @param attaches
     */
    private void setAttachPath(List<CltAttach> attaches) {
        if (attaches != null && attaches.size() > 0) {
            for (CltAttach attach : attaches) {
//                attach.setAttachPath("/attach/" + attach.getAttachPath());
                String url = String.format("http://%s:%s%s%s", fileServerIp, fileServerPort, download, attach.getAttachPath());
                attach.setAttachPath(url);
            }
        }
    }
}
