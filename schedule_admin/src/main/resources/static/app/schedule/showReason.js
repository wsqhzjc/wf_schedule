Ext.define('JDD.schedule.showReason', {
    extend: 'Ext.window.Window',
    alias: 'showReason',
    title: '失败原因',
    modal: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    initComponent: function () {
        this.callParent(arguments);
        var me = this;
        me.add({
            xtype: 'panel',
            region:'center',
            width:800,
            height:500,
            autoScroll:true,
            html: '<pre>'+me.failReason+'</pre>'
        });
    }
});