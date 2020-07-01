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


认证流程：
用户提交账号密码--->UsernamePasswordAuthenticationFilter处理--->交给AuthenticationManager处理
--->委托DaoAuthenticationProvider验证--->通过UserDetailService判断用户是否存在--->存在则返回查询到UserDetails
--->通过密码编码器对比UserDetails跟用户输入的密码是否一致--->一致后将用户信息Authentication保存至安全上下文

授权流程：
认证通过后用户访问受保护的资源--->被FilterSecurityInterceptor拦截--->SecurityMetadatasource获取访问当前资源所需要的的权限并返回Collection
--->AccessDecisionnManager（决策管理器）通过投票决策--->默认实现类AffirmativeBased通过对比资源所需要的权限、用户所有的权限，投票决策
--->决策通过--->允许访问资源，请求放行

授权的方式包括 web授权和方法授权，web授权是通过 url拦截进行授权，方法授权是通过 方法拦截进行授权。他
们都会调用accessDecisionManager进行授权决策，若为web授权则拦截器为FilterSecurityInterceptor；若为方
法授权则拦截器为MethodSecurityInterceptor。如果同时通过web授权和方法授权则先执行web授权，再执行方
法授权，最后决策通过，则允许访问资源，否则将禁止访问。

web授权注意：
规则的顺序是重要的,更具体的规则应该先写；

方法授权：
有三种注解方式：@PreAuthorize,@PostAuthorize, @Secured，一般建议使用PreAuthorize，且在控制层注解；



会话：
用户认证通过后，为了避免用户的每次操作都进行认证可将用户的信息保存在会话中。spring security提供会话管
理，认证通过后将身份信息放入SecurityContextHolder上下文，SecurityContext与当前线程进行绑定，方便获取
用户身份。
会话有四种机制：
1、always--如果没有session存在就创建一个
2、ifRequired--如果需要就创建一个Session（默认）登录时
3、never--SpringSecurity 将不会创建Session，但是如果应用中其他地方创建了Session，那么SpringSecurity将会使用它。
4、stateless--SpringSecurity将绝对不会创建Session，也不使用Session，这种无状态架构适用于REST API及其无状态认证机制。
