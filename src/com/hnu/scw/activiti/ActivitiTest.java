package com.hnu.scw.activiti;

import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author scw
 * @create 2018-01-15 11:04
 * @desc 用于进行演示Activiti的首例程序，即描述如何在代码中实现学生进行请假申请，班主任审核，教务处审核
 **/
public class ActivitiTest {

    /**
     * 1、部署流程
     * 2、启动流程实例
     * 3、请假人发出请假申请
     * 4、班主任查看任务
     * 5、班主任审批
     * 6、最终的boss审批
     */
    /**
     * 1：部署一个Activiti流程
     * 运行成功后，查看之前的数据库表，就会发现多了很多内容
     */
    @Test
    public void creatActivitiTask(){
        //加载的那两个内容就是我们之前已经弄好的基础内容哦。
        //得到了流程引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        processEngine.getRepositoryService()
                .createDeployment()
                .name("请假流程")
                .addClasspathResource("shenqing.bpmn")
                .addClasspathResource("shenqing.png")
                .deploy();
    }
    /**
     * 2：启动流程实例
     */
    @Test
    public void testStartProcessInstance(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
//        Map<String, Object> variables = new HashMap<String, Object>();
//        variables.put("user", "周江霄");
//        processEngine.getRuntimeService()
//                .startProcessInstanceById("myProcess_1:4:1004",variables);  //这个是查看数据库中act_re_procdef表  shenqing:2:104
        processEngine.getRuntimeService()
                .startProcessInstanceById("myProcess_1:1:4");  //这个是查看数据库中act_re_procdef表  shenqing:2:104
    }
    /**
     * 完成请假申请
     */
    @Test
    public void testQingjia(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
//        processEngine.getTaskService()
//                .complete("702"); //查看act_ru_task表

        Map<String, Object> variables = new HashMap<String, Object>();
        //其中message对应test.bpmn中的${message=='bzy'}，不重要对应流程变量的值
        variables.put("person", "季忠诚");
//        variables.put("message", "bzy");
//        variables.put("message", "aaaaaaaaaaaaa");//如果是随便设置一个值，流程会默认走一个分支
        processEngine.getTaskService()
                .complete("1205",variables); //如果连线设置了条件，variables这个参数必填，否则报错
    }

    /**
     * 小明学习的班主任小毛查询当前正在执行任务
     */
    @Test
    public void testQueryTask(){
        //下面代码中的小毛，就是我们之前设计那个流程图中添加的班主任内容
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        List<Task> tasks = processEngine.getTaskService()
                .createTaskQuery()
//                .executionId("501")
//                .taskId("1002")
                .taskAssignee("小毛")
                .list();
        for (Task task : tasks) {
            System.out.println(task.getName());
            System.out.println(task.getAssignee());
        }
    }

    /**
     * 班主任小毛完成任务
     */
    @Test
    public void testFinishTask_manager(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        engine.getTaskService()
                .complete("1302"); //查看act_ru_task数据表
    }

    /**
     * 教务处的大毛完成的任务
     */
    @Test
    public void testFinishTask_Boss(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        processEngine.getTaskService()
                .complete("1102");  //查看act_ru_task数据表
    }

    /**
     * 查看已经完成的任务和当前在执行的任务
     */
    @Test
    public void findHistoryTask() {
        ProcessEngine defaultProcessEngine = ProcessEngines.getDefaultProcessEngine();
        //如果只想获取到已经执行完成的，那么就要加入completed这个过滤条件
        List<HistoricTaskInstance> historicTaskInstances1 = defaultProcessEngine.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .taskDeleteReason("completed")
                .list();
        //如果只想获取到已经执行完成的，那么就要加入completed这个过滤条件
        List<HistoricTaskInstance> historicTaskInstances2 = defaultProcessEngine.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .list();
        System.out.println("执行完成的任务：" + historicTaskInstances1.size());
        System.out.println("所有的总任务数（执行完和当前未执行完）：" + historicTaskInstances2.size());
    }

