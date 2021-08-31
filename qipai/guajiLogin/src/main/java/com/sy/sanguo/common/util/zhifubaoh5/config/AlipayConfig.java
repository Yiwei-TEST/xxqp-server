package com.sy.sanguo.common.util.zhifubaoh5.config;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *版本：3.3
 *日期：2012-08-10
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
	
 *提示：如何获取安全校验码和合作身份者ID
 *1.用您的签约支付宝账号登录支付宝网站(www.alipay.com)
 *2.点击“商家服务”(https://b.alipay.com/order/myOrder.htm)
 *3.点击“查询合作者身份(PID)”、“查询安全校验码(Key)”

 *安全校验码查看时，输入支付密码后，页面呈灰色的现象，怎么办？
 *解决方法：
 *1、检查浏览器配置，不让浏览器做弹框屏蔽设置
 *2、更换浏览器或电脑，重新登录查询。
 */

public class AlipayConfig {
	
	//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
	// 合作身份者ID，以2088开头由16位纯数字组成的字符串
	public static String partner = "2088602346104288";
	
	// 交易安全检验码，由数字和字母组成的32位字符串
	// 如果签名方式设置为“MD5”时，请设置该参数
	public static String key = "";
	
    // 商户的私钥
    // 如果签名方式设置为“0001”时，请设置该参数
	public static String private_key = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIUlu8xOO5EuDMiRu4/fpICz5mQ5N3/joYaCbcc8hK6UYLol/31FBOjQufnHD+HsI3A/lpHoR5wHkWugqk1A6A4dc8wh5pvic6n0Mr4UTCkhJ6VmE4mxfoI1WiJ1ardQVX5rnHNbbFh3HNcUfTPzzhc0EoBeI+B80NgQw+lSytPTAgMBAAECgYBfLh82qII6gUHQKyV6bvQIsRG0eZZsDMxN/HT/ZQqwRg9zYnBmG22sBHG91YhMB2M5/arkOLjgGCTjXGIBgbGgRKVutrlbGTFQUfnTX9rpVPJVZVlnDOhvn6CbQuaUL12fCy0GEct84sEA4APn2bgAwRZN+/txyKijgKm+M8V+aQJBAMhN1EbDMoxcTFWY/c7Bc7R9zOod+c8M/10DdfdL5iMfyM8nxuyTotDG9QyeXPdwfdnlLgr2NJnJEtnGUlXOYl8CQQCqK4ScmSn9ifvATdJh3hahfUKQzo+J4TollkeUWEZAO3jan5sRd+pij8N1IExS+2hD788QWM2NXmExhPhcQksNAkB+kQc4K3FOVaf6UnDV61E0VGVd7dECruDRIjWxLGlf+l6F30mIBYXUzFLdRVfTFma/f1wHmykDn5VkvDccdwubAkBooL81pbq6p+dBUtgC0pFpk2GlNUvuA6xDxgWWwTvYbHifOzkV4XwcQ7ZywnwLHYb7Fmxi6abgv+HUl6Q43Y3VAkAxZ6TNPTKrpUghqo+vgNisu0nSsvJawOZ4KNEPUf62lrTprFlADowX28JVzBPe0W1RwekE7AEFS/ybzPhhQXFc";

    // 支付宝的公钥
    // 如果签名方式设置为“0001”时，请设置该参数
	public static String ali_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQComfCPqsJH++f9E2OHH9z7D/wTRytAy5BXiDWorKgx3Mp4vAxHx3gQFyR87PzBI/yw/LkfNT/06K5/j8/elEmL06IteBZCi8D4N8IxeG17tcx/apXA80rbH069hh9ERQinnBKu4A5ufBvrsdoGhxUbw1JrjWUW6Rn2D6TnSIfgyQIDAQAB";

	//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
	

	// 调试用，创建TXT日志文件夹路径
	public static String log_path = "D:\\";

	// 字符编码格式 目前支持  utf-8
	public static String input_charset = "utf-8";
	
	// 签名方式，选择项：0001(RSA)、MD5
	public static String sign_type = "0001";
	// 无线的产品中，签名方式为rsa时，sign_type需赋值为0001而不是RSA

}
