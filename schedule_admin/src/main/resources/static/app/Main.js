Ext.define('JDD.Main', {
			extend : 'Ext.container.Viewport',
			layout : 'border',
			requires : ["JDD.maincontent.North", "JDD.maincontent.Center",
					"JDD.maincontent.South", "JDD.maincontent.West",
					"JDD.maincontent.FirstPage"],
			items : [{
						xtype : "maincontent_north"
					}, {
						xtype : "maincontent_west"
					}, {
						xtype : "maincontent_south"
					}, {
						xtype : 'maincontent_center'
					}],
			initComponent : function() {
				var me = this;
				me.callParent(arguments);
				me.on("afterrender", function() {
							me.initUserEnvironment();
						});


			},
			initUserEnvironment : function(user) {

			}
		});