    /* 查询流程定义 */
    @Test
    public void findProcessDefinition() {
        ProcessEngine defaultProcessEngine = ProcessEngines.getDefaultProcessEngine();
        List<ProcessDefinition> list = defaultProcessEngine.getRepositoryService()// 与流程定义和部署对象相关的Service
                .createProcessDefinitionQuery()// 创建一个流程定义的查询
                /** 指定查询条件,where条件 */
                 //.deploymentId("101")//使用部署对象ID查询
                // .processDefinitionId(processDefinitionId)//使用流程定义ID查询
                // .processDefinitionKey(processDefinitionKey)//使用流程定义的key查询
                // .processDefinitionNameLike(processDefinitionNameLike)//使用流程定义的名称模糊查询

                /** 排序 */
                .orderByProcessDefinitionVersion().asc()// 按照版本的升序排列
                // .orderByProcessDefinitionName().desc()//按照流程定义的名称降序排列

                /** 返回的结果集 */
                .list();// 返回一个集合列表，封装流程定义
        // .singleResult();//返回惟一结果集
        // .count();//返回结果集数量
        // .listPage(firstResult, maxResults);//分页查询
        if (list != null && list.size() > 0) {
            for (ProcessDefinition pd : list) {
                System.out.println("流程定义ID:" + pd.getId());// 流程定义的key+版本+随机生成数
                System.out.println("流程定义的名称:" + pd.getName());// 对应helloworld.bpmn文件中的name属性值
                System.out.println("流程定义的key:" + pd.getKey());// 对应helloworld.bpmn文件中的id属性值
                System.out.println("流程定义的版本:" + pd.getVersion());// 当流程定义的key值相同的相同下，版本升级，默认1
                System.out.println("资源名称bpmn文件:" + pd.getResourceName());
                System.out.println("资源名称png文件:" + pd.getDiagramResourceName());
                System.out.println("部署对象ID：" + pd.getDeploymentId());
                System.out
                        .println("#########################################################");
            }
        }
    }

    /* 查询流程状态（判断流程正在执行，还是结束） */
    @Test
    public void isProcessEnd() {
        ProcessEngine defaultProcessEngine = ProcessEngines.getDefaultProcessEngine();
        String processInstanceId = "501";
        ProcessInstance pi = defaultProcessEngine.getRuntimeService()// 表示正在执行的流程实例和执行对象
                .createProcessInstanceQuery()// 创建流程实例查询
                .processInstanceId(processInstanceId)// 使用流程实例ID查询
                .singleResult();
        if (pi == null) {
            System.out.println("流程已经结束");
        } else {
            System.out.println("流程没有结束");
        }
    }

    //驳回到上一节点，
    // 思路：
    //由于是驳回到上一个节点，将当前activity的outgoing出口清空，
    // 从历史中找到最后完成的activity，然后把当前出口指向
    // 最后完成的activity。complete task，任务就会回到上一个节点中。
    // 最后再把出口改回去即可。

    @Test
    public void reject() {

        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        HistoryService historyService = processEngine.getHistoryService();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        String taskId = "1702";//这里根据你自己的taskid来写
        Map variables = new HashMap<>();

        //获取当前任务
        HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .singleResult();
        //获取流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(currTask.getProcessInstanceId())
                .singleResult();
        //获取流程定义
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(currTask.getProcessDefinitionId());
        if (processDefinitionEntity == null) {
            System.out.println("不存在的流程定义。");

        }

        //获取当前activity
        ActivityImpl currActivity = ((ProcessDefinitionImpl) processDefinitionEntity)
                .findActivity(currTask.getTaskDefinitionKey());

        //获取当前任务流入
        List<PvmTransition> histTransitionList = currActivity
                .getIncomingTransitions();


        //清除当前活动出口
        List<PvmTransition> originPvmTransitionList = new ArrayList<PvmTransition>();
        List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
        for (PvmTransition pvmTransition : pvmTransitionList) {
            originPvmTransitionList.add(pvmTransition);
        }
        pvmTransitionList.clear();

        //查找上一个user task节点
        List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery().activityType("userTask")
                .processInstanceId(processInstance.getId())
                .finished()
                .orderByHistoricActivityInstanceEndTime().desc().list();
        TransitionImpl transitionImpl = null;
        if (historicActivityInstances.size() > 0) {
            ActivityImpl lastActivity = ((ProcessDefinitionImpl) processDefinitionEntity)
                    .findActivity(historicActivityInstances.get(0).getActivityId());
            //创建当前任务的新出口
            transitionImpl = currActivity.createOutgoingTransition(lastActivity.getId());
            transitionImpl.setDestination(lastActivity);
        }else
        {
            System.out.println("上级节点不存在。");
        }
        variables = processInstance.getProcessVariables();
        // 完成任务
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskDefinitionKey(currTask.getTaskDefinitionKey()).list();
        for (Task task : tasks) {
            taskService.complete(task.getId(), variables);
            //这个一定要删除，如：A》B》C  ，从C驳回到B，如果不删除，从B驳回会到C
            //如果后期想查看这些数据在删除之前可以保存到另外的表里，表结构和这个相同
            historyService.deleteHistoricTaskInstance(task.getId());
        }

        // 恢复方向
        currActivity.getOutgoingTransitions().remove(transitionImpl);

        for (PvmTransition pvmTransition : originPvmTransitionList) {
            pvmTransitionList.add(pvmTransition);
        }
    }


