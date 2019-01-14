Ext.define("DCIS.SearchPanel", {
    extend: "Ext.form.FieldSet",
    alias: "widget.searchpanel",
    title: '检索条件',
    titleCollapse: true,
    border: false,
    margin: "0 0 -1 0",
    collapsible: this.collapsible || true,
    collapsed: this.collapsed || true,
    constructor: function (cfg) {
        var config = Ext.apply({}, cfg);
        Ext.applyIf(config, {
            layout: {
                type: "vbox",
                align: "center"
            }
        });
        if (Ext.typeOf(config.store) == "string") {
            config.store = DCIS.StoreMgr.get(config.store);
        }
        this.store = config.store;
        this.baseParams = config.store.baseParams;
        var formPanel = this.buildFormPanel(config);
        var buttonPanel = this.buildButtonPanel(config);

        if (config.items) {
            formpanel.items = config.items;
        }
        config.items = [formPanel, buttonPanel];

        this.callParent([config]);
    },
    initComponent: function () {
        this.callParent(arguments);
        this.form = this.query("dataform")[0];
    },
    buildFormPanel: function (config) {
        var formpanel = {
            xtype: "dataform",
            // flex: 1,
            margin: "0 0 0 0",
            integrateFieldType: "custom"
        };
        if (config.columns) {
            formpanel.columns = config.columns;
            delete config.columns;
        } else {
            formpanel.columns = 1;
        }
        if (config.columnsWidth) {
            formpanel.columnsWidth = config.columnsWidth;
            delete config.columnsWidth;
        }
        if (config.columnsLabelWidth) {
            formpanel.columnsLabelWidth = config.columnsLabelWidth;
            delete config.columnsLabelWidth;
        }
        if (config.store) {
            formpanel.store = config.store;
            delete config.store;
        }
        if (config.fields) {
            formpanel.fields = config.fields;
            delete config.fields;
        }
        if (config.buildField) {
            formpanel.buildField = config.buildField;
            delete config.buildField;
        }
        if (config.sortFields) {
            formpanel.sortFields = config.sortFields;
            delete config.sortFields;
        }
        if (config.customFields) {
            formpanel.customFields = config.customFields;
            delete config.customFields;
        }
        if (config.customItems) {
            formpanel.customItems = config.customItems;
            delete config.customItems;
        }
        if (config.items) {
            formpanel.items = config.items;
            delete config.items;
        }
        ;
        return formpanel;
    },
    buildButtonPanel: function (config) {
        var me = this;

        var buttonPanel = {
            width: "auto",
            xtype: "panel",
            baseCls: 'x-plain',
            border: false,
            height: 25,
            layout: {
                type: "hbox",
                align: "center"
            }
        };
        buttonPanel.items = [];

        buttonPanel.items.push({
            xtype: "button",
            text: "查询",
            name: 'search',
            width: 60,
            iconCls: 'icon-search',
            scope: me,
            listeners: {
                'click': {
                    fn: this.search,
                    scope: this
                }
            }
        });
        buttonPanel.items.push({
            xtype: "panel",
            width: 150,
            border: false,
            height: "auto"
        });
        buttonPanel.items.push({
            xtype: "button",
            text: "重置",
            iconCls: 'icon-undo',
            width: 60,
            handler: me.searchCancel,
            scope: me
        });
        return buttonPanel;
    },
    search: function () {
        if (!this.form.isValid()) {
            return;
        }
        var me = this;
        var value = this.form.getValues();
        this.store.currentPage = 1;
        if (me.store.baseParams) {
            this.store.proxy.extraParams = Ext.apply({}, me.store.baseParams);
        }
        this.store.load({
            params: value,
            callback: function () {
                me.store.proxy.extraParams = Ext.apply(me.store.proxy.extraParams, value);
                if (me.searchCallBack != null) {
                    me.searchCallBack(me.store, value);
                }
            }
        });
    },
    searchCancel: function () {
        var me = this;
        this.form.reset();
        if (me.resetCall !== null) {
            me.resetCall(me.store);
        }
        if (me.store.baseParams) {
            this.store.proxy.extraParams = Ext.apply({}, me.store.baseParams);
        } else {
            this.store.proxy.extraParams = {};
        }
    },
    resetCall: null,
    searchCallBack: null
});