# security
基于spring-security方式实现认证，授权。
1、采用spring-boot实现；
2、为了方便测试，认证已内存代替数据库；


原理：
基于FilterChainProxy生成一系列filter，交由spring管理，
其中有4个filter，分别为
1、负责初始化的filter
2、负责认证的UsernamePasswordAuthenticationFilter，干活的是AuthenticationManager(认证管理器)
3、负责授权的FilterSecurityInterceptor,干活的是AccessDecisionnManager（决策管理器）
4、负责处理异常的filter