    /**
     * 驳回到最初任务
     * 道理相同，只是选择历史中最先完成的user task。
     */
    @Test
    public void rejectToTop() {

        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        HistoryService historyService = processEngine.getHistoryService();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        String taskId = "1802";
        Map variables = new HashMap<>();

        //获取当前任务
        HistoricTaskInstance currTask = historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .singleResult();
        //获取流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(currTask.getProcessInstanceId())
                .singleResult();
        //获取流程定义
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                .getDeployedProcessDefinition(currTask.getProcessDefinitionId());
        if (processDefinitionEntity == null) {
            System.out.println("不存在的流程定义。");

        }

        //获取当前activity
        ActivityImpl currActivity = ((ProcessDefinitionImpl) processDefinitionEntity)
                .findActivity(currTask.getTaskDefinitionKey());

        //获取当前任务流入
        List<PvmTransition> histTransitionList = currActivity
                .getIncomingTransitions();


        //清除当前活动出口
        List<PvmTransition> originPvmTransitionList = new ArrayList<PvmTransition>();
        List<PvmTransition> pvmTransitionList = currActivity.getOutgoingTransitions();
        for (PvmTransition pvmTransition : pvmTransitionList) {
            originPvmTransitionList.add(pvmTransition);
        }
        pvmTransitionList.clear();

        //查找上一个user task节点
        List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery().activityType("userTask")
                .processInstanceId(processInstance.getId())
                .finished()
                .orderByHistoricActivityInstanceEndTime().asc().list();
        TransitionImpl transitionImpl = null;
        if (historicActivityInstances.size() > 0) {
            ActivityImpl lastActivity = ((ProcessDefinitionImpl) processDefinitionEntity)
                    .findActivity(historicActivityInstances.get(0).getActivityId());
            //创建当前任务的新出口
            transitionImpl = currActivity.createOutgoingTransition(lastActivity.getId());
            transitionImpl.setDestination(lastActivity);
        }else
        {
            System.out.println("上级节点不存在。");
        }
        variables = processInstance.getProcessVariables();
        // 完成任务
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .taskDefinitionKey(currTask.getTaskDefinitionKey()).list();
        for (Task task : tasks) {
            taskService.complete(task.getId(), variables);
            //这个一定要删除，如：A》B》C  ，从C驳回到B，如果不删除，从B驳回会到C
            //如果后期想查看这些数据在删除之前可以保存到另外的表里，表结构和这个相同
            historyService.deleteHistoricTaskInstance(task.getId());
        }

        // 恢复方向
        currActivity.getOutgoingTransitions().remove(transitionImpl);

        for (PvmTransition pvmTransition : originPvmTransitionList) {
            pvmTransitionList.add(pvmTransition);
        }

    }

    /**
     *
     *
     *<p>Description:查询任务办理记录</p>
     *
     * @author：SongJia
     *
     * @date: 2017-3-22上午8:54:01
     *
     */

