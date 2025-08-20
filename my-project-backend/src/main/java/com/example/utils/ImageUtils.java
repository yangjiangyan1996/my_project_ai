package com.example.utils;

import com.example.entity.base.RespBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Author YangJian
 * @Description
 * @Email 1776080295@qq.com
 * @Date 2025/8/19 20:16
 */
public class ImageUtils {

    public static String getRandomImageName() {
        List<String> list = new ArrayList<>();
        list.add("https://img1.baidu.com/it/u=3715498923,2939634736&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1428");
        list.add("https://img0.baidu.com/it/u=131893562,86665228&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=898");
        list.add("https://img2.baidu.com/it/u=851514045,2013562092&fm=253&fmt=auto&app=120&f=JPEG?w=800&h=1067");
        list.add("https://img1.baidu.com/it/u=2182146859,1446841222&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1421");
        list.add("https://img1.baidu.com/it/u=628860371,3856933897&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1422");
        list.add("https://img0.baidu.com/it/u=3517081069,3188644709&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1422");
        list.add("https://img1.baidu.com/it/u=1444067090,4162561865&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=667");
        list.add("https://img1.baidu.com/it/u=2309258749,2601028906&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=628");
        list.add("https://img2.baidu.com/it/u=2179749649,398913166&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=1255");
        list.add("https://img0.baidu.com/it/u=897907117,495356401&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=889");

        list.add("https://img1.baidu.com/it/u=488307329,791192203&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=624");
        Random random = new Random();
        int num = random.nextInt(11); // 生成 0 到 10 之间的整数
        return list.get(num);
    }
}
