package cn.zhengxuan.service;

import cn.zhengxuan.pojo.Url;
import cn.zhengxuan.pojo.UrlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShortUrlService {
    @Autowired
    private UrlMapper urlMapper;

    public  void insert_shortUrl(Url url){
        urlMapper.insert_shortUrl(url);
    }

}
