Ext.define('JDD.schedule.taskWindow', {
    extend: 'Ext.window.Window',
    alias: 'taskWindow',
    title: '任务管理',
    width: 1400,
    autoScroll: true,
    modal: true,
    resizable: false,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    initComponent: function () {
        this.callParent(arguments);
        var me = this;
        var groupName = me.groupName;
        var store = Ext.create('DCIS.Store', {
            autoLoad: true,
            baseParams: {groupName: me.groupName},
            url: 'schedule/private/ScheduleController/listTask.do',
            fields: ['groupName', 'jobName', 'schedulerName', 'running', 'active', 'cronExpr', 'jobMethod', 'extraData', 'lastFireTime', 'nextFireTime', 'currentNodeId', 'modifyTime', 'errorMsg']
        });

        var runner = new Ext.util.TaskRunner();
        var task = {
            run: function () {
                store.reload();
            },
            interval: 10 * 1000
        };

        me.on("close",function(){
            runner.destroy();
        });

        runner.start(task);

        me.add({
            xtype: 'datagrid',
            store: store,
            flex: 1,
            forceFit: true,
            showPaging: false,
            buildField: "Manual",
            height: 600,
            autoScroll: true,
            tbar: [{
                text: '新增',
                iconCls: "icon-add",
                handler: function () {
                    Ext.create('JDD.schedule.addSingleTask', {
                        groupName: groupName,
                        store: store
                    }).show();
                }
            }, {
                text: '全部启动',
                iconCls: "icon-reload",
                handler: function () {
                    Ext.Msg.confirm("确认", "确定全部启动所有任务吗？", function (button) {
                        if (button === "yes") {
                            callapi("schedule/private/ScheduleController/startAllTaskJob.do", {groupName: groupName}, function (result) {
                                if (result.success) {
                                    Ext.Msg.show({
                                        title: "提示",
                                        msg: "启动任务成功",
                                        modal: true,
                                        icon: Ext.Msg.INFO,
                                        buttons: Ext.Msg.OK
                                    });
                                    store.reload();
                                } else {
                                    Ext.Msg.show({
                                        title: '错误',
                                        msg: result.data,
                                        buttons: Ext.Msg.OK,
                                        icon: Ext.MessageBox.ERROR,
                                        modal: true
                                    });
                                }
                            })
                        }
                    })
                }
            }, {
                text: '全部关闭',
                iconCls: "icon-cancel",
                handler: function () {

                    Ext.Msg.confirm("确认", "确定停止所有任务吗？", function (button) {
                        if (button === "yes") {
                            callapi("schedule/private/ScheduleController/stopAllTaskJob.do", {groupName: groupName}, function (result) {
                                if (result.success) {
                                    Ext.Msg.show({
                                        title: "提示",
                                        msg: "停止所有任务成功",
                                        modal: true,
                                        icon: Ext.Msg.INFO,
                                        buttons: Ext.Msg.OK
                                    });
                                    store.reload();
                                }
                                else {
                                    Ext.Msg.show({
                                        title: '错误',
                                        msg: result.data,
                                        buttons: Ext.Msg.OK,
                                        icon: Ext.MessageBox.ERROR,
                                        modal: true
                                    });
                                }
                            })
                        }
                    })
                }
            }, {
                text: '刷新',
                iconCls: "icon-reload",
                handler: function () {
                    store.reload();
                }
            }],
            columns: [{
                menuDisabled: true,
                sortable: false,
                xtype: 'linkColumn',
                header: '操作',
                locked: true,
                width: 300,
                callback: function (link, record) {
                    if (!record.data.active) {
                        link[0].hidden = false;
                        link[1].hidden = false;
                        link[2].hidden = false;
                        link[3].hidden = false;
                        link[4].hidden = true;
                        link[5].hidden = false;
                    } else {
                        link[0].hidden = false;
                        link[1].hidden = false;
                        link[2].hidden = false;
                        link[3].hidden = false;
                        link[4].hidden = false;
                        link[5].hidden = true;
                    }
                    return link;
                },
                links: [{
                    icon: 'look',
                    linkText: '查看',
                    handler: function (grid, rowIndex, colIndex, record) {
                        var win = Ext.create("JDD.schedule.showSingleTask", {taskInfo: record.data});
                        win.show();
                    }
                }, {
                    icon: 'delete',
                    linkText: '删除',
                    handler: function (grid, rowIndex, colIndex, record) {
                        delTaskInfo(record.data);
                    }
                }, {
                    icon: 'edit',
                    linkText: '编辑',
                    handler: function (grid, rowIndex, colIndex, record) {
                        var win = Ext.create("JDD.schedule.editSingleTask", {store: store});
                        win.down('dataform').setValues(record.data);
                        win.show();
                    }
                }, {
                    icon: 'ok',
                    linkText: '立刻执行',
                    handler: function (grid, rowIndex, colIndex, record) {
                        Ext.Msg.confirm("确认", "确定执行该任务吗？", function (button) {
                            if (button === "yes") {
                                callapi("schedule/private/ScheduleController/executeTaskJobNow.do", {
                                    groupName: record.data.groupName,
                                    jobName: record.data.jobName
                                }, function (result) {
                                    if (result.success) {
                                        Ext.Msg.show({
                                            title: "提示",
                                            msg: "已执行任务",
                                            modal: true,
                                            icon: Ext.Msg.INFO,
                                            buttons: Ext.Msg.OK
                                        });
                                        store.reload();
                                    } else {
                                        Ext.Msg.show({
                                            title: '错误',
                                            msg: result.data,
                                            buttons: Ext.Msg.OK,
                                            icon: Ext.MessageBox.ERROR,
                                            modal: true
                                        });
                                    }
                                })
                            }
                        })
                    }
                }, {
                    icon: 'cancel',
                    linkText: '停止',
                    handler: function (grid, rowIndex, colIndex, record) {
                        if (record.data.currentNodeId === null || record.data.currentNodeId === '') {
                            Ext.Msg.show({
                                title: "提示",
                                msg: "无可用运行节点",
                                modal: true,
                                icon: Ext.Msg.INFO,
                                buttons: Ext.Msg.OK
                            });
                            return;
                        }

                        Ext.Msg.confirm("确认", "确定停止该任务吗？", function (button) {
                            if (button === "yes") {
                                callapi("schedule/private/ScheduleController/stopTaskJobNow.do", {
                                    groupName: record.data.groupName,
                                    jobName: record.data.jobName
                                }, function (result) {
                                    if (result.success) {
                                        Ext.Msg.show({
                                            title: "提示",
                                            msg: "停止任务成功",
                                            modal: true,
                                            icon: Ext.Msg.INFO,
                                            buttons: Ext.Msg.OK
                                        });
                                        store.reload();
                                    }
                                    else {
                                        Ext.Msg.show({
                                            title: '错误',
                                            msg: result.data,
                                            buttons: Ext.Msg.OK,
                                            icon: Ext.MessageBox.ERROR,
                                            modal: true
                                        });
                                    }
                                })
                            }
                        })
                    }
                }, {
                    icon: 'ok',
                    linkText: '立刻启动',
                    handler: function (grid, rowIndex, colIndex, record) {
                        if (record.data.currentNodeId === null || record.data.currentNodeId === '') {
                            Ext.Msg.show({
                                title: "提示",
                                msg: "无可用运行节点",
                                modal: true,
                                icon: Ext.Msg.INFO,
                                buttons: Ext.Msg.OK
                            });
                            return;
                        }

                        Ext.Msg.confirm("确认", "确定启动该任务吗？", function (button) {
                            if (button === "yes") {
                                callapi("schedule/private/ScheduleController/startTaskJobNow.do", {
                                    groupName: record.data.groupName,
                                    jobName: record.data.jobName
                                }, function (result) {
                                    if (result.success) {
                                        Ext.Msg.show({
                                            title: "提示",
                                            msg: "任务已启动",
                                            modal: true,
                                            icon: Ext.Msg.INFO,
                                            buttons: Ext.Msg.OK
                                        });
                                        store.reload();
                                    } else {
                                        Ext.Msg.show({
                                            title: '错误',
                                            msg: result.data,
                                            buttons: Ext.Msg.OK,
                                            icon: Ext.MessageBox.ERROR,
                                            modal: true
                                        });
                                    }
                                })
                            }
                        })
                    }
                }]
            }, {
                text: '组',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'groupName',
                width: 150
            }, {
                text: '任务名称',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'schedulerName',
                width: 150
            }, {
                text: '当前运行节点',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'currentNodeId',
                width: 150,
                renderer: function (value) {
                    if (value === null || value === '') {
                        return '<span style="color:red";font-weight:bold;>当前无可运行节点</span>'
                    } else {
                        return '<span style="color:green;font-weight:bold;" >' + value + '</span>'
                    }
                }
            }, {
                text: '任务状态',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'active',
                width: 150,
                renderer: function (value) {
                    if (value) {
                        return "<span style='color:green;font-weight:bold;'>有效</span>";
                    } else {
                        return "<span style='color:red;font-weight:bold;'>无效</span>";
                    }
                }
            }, {
                text: '运行状态',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'running',
                width: 150,
                renderer: function (value) {
                    if (value) {
                        return "<span style='color:green;font-weight:bold;'>RUNNING</span>";
                    } else {
                        return "<span style='color:red;font-weight:bold;'>STOP</span>";
                    }
                }
            }, {
                text: '类',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'jobName',
                width: 150
            }, {
                text: '方法',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'jobMethod',
                width: 150
            }, {
                text: '定时表达式',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'cronExpr',
                width: 150
            }, {
                menuDisabled: true,
                sortable: false,
                xtype: 'linkColumn',
                header: '执行历史',
                width: 200,
                links: [{
                    linkText: "查看",
                    handler: function (grid, rowIndex, colIndex, record) {
                        var win = Ext.create("JDD.schedule.taskResultWindow", {jobConfig: record.data});
                        win.show();
                    }
                }]
            }, {
                text: '上一次运行开始时间',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'lastFireTime',
                width: 150
            }, {
                text: '下一次运行开始时间',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'nextFireTime',
                width: 150
            }, {
                text: '修改时间',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'modifyTime',
                width: 150
            }, {
                text: '最后执行消息',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'errorMsg',
                width: 150
            }
            ]
        });

        me.down("datagrid").view.loadMask=false;

        var delTaskInfo = function (data) {
            Ext.Msg.confirm("确认", "确定要删除吗？", function (button) {
                if (button === "yes") {
                    var center = Ext.ComponentQuery.query("maincontent_center")[0];
                    var loading = new Ext.LoadMask(center, {
                        msg: '删除中，请稍等...'
                    });
                    loading.show();
                    callapi('schedule/private/ScheduleController/deleteTaskJob.do', data, function (result) {
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
                        loading.hide();
                    }, null, null, false)
                }
            });
        };
    }
});