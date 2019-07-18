package com.bytedance.androidcamp.network.dou.model;

import java.util.Date;

public class Like {
    public final long id;
    public int count;
    public int state;
    public String url;
    public Date date;
    public String name;

    public Like(long id){this.id=id;}
    public void setCount(int count){this.count=count;}
    public void setState(int state){this.state=state;}
    public void setUrl(String url){this.url =url;}
    public int getCount(){return count;}
    public int getState(){return state;}
    public String getUrl(){return url;}
    public void setDate(Date date){this.date=date;}
    public void setName(String name){this.name=name;}
    public String getName(){return name;}
    public Date getDate(){return date;}
}
