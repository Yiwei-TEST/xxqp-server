<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String basePath = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() == 80 ? "" : (":" + request.getServerPort())) + request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1.0"/>
    <title id="title">操作结果</title>

    <script type="text/javascript" src="<%=basePath%>/js/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="<%=basePath%>/js/jquery.qrcode.min.js"></script>
</head>

<body id="myBody" style="width: 100%;height: 100%;font-family: 微软雅黑;margin: 0 0 0 0;text-align: center;font-size: 20px;" onresize="resize()">
<div id="myDiv" style="width: 95%;text-align: center;font-size: 20px;background-color: #DDDDDD;border-radius: 8px;"><br/><img src="<%=basePath%>/images/ok.png" style="height: 33%;width: 33%;"/><br/><br/>操作完成<br/><br/><br/></div>
</body>

<script>
    var forward = getQueryString("forward");
    if (forward.length>0){
        window.location = forward;
    }else{
        var qrcode = getQueryString("qrcode");
        if (qrcode.length>0){
            $("#title").html("微信扫码支付");

            var browser = {
                versions: function () {
                    var u = navigator.userAgent, app = navigator.appVersion;
                    return {   //移动终端浏览器版本信息
                        trident: u.indexOf('Trident') > -1, //IE内核
                        presto: u.indexOf('Presto') > -1, //opera内核
                        webKit: u.indexOf('AppleWebKit') > -1, //苹果、谷歌内核
                        gecko: u.indexOf('Gecko') > -1 && u.indexOf('KHTML') == -1, //火狐内核
                        mobile: !!u.match(/AppleWebKit.*Mobile.*/), //是否为移动终端
                        ios: !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), //ios终端
                        android: u.indexOf('Android') > -1 || u.indexOf('Linux') > -1, //android终端或uc浏览器
                        iPhone: u.indexOf('iPhone') > -1, //是否为iPhone或者QQHD浏览器
                        iPad: u.indexOf('iPad') > -1, //是否iPad
                        webApp: u.indexOf('Safari') == -1 //是否web应该程序，没有头部与底部
                    };
                }(),
                language: (navigator.browserLanguage || navigator.language).toLowerCase()
            };

            var tip = "<br/><span>充值步骤：</span><br/>1、截图保存二维码</span><br/><span>2、打开微信-扫一扫，右上角选择-从相册选取二维码</span>";
            if (browser.versions.mobile) {//判断是否是移动设备打开。browser代码在下面
                var ua = navigator.userAgent.toLowerCase();//获取判断用的对象
                if (ua.match(/MicroMessenger/i) == "micromessenger") {
                    tip = "<br/><span>充值步骤：</span><br/><span>1、长按二维码</span><br/>2、点击：识别图中二维码</span>";
                }
            }

            $('#myDiv').css("background-color","white");
            $('#myDiv').css("padding-top",(window.screen.availHeight-168)/4);
            $('#myDiv').qrcode({width: 168,height: 168,text: qrcode});
            var image = new Image();
            image.src = document.getElementsByTagName("canvas").item(0).toDataURL("image/png");

            $("#myDiv").html('<image src="'+image.src+'"/>'+tip);
        }
    }

    $(document).ready(function () {
        resize();
    });

    function getQueryString(name)
    {
        var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        var value="";
        if(r!=null){
            value=unescape(r[2]);
        }
        if (value==null){
            value="";
        }
        return value;
    }

    function resize() {
        var w = Math.ceil($("#myBody").width() * 95 / 100);
        if (w%2==1){
            w=w+1;
        }

        var t = ($("#myBody").width() - w)/2;
        var m=t+"px "+t+"px "+t+"px "+t+"px";

        $("#myDiv").css({
            margin:m
        });
    }
</script>
</html>