package com.bamboo.blockchain.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Test {




    public  static  void  main (String args []){


        List sales= new ArrayList<Sales>();
        Sales s1= new Sales(1, "李满", "1383838457", "2", "广东省深圳市南市区1223号", new Date());
        Sales s2= new Sales(2, "李名", "1383838454", "3", "广东省深圳市南市区1221号", new Date());
        Sales s3= new Sales(3, "李三", "1383838451", "1", "广东省深圳市南市区1222号", new Date());
        sales.add(s1);
        sales.add(s2);
        sales.add(s3);

        try {
            ExcelUtil.generateXlfile("D://数据导出记录",
                    new String[]{"UID", "姓名", "手机", "云鑫通", "地址", "时间"},
                    new int[]{10, 20, 15, 10, 50, 20},
                    new String[]{"uid", "name", "phone", "yxt", "address", "ctime"},
                    sales);
        }catch (Exception e){

        }


    }
}


class  Sales{

    private  int uid;
    private String name;
    private String phone;
    private String yxt;
    private String address;
    private Date ctime;


    public Sales(int uid, String name, String phone, String yxt, String address, Date ctime) {
        this.uid = uid;
        this.name = name;
        this.phone = phone;
        this.yxt = yxt;
        this.address = address;
        this.ctime = ctime;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getYxt() {
        return yxt;
    }

    public void setYxt(String yxt) {
        this.yxt = yxt;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }
}