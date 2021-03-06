<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() == 80 ? "" : (":" + request.getServerPort())) + request.getContextPath();
    String userId = request.getParameter("userId");
    if (userId==null){
        userId="";
    }

    String goodsId = request.getParameter("goodsId");
    if (goodsId==null){
        goodsId="";
    }
    String queryString = request.getQueryString();
%>
<!doctype html>
<html>

<head>
    <meta charset="UTF-8">
    <title></title>
    <meta name="viewport"
          content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no"/>
    <link rel="stylesheet" href="<%=basePath%>/h5/pay/css/mui.min.css"/>
    <link rel="stylesheet" href="<%=basePath%>/h5/pay/css/public.css"/>
    <script src="<%=basePath%>/h5/pay/js/jquery-3.2.1.min.js"></script>
    <script src="<%=basePath%>/h5/pay/js/md5.js"></script>
    <style>
        body {
            background-color: #efeff4;
        }

        .head {
            padding: .3rem;
        }

        .head-img {
            width: 1rem;
            height: 1rem;
            border-radius: 50%;
            overflow: hidden;
            border: 2px solid #fff;
        }

        .border-b {
            border-bottom: 1px solid #F2F2F2;
        }

        .list li {
            padding: .3rem .2rem;
        }

        .list li div span:last-child {
            color: #8A6DE9;
        }

        .list li div:last-child {
            color: red;
        }

        input[type=text] {
            padding: .1rem;
            background: #EFEFF4;
        }

        button {
            width: 80%;
            height: .88rem;
        }

        .li-active {
            background: #ccc;
        }

        .select {
            height: 1rem;
        }
    </style>
</head>

<body style="background-color: white;">
<script>var fz = document.documentElement.clientWidth / 7.5 + 'px';
document.documentElement.style.fontSize = fz;</script>
<table cellpadding="0" cellspacing="0" style="position: fixed;text-align: center;height: 60px;width: 100%;background: #f4c4a0;" align="center">
    <tr style="width: 100%;text-align: center;height: 100%;" align="center">
        <td style="width: 100%;text-align: center;height: 100%;font-size: 22px;">??????????????????</td>
    </tr>
</table>

<div class="mui-content" style="padding-top: 60px;">
    <table class="list bg-fff" id="goodsItems" style="width: 100%;">

    </table>
    <div style="width: 100%;background: white;text-align: center;padding-top: 5px;padding-bottom: 5px;padding-left: 2%;padding-right: 2%;">
        <%--<div class="mui-col-xs-10 center-float-left">--%>
        <%--<input id="checkbox" class="ml10 mt5" type="checkbox" value="a" />--%>
        <%--<span class="ml10">???????????????</span>--%>
        <%--</div>--%>
        <table style="width: 100%;text-align: center;margin-top: 10px;">
            <tr>
                <td>
                    <input id="oUserId" class="mui-text-center show" style="height: 50px;" type="text" pattern="[0-9]*" minlength="6"
                           maxlength="6" placeholder="???????????????ID" value="<%=userId%>" onchange="queryUser(this.value)"/>
                </td>
            </tr>
            <tr id="myMsg" style="display: none;">
                <td>
                    <div style="padding-top: 5px;">
                        <table style="width: 100%;">
                            <tr>
                                <td><img id="userImg" style="width: 64px;height: 64px;" src=""></td>
                            </tr>
                            <tr>
                                <td id="userName">??????</td>
                            </tr>
                        </table>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <div class="center-float-center mt30" style="left:0;right:0;padding-top: 10px;">
                        <button type="button" class="mui-btn mui-btn-success ft38" onclick="pay('');">??????</button>
                    </div>
                </td>
            </tr>
        </table>
    </div>
</div>

<div id="div_help"
     style="position:fixed;top: 0px;display: none;background-image: url('<%=basePath%>/h5/pay/image/android_h5_help.png');background-repeat: round;background-size:100%;width: 100%;height: 100%;">
</div>
<!--<script src="js/mui.min.js"></script>-->
<script type="text/javascript">
    // $('.list li').click(function () {
    //     $(this).addClass('li-active').siblings().removeClass('li-active');
    // })
    // var flag = false;
    // $("#checkbox").change(function(){
    //   if(!flag){
    //   	$('#oUserId').show();
    //   	flag=true;
    //   	return;
    //   }else{
    //   	$('#oUserId').hide();
    //   	flag=false;
    //   }
    // });
    //			$('#checkbox').change()
</script>
</body>

