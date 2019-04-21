package cn.zhengxuan.pojo;

import java.util.List;

public interface UrlMapper {
   void insert_shortUrl(Url url);

   String longurl(String shortUrl);
}
