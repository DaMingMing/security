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



授权服务配置总结：授权服务配置分成三大块，可以关联记忆。
1、既然要完成认证，它首先得知道客户端信息从哪儿读取，因此要进行客户端详情配置。
2、既然要颁发token，那必须得定义token的相关endpoint，以及token如何存取，以及客户端支持哪些类型的token。
3、既然暴露除了一些endpoint，那对这些endpoint可以定义一些安全上的约束等


授权模式：
1、授权码模式：
四种模式中最安全的一种模式。一般用于client是Web服务器端应用或第三方的原生App调用资源服务的时候。
因为在这种模式中access_token不会经过浏览器或移动端的App，而是直接从服务端去交换，这样就最大限度的减小了令牌泄漏的风险。
流程：
（1）资源拥有者打开客户端，客户端要求资源拥有者给予授权，它将浏览器被重定向到授权服务器，重定向时会附加客户端的身份信息。请求如下：
/uaa/oauth/authorize?client_id=c1&response_type=code&scope=all&redirect_uri=http://www.baidu.com
（2）浏览器出现向授权服务器授权页面，之后将用户同意授权。
（3）授权服务器将授权码（AuthorizationCode）转经浏览器发送给client(通过redirect_uri)。
（4）客户端拿着授权码向授权服务器索要访问access_token，请求如下：
/uaa/oauth/token?client_id=c1&client_secret=secret&grant_type=authorization_code&code=5PgfcD&redirect_uri=http://www.baidu.com
（5）授权服务器返回令牌(access_token)
2、简化模式：
简化模式用于没有服务器端的第三方单页面应用，因为没有服务器端就无法接收授权码。
流程：
（1）资源拥有者打开客户端，客户端要求资源拥有者给予授权，它将浏览器被重定向到授权服务器，重定向时会附加客户端的身份信息。如：
/uaa/oauth/authorize?client_id=c1&response_type=token&scope=all&redirect_uri=http://www.baidu.com
（2）浏览器出现向授权服务器授权页面，之后将用户同意授权。
（3）授权服务器将授权码将令牌（access_token）以Hash的形式存放在重定向uri的fargment中发送给浏览器。
3、密码模式：
密码模式因有密码泄露风险，一般用于我们自己开发的，第一方原生App或第一方单页面应用。
流程：
（1）资源拥有者将用户名、密码发送给客户端
（2）客户端拿着资源拥有者的用户名、密码向授权服务器请求令牌（access_token），请求如下：
/uaa/oauth/token?client_id=c1&client_secret=secret&grant_type=password&username=shangsan&password=123
（3）授权服务器将令牌（access_token）发送给client。
4、客户端模式：
这种模式是最方便但最不安全的模式。因此这就要求我们对client完全的信任，而client本身也是安全的。一般用来提供给我们完全信任的服务器端服务。比如，合作方系统对接，拉取一组用户信息。
流程：
（1）客户端向授权服务器发送自己的身份信息，并请求令牌（access_token）
（2）确认客户端身份无误后，将令牌（access_token）发送给client，请求如下：
/uaa/oauth/token?client_id=c1&client_secret=secret&grant_type=client_credentials