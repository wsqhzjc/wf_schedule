Ext.define('JDD.schedule.showSingleTask', {
    extend: 'Ext.window.Window',
    alias: 'showSingleTask',
    title: '查看任务详细',
    modal: true,
    resizable: false,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    initComponent: function () {
        this.callParent(arguments);
        var me = this;
        var res = callapiSync('schedule/private/ScheduleController/showTask.do', {groupName: me.taskInfo.groupName, jobName: me.taskInfo.jobName});
        var jobConfig = res.jobConfig;
        var nodes = res.nodes;
        me.add({
            xtype: 'dataform',
            baseCls: 'x-plain',
            border: true,
            columns: 2,
            items: [{
                name: 'groupName',
                colspan: 2,
                value: jobConfig.groupName,
                fieldLabel: '组',
                readOnly: true
            }, {
                name: 'schedulerName',
                colspan: 2,
                value: jobConfig.schedulerName,
                fieldLabel: '任务名称',
                readOnly: true
            },{
                xtype:'combo',
                name: 'running',
                colspan: 2,
                fieldLabel: '任务状态',
                readOnly: true,
                store:[['0','STOP'],['1','RUN']],
                value: jobConfig.running
            },{
                name: 'cronExpr',
                colspan: 2,
                fieldLabel: '定时表达式',
                value: jobConfig.cronExpr,
                readOnly: true
            },{
                name: 'jobName',
                colspan: 2,
                fieldLabel: '任务执行类',
                value: jobConfig.jobName,
                readOnly: true
            },{
                name: 'jobMethod',
                colspan: 2,
                value: jobConfig.jobMethod,
                fieldLabel: '任务执行方法',
                readOnly: true
            },{
                name: 'currentNodeId',
                colspan: 2,
                value : jobConfig.currentNodeId,
                fieldLabel: '当前运行节点',
                readOnly: true
            },{
                colspan: 2,
                value : nodes.join(","),
                fieldLabel: '所有节点',
                readOnly: true
            },{
                name: 'lastFireTime',
                colspan: 2,
                value: jobConfig.lastFireTime,
                fieldLabel: '上次运行开始事件',
                readOnly: true
            },{
                name: 'nextFireTime',
                colspan: 2,
                value : jobConfig.nextFireTime,
                fieldLabel: '下次运行开始事件',
                readOnly: true
            },{
                name: 'modifyTime',
                colspan: 2,
                value: jobConfig.modifyTime,
                fieldLabel: '修改事件',
                readOnly: true
            },{
                xtype: 'textareafield',
                name: 'errorMsg',
                value: jobConfig.errorMsg,
                colspan:2,
                fieldLabel: '最后执行信息',
                readOnly: true
            }]
        });
    }
});