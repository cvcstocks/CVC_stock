package com.stock.mx2.utils;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    /**
     * unicode編碼轉換為漢字
     * @param unicodeStr 待轉化的編碼
     * @return 返迴轉化后的漢子
     */
    public static String UnicodeToCN(String unicodeStr) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(unicodeStr);
        char ch;
        while (matcher.find()) {
            //group
            String group = matcher.group(2);
            //ch:'李四'
            ch = (char) Integer.parseInt(group, 16);
            //group1
            String group1 = matcher.group(1);
            unicodeStr = unicodeStr.replace(group1, ch + "");
        }

        return unicodeStr.replace("\\", "").trim();
    }

    /**
     * 漢字轉化為Unicode編碼
     * @param CN 待轉化的中文
     * @return 返迴轉化之後的unicode編碼
     */
    public static String CNToUnicode(String CN) {

        try {
            StringBuffer out = new StringBuffer("");
            //直接獲取字符串的unicode二進制
            byte[] bytes = CN.getBytes("unicode");
            //然後將其byte轉換成對應的16進製表示即可
            for (int i = 0; i < bytes.length - 1; i += 2) {
                out.append("\\u");
                String str = Integer.toHexString(bytes[i + 1] & 0xff);
                for (int j = str.length(); j < 2; j++) {
                    out.append("0");
                }
                String str1 = Integer.toHexString(bytes[i] & 0xff);
                out.append(str1);
                out.append(str);
            }
            return out.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String delHTMLTag(String htmlStr){
        String regEx_script="<script[^>]*?>[\\s\\S]*?<\\/script>"; //定義script的正則表達式
        String regEx_style="<style[^>]*?>[\\s\\S]*?<\\/style>"; //定義style的正則表達式
        String regEx_html="<[^>]+>"; //定義HTML標籤的正則表達式

        Pattern p_script=Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE);
        Matcher m_script=p_script.matcher(htmlStr);
        htmlStr=m_script.replaceAll(""); //過濾script標籤

        Pattern p_style=Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE);
        Matcher m_style=p_style.matcher(htmlStr);
        htmlStr=m_style.replaceAll(""); //過濾style標籤

        Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE);
        Matcher m_html=p_html.matcher(htmlStr);
        htmlStr=m_html.replaceAll(""); //過濾html標籤

        return htmlStr.trim(); //返迴文本字符串
    }



}