    /**
     * 查询任务办理记录
     */
    @Test
    public void queryHistoryPersonTask(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        String processInstanceId = "601";
        List<HistoricTaskInstance> list = engine.getHistoryService().createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByProcessInstanceId().desc()
                .list();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        if(list!=null){
            for (HistoricTaskInstance historicTaskInstance : list) {
                System.out.print(historicTaskInstance.getId()+">>>>>>>>");
                System.out.print(historicTaskInstance.getName()+">>>>>>>>");
                System.out.print(historicTaskInstance.getAssignee()+">>>>>>>>");
                System.out.print(historicTaskInstance.getStartTime()+">>>>>>>>");
                System.out.print(historicTaskInstance.getEndTime()+">>>>>>>>");
//                System.out.print(format.format(historicTaskInstance.getStartTime())+">>>>>>>>");
//                System.out.print(format.format(historicTaskInstance.getEndTime())+">>>>>>>>");
                System.out.println(historicTaskInstance.getExecutionId()+">>>>>>>>");
            }
        }

    }

    /**组任务，查询我的个人任务,没有执行结果*/
    @Test
    public void findPersonalTaskList() {
        // 任务办理人
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        String assignee = "AA";
        List<Task> list = processEngine.getTaskService()//
                .createTaskQuery()//
                .taskAssignee(assignee)// 个人任务的查询
                .list();
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID：" + task.getId());
                System.out.println("任务的办理人：" + task.getAssignee());
                System.out.println("任务名称：" + task.getName());
                System.out.println("任务的创建时间：" + task.getCreateTime());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("#######################################");
            }
        }
    }

    /**查询组任务*/
    @Test
    public void findGroupTaskList() {
        // 任务办理人
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        String candidateUser = "AA";
        List<Task> list = processEngine.getTaskService()//
                .createTaskQuery()//
                .taskCandidateUser(candidateUser)// 参与者，组任务查询
                .list();
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID：" + task.getId());
                System.out.println("任务的办理人：" + task.getAssignee());
                System.out.println("任务名称：" + task.getName());
                System.out.println("任务的创建时间：" + task.getCreateTime());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("#######################################");
            }
        }
    }

    /**拾取任务，将组任务分给个人任务，指定任务的办理人字段*/
    @Test
    public void claim(){
        //将组任务分配给个人任务
        //任务ID
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        String taskId = "104";
        //分配的个人任务（可以是组任务中的成员，也可以是非组任务的成员）
        String userId = "BB";
        processEngine.getTaskService()//
                .claim(taskId, userId);
    }

    /**完成任务*/
    @Test
    public void completeTask() {
        // 任务ID
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        String taskId = "104";
        processEngine.getTaskService()//
                .complete(taskId);
        System.out.println("完成任务：" + taskId);
    }

    /**查询正在执行的组任务列表*/
    @Test
    public void findGroupCandidate() {
        // 任务ID
        String taskId = "104";
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        List<IdentityLink> list = processEngine.getTaskService()//
                .getIdentityLinksForTask(taskId);
        if (list != null && list.size() > 0) {
            for (IdentityLink identityLink : list) {
                System.out.println("任务ID：" + identityLink.getTaskId());
                System.out.println("流程实例ID："
                        + identityLink.getProcessInstanceId());
                System.out.println("用户ID：" + identityLink.getUserId());
                System.out.println("工作流角色ID：" + identityLink.getGroupId());
                System.out.println("#########################################");
            }
        }
    }

    /**查询历史的组任务列表*/
    @Test
    public void findHistoryGroupCandidate() {
        // 流程实例ID
        String processInstanceId = "101";
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        List<HistoricIdentityLink> list = processEngine.getHistoryService()
                .getHistoricIdentityLinksForProcessInstance(processInstanceId);
        if (list != null && list.size() > 0) {
            for (HistoricIdentityLink identityLink : list) {
                System.out.println("任务ID：" + identityLink.getTaskId());
                System.out.println("流程实例ID："
                        + identityLink.getProcessInstanceId());
                System.out.println("用户ID：" + identityLink.getUserId());
                System.out.println("工作流角色ID：" + identityLink.getGroupId());
                System.out.println("#########################################");
            }
        }
    }








}
