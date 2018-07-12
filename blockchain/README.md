# blockchain
区块链学习java实现的简单区块链基础代码
源码参考了江南的blog和github上的代码

理论篇的blog请参考如下
[第一篇：区块链通识基础知识总结](https://blog.csdn.net/zjcjava/article/details/80933057)

[第二篇：区块链的结构和相关技术概述](https://blog.csdn.net/zjcjava/article/details/80950749)


```

├─java
│  └─com
│      └─bamboo
│          └─blockchain
│              ├─model1
│              ├─model2
│              └─utils
└─resources
```
mode1是基础部分实现
mode2是使用了交易的简单实现



启动方式：

BlockUtils 简单区块结构
BlockPowUtils POW验证
BlockTransactionUtils 交易

查询余额
get http://localhost:4567/


转账
post http://localhost:4567/
{"type":"send","data":"5"}

挖矿，把交易记录记录进数据链中
post http://localhost:4567/
{"type":"mine","data":"1"}

