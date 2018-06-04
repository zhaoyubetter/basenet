1. RequestHandler 好封装；
2. ResponseHandler 不好封装，因为：
    a.如果需要json解析，basenet需要导入gson库；
    b.需要包装一下AbsRequestCallback;
    c.需要重新创建一个新Builder类，用来创建网络请求，如：NetRequest_bak，示例的这个，是不完美的；
    d.需要重新创建一个新的Response类，表示新app的后端返回数据, 错误信息，message 字段也是不一样的；

    所以这里还是放入具体app的网络层去重写吧。这里只是demo；^_^