<script>
    var inWeixin;
    var openid=getQueryString("openid");;
    if ("1"==getQueryString("weixin")) {
        inWeixin=true;
    }else{
        inWeixin=false;
    }

    var defaultPayType = getQueryString("type");
    if (defaultPayType.length>0&&(defaultPayType.indexOf("weixinnative")>=0||defaultPayType.indexOf("weixinjsapi")>=0)) {
        inWeixin=true;
    }

    var code = getQueryString("code")

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

    var browser = {
        versions: function () {
            var u = navigator.userAgent, app = navigator.appVersion;
            return {   //?????????????????????????????????
                trident: u.indexOf('Trident') > -1, //IE??????
                presto: u.indexOf('Presto') > -1, //opera??????
                webKit: u.indexOf('AppleWebKit') > -1, //?????????????????????
                gecko: u.indexOf('Gecko') > -1 && u.indexOf('KHTML') == -1, //????????????
                mobile: !!u.match(/AppleWebKit.*Mobile.*/), //?????????????????????
                ios: !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), //ios??????
                android: u.indexOf('Android') > -1 || u.indexOf('Linux') > -1, //android?????????uc?????????
                iPhone: u.indexOf('iPhone') > -1, //?????????iPhone??????QQHD?????????
                iPad: u.indexOf('iPad') > -1, //??????iPad
                webApp: u.indexOf('Safari') == -1 //??????web????????????????????????????????????
            };
        }(),
        language: (navigator.browserLanguage || navigator.language).toLowerCase()
    };

    if (browser.versions.mobile) {//????????????????????????????????????browser???????????????
        var ua = navigator.userAgent.toLowerCase();//????????????????????????

        if (ua.match(/MicroMessenger/i) == "micromessenger") {
            //??????????????????
            var height = document.body.scrollHeight;//-$(".header_table").height();

            $("#div_help").css("height", height);
            if (!inWeixin) {
                if (browser.versions.ios) {
                    //?????????IOS???????????????
                    $("#div_help").css("background-image", "url('<%=basePath%>/h5/pay/image/ios_h5_help.png')");
                    $("#div_help").css("display", "");
                }
                if (browser.versions.android) {
                    //??????????????????????????????
                    $("#div_help").css("background-image", "url('<%=basePath%>/h5/pay/image/android_h5_help.png')");
                    $("#div_help").css("display", "");
                }
            }

        } else {
        }
    } else {
    }

    function loadPayItems() {
        $.ajax({
            timeout: 6000,
            async: true,
            type: "GET",
            url: "<%=basePath%>/qipai!getPayItems.action",
            data: {specialPayType:defaultPayType},
            dataType: "json",
            success: function (result) {
//                JSON.stringify(result);//json????????????
//                JSON.parse(jsonStr);//????????????json
                goodsResult = result;
                goodsItems = result.payItem;
                var bl = true;
                $.each(result.payItem, function (index, tempData) {
                    var tempStr = '<tr class="center-float-center mui-row border-b" style="height: 55px;" id="goodsItemID' + index + '" onclick="clickGoodsItem(this,' + tempData.id + ');">' +
                        '<td class="mui-col-xs-4" style="width: 32%;"><span style="color: black;padding-left: 10px;">' + (tempData.roomCards+tempData.specialGive) + '???</span></td>';

                    tempStr += '<td class="mui-col-xs-6 mui-text-center"  style="width: 36%;">1???=' + parseInt((tempData.roomCards + tempData.specialGive) / tempData.amount) + '??????</td>' +
                        '<td class="mui-col-xs-2 mui-text-right" style="padding-right: 10px;width: 32%;color: red;">???' + tempData.amount + '</td></tr>';
                    $("#goodsItems").append(tempStr);

                    if (bl&&tempData.id=="<%=goodsId%>") {
                        bl=false;
                        $("#goodsItemID"+index).click();
                    }
                });
                if (bl&&goodsItems.length>0){
                    $("#goodsItemID"+(goodsItems.length>=2?parseInt(goodsItems.length/2):1)).click();
                }

                if(openid.length==0&&result.hasOwnProperty("appid")&&result.appid!=""&&""==getQueryString("code")&&defaultPayType!=""&&defaultPayType.indexOf("weixinjsapi")>=0){
                    window.location="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+result.appid+"&redirect_uri="+encodeURIComponent('<%=basePath%>/h5/pay/index.jsp?<%=queryString%>')+"&response_type=code&scope=snsapi_base&state="+defaultPayType+"#wechat_redirect";
                }
            },
            error: function (req, status, err) {
                console.info(status + "," + err);
            }
        });
        if ($("#oUserId").val()!=""){
            queryUser($("#oUserId").val());
        }
    }

    var goodsResult;
    var goodsItems;
    var currentGoods;
    $(document).ready(function () {
        if (openid==""&&code!=""&&defaultPayType!="") {
            var state = getQueryString("state");
            if ((state.indexOf("http://")==0||state.indexOf("https://")==0)&&state.indexOf("<%=basePath%>")!=0) {
                if(state.indexOf("?")>=0){
                    state+="&code="+encodeURIComponent(code);
                }else{
                    state+="?code="+encodeURIComponent(code);
                }
                window.location=state;
            }else{
                $.ajax({
                    timeout: 6000,
                    async: true,
                    type: "GET",
                    url: "<%=basePath%>/authorizationAction!getUserOpenid.action",
                    data: {code:code,payType:defaultPayType},
                    dataType: "json",
                    success: function (result) {
//                JSON.stringify(result);//json????????????
//                JSON.parse(jsonStr);//????????????json
                        //??????openid
                        if (result.code==0){
                            openid=result.message;
                        } else{
                            alert(result.message);
                        }
                        loadPayItems();
                    },
                    error: function (req, status, err) {
                        console.info(status + "," + err);
                        loadPayItems();
                    }
                });
            }
        }else{
            loadPayItems();
        }
    });

    function clickGoodsItem(obj, goodsId) {
        $("tr[id^='goodsItemID']").each(function () {
            if (this === obj) {
                $("#" + this.id).css("background", "#87ddd9");
            } else {
                $("#" + this.id).css("background", "white");
            }
        });

        for (var j = 0, len = goodsItems.length; j < len; j++) {
            if (goodsItems[j].id == goodsId) {
                currentGoods = goodsItems[j];
                break;
            }
        }
    }

    var currentUser;
    var loadingUser=false;
    function queryUser(userId) {
        currentUser = null;
        if (userId.length == 6) {
            loadingUser = true;
            $("#myMsg").css("display", "");

            $.ajax({
                timeout: 6000,
                async: true,
                type: "GET",
                url: "<%=basePath%>/user!loadUserMsg.action",
                data: {userId: userId},
                dataType: "json",
                success: function (result) {
                    if (result.code == 0) {
                        var user = JSON.parse(result.message);
                        $("#userImg").attr("src", "");
                        if (user.userId) {
                            currentUser = user;
                            $("#userImg").attr("src", user.headimgurl);
                            $("#userName").html(user.name);
                        } else {
                            $("#myMsg").css("display", "none");
                            alert("??????ID???" + userId + "??????????????????");
                        }
                    } else {
                        $("#myMsg").css("display", "none");
                        alert("??????ID???" + userId + "??????????????????");
                    }
                    loadingUser = false;
                },
                error: function (req, status, err) {
                    loadingUser = false;
                    console.info(status + "," + err);
                }
            });
        } else {
            $("#myMsg").css("display", "none");
        }
    }

    function pay(payType) {
        if(!currentUser.payBindId){
            alert("??????????????????????????????????????????")
            return;
        }
        if (defaultPayType.length>0){
            payType = defaultPayType;
        }
        var currentMark;
        var browser = {
            versions: function () {
                var u = navigator.userAgent, app = navigator.appVersion;
                return {   //?????????????????????????????????
                    trident: u.indexOf('Trident') > -1, //IE??????
                    presto: u.indexOf('Presto') > -1, //opera??????
                    webKit: u.indexOf('AppleWebKit') > -1, //?????????????????????
                    gecko: u.indexOf('Gecko') > -1 && u.indexOf('KHTML') == -1, //????????????
                    mobile: !!u.match(/AppleWebKit.*Mobile.*/), //?????????????????????
                    ios: !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), //ios??????
                    android: u.indexOf('Android') > -1 || u.indexOf('Linux') > -1, //android?????????uc?????????
                    iPhone: u.indexOf('iPhone') > -1, //?????????iPhone??????QQHD?????????
                    iPad: u.indexOf('iPad') > -1, //??????iPad
                    webApp: u.indexOf('Safari') == -1 //??????web????????????????????????????????????
                };
            }(),
            language: (navigator.browserLanguage || navigator.language).toLowerCase()
        };

        if (browser.versions.mobile) {//????????????????????????????????????browser???????????????
            var ua = navigator.userAgent.toLowerCase();//????????????????????????

            if (ua.match(/MicroMessenger/i) == "micromessenger") {
                //??????????????????
                var height = document.body.scrollHeight;//-$(".header_table").height();

                $("#div_help").css("height", height);
                if (!inWeixin){
                    if (browser.versions.ios) {
                        //?????????IOS???????????????
                        $("#div_help").css("background-image", "url('<%=basePath%>/h5/pay/image/ios_h5_help.png')");
                        $("#div_help").css("display", "");
                    }
                    if (browser.versions.android) {
                        //??????????????????????????????
                        $("#div_help").css("background-image", "url('<%=basePath%>/h5/pay/image/android_h5_help.png')");
                        $("#div_help").css("display", "");
                    }
                    return;
                }
            }
                if (browser.versions.ios) {
                    //?????????IOS???????????????
                    currentMark="wapios";
                }else if (browser.versions.android) {
                    //??????????????????????????????
                    currentMark="wapandroid";
                }else{
                    currentMark="wap";
                }
//            if (ua.match(/WeiBo/i) == "weibo") {
//                //??????????????????????????????
//            }
//            if (ua.match(/QQ/i) == "qq") {
//                //???QQ????????????
//            }
        } else {
            alert("???????????????????????????");
            return;
        }

        if (currentGoods == null) {
            alert("?????????????????????")
            return;
        }

        if (loadingUser==true){
            var timer;
            var handler = function(){
                clearInterval(timer);

                pay(payType);
            }
            timer = setInterval(handler , 1000);
        }else if (currentUser != null) {
            $.ajax({
                timeout: 6000,
                async: true,
                type: "GET",
                url: "<%=basePath%>/support!ovali_com.action",
                data: {
                    userId: currentUser.userId,
                    payType: payType == '' ? goodsResult.payType : payType,
                    flat_id: currentUser.flatId,
                    openid:openid,
                    server_id: currentUser.enterServer,
                    p: "upstream",
                    total_fee: currentGoods.amount * 100,
                    itemid: currentGoods.id,
                    c:currentMark,
                    k: "sign"
                },
                dataType: "json",
                success: function (result) {
//                JSON.stringify(result);//json????????????
//                JSON.parse(jsonStr);//????????????json
                    if (payType.indexOf("weixinjsapi")>=0){
                        if (result.hasOwnProperty("url")) {
                            weixinPay(result.url);
                        }else{
                            alert(result.msg);
                        }
                    } else{
                        if (result.hasOwnProperty("url") && result.url.hasOwnProperty("pay_info") && result.url.pay_info.length > 0) {
                            window.location = result.url.pay_info;
                        } else if (payType == '' && goodsResult.sparePay.length > 0) {
                            pay(goodsResult.sparePay);
                        } else {
                            alert(result.msg);
                        }
                    }
                },
                error: function (req, status, err) {
                    console.info(status + "," + err);
                }
            });
        }else{
            if ($("#oUserId").val().trim().length==0) {
                alert("???????????????ID");
            }else{
                alert("??????ID?????????");
            }
            return;
        }
    }

    var appId,timeStamp,nonceStr,package0,signType,paySign;

    function weixinPay(result){
                appId = result.appId;
                timeStamp = result.timeStamp;
                nonceStr = result.nonceStr;
                package0 = result.package;
                signType = result.signType;
                paySign = result.paySign;

                if (typeof WeixinJSBridge == "undefined") {
                    if (document.addEventListener) {
                        document.addEventListener('WeixinJSBridgeReady',
                            onBridgeReady, false);
                    } else if (document.attachEvent) {
                        document.attachEvent('WeixinJSBridgeReady',
                            onBridgeReady);
                        document.attachEvent('onWeixinJSBridgeReady',
                            onBridgeReady);
                    }
                } else {
                    onBridgeReady();
                }
    }
    function onBridgeReady() {
        WeixinJSBridge.invoke('getBrandWCPayRequest', {
                "appId": appId,     //???????????????,???????????????
                "timeStamp": timeStamp,         //?????????,???1970??????????????????
                "nonceStr": nonceStr, //?????????
                "package": package0,
                "signType": signType,         //?????????????????????
                "paySign": paySign //????????????
            },
            function (res) {
                if (res.err_msg == "get_brand_wcpay_request:ok") {
                    console.log('????????????');
                    //??????????????????????????????
                } else if (res.err_msg == "get_brand_wcpay_request:cancel") {
                    console.log('????????????');
                } else if (res.err_msg == "get_brand_wcpay_request:fail") {
                    console.log('????????????');
                    WeixinJSBridge.call('closeWindow');
                } //????????????????????????????????????,???????????????????????????res.err_msg?????????????????????????????????ok,?????????????????????????????????
            });
    }
</script>
</html>