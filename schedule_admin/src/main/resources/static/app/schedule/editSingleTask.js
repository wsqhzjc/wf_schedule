Ext.define('JDD.schedule.editSingleTask', {
    extend: 'Ext.window.Window',
    alias: 'editSingleTask',
    title: '编辑任务',
    modal: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    initComponent: function () {
        this.callParent(arguments);
        var me = this;
        me.add({
            xtype: 'dataform',
            baseCls: 'x-plain',
            border: true,
            columns: 2,
            items: [{
                name: 'groupName',
                colspan: 2,
                fieldLabel: '任务组',
                readOnly: true
            }, {
                name: 'schedulerName',
                colspan: 2,
                fieldLabel: '任务名称'
            }, {
                name: 'cronExpr',
                afterLabelTextTpl: required,
                colspan: 2,
                allowBlank: false,
                fieldLabel: '定时表达式',
                blankText: '定时表达式不能为空',
                maxLength: 40
            }, {
                name: 'jobName',
                afterLabelTextTpl: required,
                colspan: 2,
                allowBlank: false,
                fieldLabel: '任务执行类',
                blankText: '任务执行类不能为空',
                readOnly: true,
                maxLength: 200
            }, {
                name: 'jobMethod',
                afterLabelTextTpl: required,
                colspan: 2,
                allowBlank: false,
                fieldLabel: '任务执行方法',
                blankText: '任务执行方法不能为空',
                maxLength: 30
            }, {
                name:'extraData',
                colspan: 2,
                fieldLabel: '参数(String类型)'
            }, {
                xtype: 'radiogroup',
                fieldLabel: '状态:',
                colspan: 2,
                items: [
                    {boxLabel: '有效', name: 'active', inputValue: true},
                    {boxLabel: '无效', name: 'active', inputValue: false}
                ]
            }, {
                xtype:'hidden',
                name:'running',
                value: false
            }]
        });
    },
    buttons: [{
        text: '修改信息',
        iconCls: "icon-edit",
        handler: function () {
            var currentWindow = this.up('window');
            var form = currentWindow.down('dataform').getForm();
            if (!form.isValid()) {
                return;
            }
            var store = currentWindow.store;
            var datas = form.getValues();

            var loading = new Ext.LoadMask(currentWindow, {
                msg: '修改中，请稍等...'
            });
            loading.show();
            callapi("schedule/private/ScheduleController/updateTask.do", datas,
                function (result) {
                    if (result.success) {
                        Ext.Msg.show({
                            title: "提示",
                            msg: "任务信息修改成功",
                            modal: true,
                            icon: Ext.Msg.INFO,
                            buttons: Ext.Msg.OK
                        });
                        store.reload();
                        currentWindow.close();
                    }
                    else {
                        Ext.Msg.show({
                            title: '错误',
                            msg: result.data,
                            buttons: Ext.Msg.OK,
                            icon: Ext.Msg.ERROR,
                            modal: true
                        });
                    }
                    loading.hide();
                });
        }
    }, {
        text: '取消',
        iconCls: "icon-no",
        handler: function () {
            this.up('window').close();
        }
    }]
});