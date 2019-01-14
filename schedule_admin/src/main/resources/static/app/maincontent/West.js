Ext.define("JDD.maincontent.West", {
    extend: "Ext.panel.Panel",
    alias: "widget.maincontent_west",
    region: 'west',
    collapsible: true,
    title: '功能菜单',
    split: true,
    width: 180,
    defaults: {
        bodyStyle: 'padding:5px 0px 10px 0px'
    },
    layout: {
        type: 'accordion',
        titleCollapse: false,
        animate: false
    },
    tools: [{
        id: 'help',
        qtip: '用户手册',
        handler: function () {
        }
    }],
    animate: false,
    initComponent: function () {
        this.callParent(arguments);
        var me = this;
        callapi("schedule/private/home/listModule.do", {
            root: $systemId
        }, function (result) {
            var gr = result;
            for (var i = 0; i < gr.length; i++) {
                var panel = Ext.create("Ext.tree.Panel", {
                    title: gr[i].text,
                    rootVisible: false,
                    icon: gr[i].icon,
                    titleCollapse: true,
                    store: Ext.create('Ext.data.TreeStore', {
                        proxy: {
                            type: 'ajax',
                            url: 'schedule/private/home/listMenu.do'
                        },
                        nodeParam: 'code',
                        root: {
                            expanded: false,
                            text: gr[i].text,
                            id: gr[i].code,
                            leaf: false
                        },
                        autoLoad: true,
                        fields: ['id', 'text', 'leaf', 'icon', 'expanded', 'moduleLink', 'billType']
                    }),
                    listeners: {
                        cellclick: function (tree, td, cellIndex,
                                             record, tr, rowIndex, e, eo) {
                            if (record.data.leaf == true) {
                                me.itemClick(record);
                            }
                        }
                    }
                });
                me.add(panel);
            }
        });
    },
    itemClick: function (rec) {
        var re = rec.data;
        var className = re.moduleLink;
        var center = Ext.ComponentQuery.query("maincontent_center")[0];
        if (className == "" || className == null) {
            var pa = Ext.create("JDD.maincontent.undefinded", {
                title : re.text
            });
            center.add(pa);
            center.setActiveTab(pa);
        } else {
            var arr = className.split('.');
            var alias = arr[arr.length - 1];
            if (alias == "" || alias == null) {
                var pa = Ext.create("JDD.maincontent.undefinded", {
                    title : re.text
                });
                center.add(pa);
                center.setActiveTab(pa);
            } else {
                var loading = new Ext.LoadMask(center, {
                    msg : '页面加载中，请稍等...'
                });
                loading.show();
                var panelQuery = Ext.ComponentQuery.query(alias);
                if (panelQuery.length == 0) {
                    try {
                        var panel = Ext.create(className, {
                            title : rec.parentNode.data.text + '-'
                            + re.text,
                            billType : re.billType,
                            icon : re.icon,
                            menuCode : re.id
                        });
                        center.add(panel);
                        center.setActiveTab(panel);
                        loading.hide();
                    } catch (e) {}

                } else {
                    for (var i = 0; i < panelQuery.length; i++) {
                        var ltype = panelQuery[i].billType;
                        var ftype = re.billType;
                        if (ltype == null) {
                            ltype = ""
                        }
                        if (ftype == null) {
                            ftype = ""
                        }
                        if (ltype == ftype) {
                            center.setActiveTab(panelQuery[i]);
                            loading.hide();
                            return;
                        }
                    }
                    try {
                        var panel = Ext.create(className, {
                            title : rec.parentNode.data.text + '-'
                            + re.text,
                            billType : re.billType,
                            icon : re.icon,
                            menuCode : re.id
                        });
                        center.add(panel);
                        center.setActiveTab(panel);
                        loading.hide();
                        return;
                    } catch (e) {}
                }
            }
        }

    }
});