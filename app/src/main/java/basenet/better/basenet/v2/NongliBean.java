package basenet.better.basenet.v2;

/*
{
    "status": 200,//成功状态
    "message": "success",//成功
    "data": {
        "year": 2017,//当前传参公历年
        "month": 2,//当前传参公历月
        "day": 2,//当前传参的公历日
        "lunarYear": 2017,//数字农历年
        "lunarMonth": 1,//数字农历月
        "lunarDay": 6,//数字农历号
        "cnyear": "贰零壹柒 ",//农历中文表示年
        "cnmonth": "正",//农历中文表示月
        "cnday": "初六",//农历中文表示天
        "hyear": "丁酉",//年
        "cyclicalYear": "丙申",//甲子年
        "cyclicalMonth": "辛丑",//甲子月
        "cyclicalDay": "庚申",//甲子日
        "suit": "纳采,订盟,祭祀,求嗣,出火,塑绘,裁衣,会亲友,入学,拆卸,扫舍,造仓,挂匾,掘井,开池,结网,栽种,纳畜,破土,修坟,立碑,安葬,入殓",//宜
        "taboo": "祈福,嫁娶,造庙,安床,谢土",//禁忌
        "animal": "鸡",	//生肖
        "week": "星期四",//星期
        "festivalList": [],//当天节日
        "jieqi": {//当月节气
            "4": "立春",//4日立春
            "19": "雨水"//19日雨水
        },
        "maxDayInMonth": 29,//农历月当前月天数
        "leap": false,//是否是闰月
        "bigMonth": false,//是否是大月
        "lunarYearString": "丁酉"//农历年
    }
}
 */
public class NongliBean {

    public int year;
    public int month;
    public int day;

    public int lunarYear;
    public int lunarMonth;
    public int lunarDay;

    public String suit;
    public String taboo;
}
