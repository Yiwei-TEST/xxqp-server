package com.sy.sanguo.common.util.hnliantong;


import java.security.MessageDigest;

public class MD5Utils
{
  public static final String MD5(String paramString)
  {
    paramString = paramString + "acr098lklsssbuek2uendkl";
    char[] arrayOfChar1 = { '1', 'A', 'E', '2', '3', '0', '4', 'C', '5', '6', 'D', 'F', '7', '8', '9', 'B' };
    try
    {
      byte[] arrayOfByte1 = paramString.getBytes();
      MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
      localMessageDigest.update(arrayOfByte1);
      byte[] arrayOfByte2 = localMessageDigest.digest();
      int i = arrayOfByte2.length;
      char[] arrayOfChar2 = new char[i * 2];
      int j = 0;
      for (int k = 0; k < i; ++k)
      {
        int l = arrayOfByte2[k];
        arrayOfChar2[(j++)] = arrayOfChar1[(l >>> 4 & 0xF)];
        arrayOfChar2[(j++)] = arrayOfChar1[(l & 0xF)];
      }
      return a(new String(arrayOfChar2));
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    return null;
  }

  private static String a(String paramString)
  {
    char[] arrayOfChar = paramString.toCharArray();
    for (int i = 0; i < arrayOfChar.length; ++i)
      arrayOfChar[i] = (char)(arrayOfChar[i] ^ 0x74);
    String str = new String(arrayOfChar);
    return str;
  }

  public static void main(String[] paramArrayOfString)
  {
    System.out.println(MD5("9372592mj113"));
  }
}

/* Location:           F:\cocos\guajiqt\proj.android.liantong\libs\sdk-pay-yyplatform-1.0.0.4.jar
 * Qualified Name:     com.bonc.common.tools.MD5Utils
 * JD-Core Version:    0.5.4
 */