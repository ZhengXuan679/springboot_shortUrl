package cn.zhengxuan.controller;

import cn.zhengxuan.pojo.Url;
import cn.zhengxuan.service.LongurlService;
import cn.zhengxuan.service.ShortUrlService;
import cn.zhengxuan.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller
@EnableAutoConfiguration
@Transactional
public class ShortUrlController {

    @Autowired
    private ShortUrlService shortUrlService;

    @Autowired
    private LongurlService longurlService;

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;



    @RequestMapping("/shortUrl")
    @ResponseBody
    Map<String, Object> home(@RequestBody Url url,HttpServletRequest request) {

        Integer count = (Integer) request.getSession().getAttribute("count");
        System.out.println("================="+count);
        if (count == null) {
           request.getSession().setAttribute("count",1);

        } else {
            count++;
            request.getSession().setAttribute("count",count);
        }


        Map<String, Object> map = new HashMap<>();
        if(url!=null){
            String[] aResult = shortUrl(url.getLongUrl(), (int) url.getLength(),url.getKeyword());
            for (int i = 0; i < aResult.length; i++) {
                System.out.println("[" + i + "]:" + aResult[i]);
            }
            Random random=new Random();
            int t=0;
            if(32%(aResult.length)!=0){
                t=32/aResult.length-1;
            }else{
                t=32/aResult.length;
            }
            int j=random.nextInt(t-1);
            System.out.println("j="+j);
            url.setShortUrl(aResult[j]);
            shortUrlService.insert_shortUrl(url);

            map.put("returnCode", 200);
            map.put("msg", "成功");
            map.put("count", request.getSession().getAttribute("count"));
            map.put("data", aResult[j]);

        }



        return map;
    }


    @RequestMapping("/zhengxuan/{ss}")
    public ModelAndView jumpLongLink(HttpServletRequest request, ModelAndView mav, @PathVariable("ss")String ss) {

        String longUrl = "";
        System.out.println("ss="+ss);
        String longurl = longurlService.longurl(ss);

        if (longUrl!=null) {

            longUrl = longurl;
        }
        mav.setViewName("redirect:" + longUrl);
        return mav;
    }



    /**
     * 根据长度生成短链接
     * @param url 代表长链接
     * @param length 代表短链接的长度
     * @return
     */
    public static String[] shortUrl(String url,int length,String key) {
        // 可以自定义生成 MD5 加密字符传前的混合 KEY
       // String key = "test";
        // 要使用生成 URL 的字符
        int leg=0;
        if(leg%length!=0){
            leg=(32/length)-1;
        }else{
            leg=(32/length);
        }
        length=Integer.valueOf(length);

        System.out.println("leg="+leg);
        String[] chars = new String[] { "a", "b", "c", "d", "e", "f", "g", "h",
                "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
                "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
                "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H",
                "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
                "U", "V", "W", "X", "Y", "Z"

        };
        // 对传入网址进行 MD5 加密
        String hex = md5ByHex(key + url);

        String[] resUrl = new String[leg];
        for (int i = 0; i < leg; i++) {

            // 把加密字符按照 8 位一组 16 进制与 0x3FFFFFFF 进行位与运算
            String sTempSubString = hex.substring(i * length, i * length + length);

            // 这里需要使用 long 型来转换，因为 Inteper .parseInt() 只能处理 31 位 , 首位为符号位 , 如果不用long ，则会越界
            long lHexLong = 0x3FFFFFFF & Long.parseLong(sTempSubString, 16);
            String outChars = "";
            for (int j = 0; j < length; j++) {
                // 把得到的值与 0x0000003D 进行位与运算，取得字符数组 chars 索引
                long index = 0x0000003D & lHexLong;
                // 把取得的字符相加
                outChars += chars[(int) index];
                // 每次循环按位右移 5 位
                lHexLong = lHexLong >> (length-1);
            }
            // 把字符串存入对应索引的输出数组
            resUrl[i] = outChars;
        }
        return resUrl;
    }

    /**
     * MD5加密(32位大写)
     * @param src
     * @return
     */
    public static String md5ByHex(String src) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] b = src.getBytes();
            md.reset();
            md.update(b);
            byte[] hash = md.digest();
            String hs = "";
            String stmp = "";
            for (int i = 0; i < hash.length; i++) {
                stmp = Integer.toHexString(hash[i] & 0xFF);
                if (stmp.length() == 1)
                    hs = hs + "0" + stmp;
                else {
                    hs = hs + stmp;
                }
            }
            return hs.toUpperCase();
        } catch (Exception e) {
            return "";
        }

    }
}
