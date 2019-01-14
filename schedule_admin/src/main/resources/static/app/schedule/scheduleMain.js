Ext.define('JDD.schedule.scheduleMain', {
    extend: 'Ext.panel.Panel',
    title: 'Job',
    xtype: 'scheduleMain',
    closable: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    initComponent: function () {
        this.callParent(arguments);
        var me = this;
        var store = Ext.create('DCIS.Store', {
            autoLoad: true,
            url: 'schedule/private/ScheduleController/listGroup.do',
            fields: ['name', 'haveCluster', 'clusterNodes']
        });

        me.add({
            xtype: 'datagrid',
            store: store,
            showPaging:false,
            flex: 1,
            forceFit: true,
            buildField: "Manual",
            height: 600,
            autoScroll: true,
            tbar: [{
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
                width: 150,
                callback: function (link, record) {
                    return link;
                },
                links: [{
                    icon: 'list',
                    linkText: '任务',
                    handler: function (grid, rowIndex, colIndex, record) {
                        var win = Ext.create("JDD.schedule.taskWindow", {groupName: record.data.name});
                        win.show();
                    }
                }, {
                    icon: 'delete',
                    linkText: '删除',
                    handler: function (grid, rowIndex, colIndex, record) {
                        Ext.Msg.confirm("确认", "删除后所有任务将移除停止；确定要删除吗？", function (button) {
                            if (button === "yes") {
                                callapi('schedule/private/ScheduleController/deleteGroup.do', {groupName: record.data.name}, function (result) {
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
                }]
            }, {
                text: '任务组',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'name',
                width: 150
            }, {
                text: '运行节点数',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'haveCluster',
                width: 150,
                renderer: function (value) {
                    if (value > 0) {
                        return "<span style='color:green;font-weight:bold;'>"+value+"</span>";
                    } else {
                        return "<span style='color:red;font-weight:bold;'>"+ value + "</span>";
                    }
                }
            }, {
                text:'节点',
                menuDisabled: true,
                sortable: false,
                dataIndex: 'clusterNodes',
                width: 150,
                renderer: function (value) {
                    if (value.length > 0) {
                        return "<span style='color:green;font-weight:bold;'>"+value+"</span>";
                    } else {
                        return "<span style='color:red;font-weight:bold;'>无可用节点</span>";
                    }
                }
            }
            ]
        });
    }
});