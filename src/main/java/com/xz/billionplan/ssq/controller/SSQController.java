package com.xz.billionplan.ssq.controller;

import com.xz.billionplan.ssq.bean.SSQBean;
import com.xz.billionplan.ssq.service.DLTService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Package: com.xz.billionplan.dlt.controller
 * @ClassName: DLTController
 * @Author: xz
 * @Date: 2020/8/21 9:49
 * @Version: 1.0
 */
@RestController
@RequestMapping("ssq")
@Slf4j
public class SSQController {

    @Autowired
    private DLTService dltService;

    /**
     * 不结合往期号码，幸运随机
     * @return 幸运号码
     */
    @GetMapping("/lucky")
    public String lucky(){
        log.info("获取幸运号码");
        long start = System.currentTimeMillis();
        while (true) {
            if(System.currentTimeMillis() - start >= 10000){
                return "重新运行";
            }
            List<Integer> standard = randomNum();
            List<List<Integer>> list = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                list.add(randomNum());
            }
            Map<String, AtomicInteger> statistics = statistics(standard, list);
//            statistics.forEach((k, v) -> System.out.println(k + " : " + v.get()));
            if (statistics.get("一等奖").get() > 0) {
                log.info("幸运号码:{}",standard.toString());
                List<Integer> result = standard.subList(0, 6);
                result.sort(Integer::compareTo);
                result.add(standard.get(6));
                return result.toString();
            }
        }
    }

    /**
     * 概率分析，一组最大概率，一组最小概率
     * @return 概率号码
     */
    @GetMapping("/probability")
    public String probability(int times){
        log.info("获取概率号码");
        List<SSQBean> list = dltService.list();
        if(times <= 0){
            times = list.size();
        }else if(times > list.size()){
            times = list.size();
        }
        Map<String,Integer> redMap = new HashMap<>();
        Map<String,Integer> blueMap = new HashMap<>();
        for (int i = 0; i < times; i++) {
            String[] split = list.get(i).getAllNum().split(",");
            for (int j = 0; j < 5; j++) {
                if(!redMap.containsKey(split[j])){
                    redMap.put(split[j],0);
                }else{
                    redMap.put(split[j],redMap.get(split[j])+1);
                }
            }
            for (int j = 5; j < split.length; j++) {
                if(!blueMap.containsKey(split[j])){
                    blueMap.put(split[j],0);
                }else{
                    blueMap.put(split[j],blueMap.get(split[j])+1);
                }
            }
        }
        ArrayList<Map.Entry<String, Integer>> entries1 = new ArrayList<>(redMap.entrySet());
        entries1.sort(Comparator.comparingInt(Map.Entry::getValue));
        ArrayList<Map.Entry<String, Integer>> entries2 = new ArrayList<>(blueMap.entrySet());
        entries2.sort(Comparator.comparingInt(Map.Entry::getValue));
        List<String> high = new ArrayList<>();
        List<String> low = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            high.add(entries1.get(i).getKey());
            low.add(entries1.get(entries1.size()-1-i).getKey());
        }
        for (int i = 5; i < 7; i++) {
            high.add(entries2.get(i).getKey());
            low.add(entries2.get(entries2.size()-1-i).getKey());
        }
        List<String> result1 = high.subList(0, 6);
        result1.sort(Comparator.comparingInt(Integer::parseInt));
        result1.add(high.get(6));
        List<String> result2 = low.subList(0, 6);
        result2.sort(Comparator.comparingInt(Integer::parseInt));
        result2.add(low.get(6));
        List<List<String>> result = new ArrayList<>();
        result.add(result1);
        result.add(result2);
        return result.toString();
    }

    /**
     * 趋势分析，算出基准值，算出趋势
     * @return 概率号码
     */
    @GetMapping("/trend")
    public String trend(int times) throws Exception {
        log.info("获取趋势号码");
        List<SSQBean> list = dltService.list();
        if(times <= 0){
            times = list.size();
        }else if(times > list.size()){
            times = list.size();
        }
        Map<String,String> map = new TreeMap<>();
        list.sort(Comparator.comparingInt(item->-item.getId()));
        for (int i = 0; i < times ; i++) {
            SSQBean bean = list.get(i);
            for (int j = 1; j < 8; j++) {
                Class<? extends SSQBean> clazz = SSQBean.class;
                Method method = clazz.getMethod("getNum" + j);
                if(map.containsKey("num"+j)){
                    map.put("num"+j,String.valueOf(Integer.parseInt(map.get("num"+j))+(int)method.invoke(bean)));
                }else{
                    map.put("num"+j,(int)method.invoke(bean)+"");
                }
            }
        }
        SSQBean bean = list.get(0);
        int finalTimes = times;
        map.forEach((k, v)-> {
            int i = Integer.parseInt(v) / finalTimes;
            String s = "get" + k.substring(0, 1).toUpperCase() + k.substring(1);
            Class<SSQBean> clazz = SSQBean.class;
            try {
                Method method = clazz.getMethod(s);
                map.put(k,(i==(int)method.invoke(bean)?"":i>(int)method.invoke(bean)?"+":"-")+i);
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        });
        return map.toString();
    }

    private List<Integer> randomNum() {
        List<Integer> list = new ArrayList<>();
        while (list.size() < 7) {
            if (list.size() < 5) {
                Integer i = new Random().nextInt(33) + 1;
                if (!list.contains(i)) {
                    list.add(i);
                }
            } else {
                Integer i = new Random().nextInt(16) + 1;
                if (!list.contains(i)) {
                    list.add(i);
                }
            }

        }
        return list;
    }

    private Map<String, AtomicInteger> statistics(List<Integer> standard, List<List<Integer>> list) {
        Map<String, AtomicInteger> map = new HashMap<>();
        map.put("一等奖", new AtomicInteger(0));
        map.put("二等奖", new AtomicInteger(0));
        map.put("三等奖", new AtomicInteger(0));
        map.put("四等奖", new AtomicInteger(0));
        map.put("五等奖", new AtomicInteger(0));
        map.put("六等奖", new AtomicInteger(0));
        map.put("没中奖", new AtomicInteger(0));
        List<Integer> redStandard = standard.subList(0, 5);
        List<Integer> blueStandard = standard.subList(5, 7);
        list.forEach(item -> {
            List<Integer> red = item.subList(0, 6);
            List<Integer> blue = item.subList(6, 7);
            int blueCount = (int) blue.stream().filter(blueStandard::contains).count();
            int redCount = (int) red.stream().filter(redStandard::contains).count();
            if (redCount == 6 && blueCount == 1) {
                map.get("一等奖").incrementAndGet();
            } else if (redCount == 6) {
                map.get("二等奖").incrementAndGet();
            } else if (redCount == 5 && blueCount == 1) {
                map.get("三等奖").incrementAndGet();
            } else if ((redCount == 5 && blueCount == 0) || (redCount == 4 && blueCount == 1)) {
                map.get("四等奖").incrementAndGet();
            } else if ((redCount == 4 && blueCount == 0) || (redCount == 3 && blueCount == 1)) {
                map.get("五等奖").incrementAndGet();
            } else if (blueCount == 1) {
                map.get("六等奖").incrementAndGet();
            } else {
                map.get("没中奖").incrementAndGet();
            }
        });
        return map;
    }
}
