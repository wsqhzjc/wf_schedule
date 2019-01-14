Ext.define('JDD.schedule.taskResultWindow', {
    extend: 'Ext.window.Window',
    alias: 'taskResultWindow',
    title: '任务执行历史',
    width: 1400,
    modal: true,
    resizable: false,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    initComponent: function () {
        this.callParent(arguments);
        var me = this;
        var jobConfig = me.jobConfig;
        var store = Ext.create('DCIS.Store', {
            autoLoad: true,
            baseParams: {
                groupName: jobConfig.groupName,
                jobName: jobConfig.jobName
            },
            url: 'schedule/private/ScheduleController/getTaskResultList.do',
            fields: ['id', 'groupName', 'jobName', 'currentNodeId', 'errorMsg', 'lastFireTime', 'lastEndTime', 'success']
        });

        me.add({
            border: false,
            store: store,
            xtype: 'searchpanel',
            title: '查询',
            collapsible: true,
            collapsed: false,
            columns: 3,
            buildField: "Manual",
            forceFit: true,
            items: [{
                xtype: 'combo',
                name: 'success',
                fieldLabel: '执行状态',
                store: [[1, 'SUCCESS'], [0, 'FAIL']]
            }]
        });

        me.add({
            xtype: 'datagrid',
            store: store,
            flex: 1,
            forceFit: true,
            buildField: "Manual",
            height: 600,
            autoScroll: true,
            tbar: [{
                text: '清空执行日志',
                iconCls: "icon-add",
                handler: function () {
                    Ext.Msg.confirm("确认", "确定要删除吗？", function (button) {
                        if (button === "yes") {
                            callapi("schedule/private/ScheduleController/deleteTaskResultList.do", {
                                groupName: jobConfig.groupName,
                                jobName: jobConfig.jobName
                            }, function (result) {
                                if (result.success) {
                                    Ext.Msg.show({
                                        title: "提示",
                                        msg: "删除成功",
                                        modal: true,
                                        icon: Ext.Msg.INFO,
                                        buttons: Ext.Msg.OK
                                    });
                                    store.reload();
                                } else {
                                    Ext.Msg.show({
                                        title: '错误',
                                        msg: "删除失败",
                                        buttons: Ext.Msg.OK,
                                        icon: Ext.Msg.ERROR,
                                        modal: true
                                    });
                                }
                            })
                        }
                    });
                }
            }],
            columns: [{
                text: '执行结果',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'success',
                width: 150,
                renderer: function (value) {
                    if (value === 1) {
                        return "<span style='color:green;font-weight:bold;'>SUCCESS</span>";
                    } else if (value === 0) {
                        return "<span style='color:red;font-weight:bold;'>FAIL</span>";
                    }
                }
            }, {
                text: '执行节点',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'currentNodeId',
                width: 150
            }, {
                text: '执行开始时间',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'lastFireTime',
                width: 150
            }, {
                text: '执行结束时间',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'lastEndTime',
                width: 150
            }, {
                menuDisabled: true,
                sortable: false,
                xtype: 'linkColumn',
                header: '失败原因',
                width: 200,
                links: [{
                    linkText: "查看",
                    handler: function (grid, rowIndex, colIndex, record) {
                        if (record.data.success === 0) {
                            var win = Ext.create("JDD.schedule.showReason", {failReason: record.data.errorMsg});
                            win.show();
                        }
                    }
                }]
            }]
        });
    }
});