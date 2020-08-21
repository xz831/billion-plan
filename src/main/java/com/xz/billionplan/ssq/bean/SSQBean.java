package com.xz.billionplan.ssq.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Package: com.xz.billionplan.dlt.bean
 * @ClassName: DLTBean
 * @Author: xz
 * @Date: 2020/8/21 10:00
 * @Version: 1.0
 */
@Data
@TableName("facaibiao")
public class SSQBean {

    private int id;
    private int num1;
    private int num2;
    private int num3;
    private int num4;
    private int num5;
    private int num6;
    private int num7;
    private String allNum;
}
