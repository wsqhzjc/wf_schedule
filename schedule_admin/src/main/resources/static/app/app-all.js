//主程序入口点
Ext.onReady(function () {
    Ext.application({
        name: 'JDD',
        requires: ["JDD.Main"],
        appFolder: 'app',
        launch: function () {
        	window.applicationName='JDD';
            Ext.create("JDD.Main");
        }
    });
});
