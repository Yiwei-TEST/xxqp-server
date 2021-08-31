package com.sy.sanguo.common.util.yinlian;

/**
 * @author
 */
/**
 * @author
 */
public class Constants {
  //----商户信息：商户根据对接的实际情况对下面数据进行修改； 以下数据在测试通过后，部署到生产环境，需要替换为生产的数据----
  //商户编号，由易联产生，邮件发送给商户(测试)
  //public static final String MERCHANT_ID = "502050000074";
  //正式
  public static final String MERCHANT_ID = "502020000819";
  //商户接收订单通知接口地址；
  //public static final String MERCHANT_NOTIFY_URL = "http://www.xxxxx.com/Notify.do";
  //商户RSA私钥，商户自己产生（可采用易联提供RSA工具产生）
  public static final String MERCHANT_RSA_PRIVATE_KEY ="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKJ4JpA4gOrhUui8tf3DYE48o/Jjq1HbugVrnzJl02F1cd+LBelw6V4u5vR+EupkqG5HHh1KW6MKA7kBwxI3Ft5bAvc2vYSN2FgFk1o3Q0yPr2vuoc3nE9XtGV2uI4FF3FZauHpuuM3BxVv8xYsgaBekWaXLH4t1L7tzhet9tQDVAgMBAAECgYBrL8OZYH3E+DT/sQiMS6qs0xsCnjj3v3PhgZg84qrrYFVpOsoCseh0p5LdeJO4KgJpBuhaQKVmUgQUuZHd4ZRmvwIqJXytsZR8g2stkJjFPu2W7sUg3X1XgTiCsb7pbH74//hs56vv2MIan9TJFzwy5XPETBFYVodIq4jVKQgYoQJBANIs4akzGcg0cSdIeXAviL83UIzvMvnOSK2sJpsy2m3i6qoYBl3XDmHDTZWfKITxfuVIArBKOQh5mPK33yPCNHkCQQDF5ISwfJKzZB8wJOtmp3Mgb+zXcgLE4N4eDPjeMwwU+XoYgXJUn44lGguYybflpl2HLZK9lPpGlVmOa3xPQoA9AkAlBxcykfgm12Zf/1q1swoqdfpQ+gtHC1qujje2+/yTg3jIQrdMbQjxnFDc3U9sLIeoqUkf+dOWdKXHjTFYwWwxAkBLURkLTq5K5m1UVojdbhfMHa0npnXVVrxS9Z72erk7dmP41bOc74kbJ9mIEiYSmon4O+kgjIXgvVDzeoc6Z1YFAkA2T0wig08hE249lafoMdNtNRBX+NznQsoxKrX7rep5w0j61ZcyKvs7jVrK7ZNie/6TFQsz90azxiIgzEsomI23";
  //测试环境
  //public static final String MERCHANT_RSA_PRIVATE_KEY ="MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMKxT0pjZmhVBm552zfRpNmAErZgzzNUX8qFElAi3f/LjkOCHu9Q7PbaUL9bjWu9JBfH+wzKzS+VrMiuxzgL0W8+dCGdTR27HY8/map9kZ/EAmzIoHN2P3L98/LluUQwjctM8hHU7IenRAzpXF+skbLIvAfBxUQSw/cpaA/zfV0rAgMBAAECgYEAhUjhoWl/ODnNF2Cw5PvOwV/eb6DC+L7wfTKwoM/d7zopgKnaB7f9Idm/oeFJZdKsnOM5ST6lTchH63NLy2C71RzXLvZX1G6EobapEuGtkXM7eCX0cNhiX5ShMCOWTNpvx+X+y310SVT0xISXtF2gClEpwJQYsJ/1v4IO/vbsDRECQQDiCBYKS2GJl/lPTntXjaglRq37Kfz0T5dxqIlWcuSY/sJHSwk5Cx7SNDS4JYvxOx4oyKVLzuERVlQU+W3sxi1XAkEA3IGF9OdbrqVMMyxrsevNWKjxORhYd5Rbuwhi/lTl53uq5i+xQB6svI9uVDljpjlg0ek33I7vXdPAoyrl9f3WTQJAQCgfaWigFNgIMdSK+f0BXyNCuuneHNKCoAZAhUMzU6HsLyRDR+e6Jdwfoq01nZE0LEtXb78W7z7buuBZrT1LHQJALde7usuyst9otSbiCO5mFa2h4OXf1pSAcaTvt8J8vaBnSk3WAuedr+H95QK9Zdx99YYP+Am7V/1jCkkr3/Vy7QJAO+v2iZrcFIUtIcmNcgbTIhuNcQcI85ndDDkmKzJPwyhOFiFR4d5mJM6EHQq2QQMvuAmfmX8i/TCxtz0A21AJ8w==";



  //----易联信息： 以下信息区分为测试环境和生产环境，商户根据自己对接情况进行数据选择----
  //易联服务器地址_测试环境
  //public static final String PAYECO_URL = "https://testmobile.payeco.com";
  //易联服务器地址_生产环境
  public static final String PAYECO_URL = "https://mobile.payeco.com";

  //订单RSA公钥（易联提供）_测试环境
  //public static final String PAYECO_RSA_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCRxin1FRmBtwYfwK6XKVVXP0FIcF4HZptHgHu+UuON3Jh6WPXc9fNLdsw5Hcmz3F5mYWYq1/WSRxislOl0U59cEPaef86PqBUW9SWxwdmYKB1MlAn5O9M1vgczBl/YqHvuRzfkIaPqSRew11bJWTjnpkcD0H+22kCGqxtYKmv7kwIDAQAB";
	 
  //订单RSA公钥（易联提供）_生产环境
  public static final String PAYECO_RSA_PUBLIC_KEY ="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCoymAVb04bvtIrJxczCT/DYYltVlRjBXEBFDYQpjCgSorM/4vnvVXGRb7cIaWpI5SYR6YKrWjvKTJTzD5merQM8hlbKDucxm0DwEj4JbAJvkmDRTUs/MZuYjBrw8wP7Lnr6D6uThqybENRsaJO4G8tv0WMQZ9WLUOknNv0xOzqFQIDAQAB";
  


}
