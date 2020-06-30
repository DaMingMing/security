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


分布式系统认证技术方案：
流程描述：
（1）用户通过接入方（应用）登录，接入方采取OAuth2.0方式在统一认证服务(UAA)中认证。
（2）认证服务(UAA)调用验证该用户的身份是否合法，并获取用户权限信息。
（3）认证服务(UAA)获取接入方权限信息，并验证接入方是否合法。
（4）若登录用户以及接入方都合法，认证服务生成jwt令牌返回给接入方，其中jwt中包含了用户权限及接入方权
限。
（5）后续，接入方携带jwt令牌对API网关内的微服务资源进行访问。
（6）API网关对令牌解析、并验证接入方的权限是否能够访问本次请求的微服务。
（7）如果接入方的权限没问题，API网关将原请求header中附加解析后的明文Token，并将请求转发至微服务。
（8）微服务收到请求，明文token中包含登录用户的身份和权限信息。因此后续微服务自己可以干两件事：1，用
户授权拦截（看当前用户是否有权访问该资源）2，将用户信息存储进当前线程上下文（有利于后续业务逻辑随时
获取当前用户信息）
流程所涉及到UAA服务、API网关这两个组件职责如下：
1）统一认证服务(UAA)
它承载了OAuth2.0接入方认证、登入用户的认证、授权以及生成令牌的职责，完成实际的用户认证、授权功能。
2）API网关
作为系统的唯一入口，API网关为接入方提供定制的API集合，它可能还具有其它职责，如身份验证、监控、负载均
衡、缓存等。API网关方式的核心要点是，所有的接入方和消费端都通过统一的网关接入微服务，在网关层处理所
有的非业务功能。

OAauth2.0包括以下角色：
1、客户端
本身不存储资源，需要通过资源拥有者的授权去请求资源服务器的资源，比如：Android客户端、Web客户端（浏
览器端）、微信客户端等。
2、资源拥有者
通常为用户，也可以是应用程序，即该资源的拥有者。
3、授权服务器（也称认证服务器）
用于服务提供商对资源拥有的身份进行认证、对访问资源进行授权，认证成功后会给客户端发放令牌
（access_token），作为客户端访问资源服务器的凭据。本例为微信的认证服务器。
4、资源服务器
存储资源的服务器，本例子为微信存储的用户信息。
现在还有一个问题，服务提供商能允许随便一个客户端就接入到它的授权服务器吗？答案是否定的，服务提供商会
给准入的接入方一个身份，用于接入时的凭据:
client_id：客户端标识 client_secret：客户端秘钥
因此，准确来说，授权服务器对两种OAuth2.0中的两个角色进行认证授权，分别是资源拥有者、客户端。