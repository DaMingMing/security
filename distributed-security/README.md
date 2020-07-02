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


OAuth2.0的服务提供方涵盖两个服务，即授权服务 (Authorization Server，也叫认证服务) 和资源服务 (Resource
Server)，使用 Spring Security OAuth2 的时候你可以选择把它们在同一个应用程序中实现，也可以选择建立使用
同一个授权服务的多个资源服务。
授权服务 (Authorization Server）应包含对接入端以及登入用户的合法性进行验证并颁发token等功能，对令牌
的请求端点由 Spring MVC 控制器进行实现，下面是配置一个认证服务必须要实现的endpoints：
AuthorizationEndpoint 服务于认证请求。默认 URL： /oauth/authorize 。
TokenEndpoint 服务于访问令牌的请求。默认 URL： /oauth/token 。
资源服务 (Resource Server)，应包含对资源的保护功能，对非法请求进行拦截，对请求中token进行解析鉴
权等，下面的过滤器用于实现 OAuth 2.0 资源服务：
OAuth2AuthenticationProcessingFilter用来对请求给出的身份令牌解析鉴权。

认证流程如下：
1、客户端请求UAA授权服务进行认证。
2、认证通过后由UAA颁发令牌。
3、客户端携带令牌Token请求资源服务。
4、资源服务校验令牌的合法性，合法即返回资源信息