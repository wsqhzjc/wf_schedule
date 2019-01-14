Ext.define("JDD.maincontent.North", {
    extend: "Ext.panel.Panel",
    alias: "widget.maincontent_north",
    height: 45,
    bodyCls: "north",
    region: "north",
    border: false,
    html: '<div id="head">'
    + '<div id="logo" class="FONT">统一任务调度中心</div>'
    + '<div id="inform"><ul><li><a>欢迎访问！'
    + '</a>'
    + '</li><li><a><img src="./resources/images/icon1.jpg" width="21" height="22" />' + $USER.longName
    + '</a>'
    + '</li> <li><a href="javascript:JDD.maincontent.North.logMain()"><img src="./resources/images/icon2.jpg" width="21" height="22" />主页</a> </li><li><a href="javascript:JDD.maincontent.North.logOut()"><img src="./resources/images/icon3.jpg" width="21" height="22" />退出</a></li></ul></div>'
    + '</div>',
    statics: {
        logOut: function () {
            callapi("schedule/private/home/loginout.do", {},
                function (result) {
                    if (result == true) {

                        window.location.href = $loginOutUrl;

                    }
                });
        },
        reload: function () {

        },
        logMain: function () {

        }
    },
    initComponent: function () {
        this.callParent(arguments);
        var me = this;
    }
});
