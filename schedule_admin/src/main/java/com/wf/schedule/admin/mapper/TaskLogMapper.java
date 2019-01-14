package com.wf.schedule.admin.mapper;

import com.wf.schedule.admin.po.TaskLogPo;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface TaskLogMapper {
    @Transactional
    List<TaskLogPo> listTaskLog(Map<String, Object> params);

    @Transactional
    void deleteTaskResultList(@Param("groupName") String groupName, @Param("jobName") String jobName);

    @Transactional
    void saveTaskLog(TaskLogPo logPo);
}
