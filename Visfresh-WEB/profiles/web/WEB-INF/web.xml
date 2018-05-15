<web-app id="SmartTrace">
    <description>SmartTrace WEB-${instance}</description>

	<context-param>
		<param-name>contextClass</param-name>
		<param-value>org.springframework.web.context.support.AnnotationConfigWebApplicationContext</param-value>
	</context-param>
	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>com.visfresh.init.prod</param-value>
	</context-param>

    <filter>
    	<filter-name>AccessControlAllowOriginFilter</filter-name>
    	<filter-class>com.visfresh.filters.AccessControlAllowOriginFilter</filter-class>
    </filter>
	<filter>
		<filter-name>springSecurityFilterChain</filter-name>
		<filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
	</filter>

    <filter-mapping>
        <filter-name>AccessControlAllowOriginFilter</filter-name>
        <url-pattern>/vf/rest/*</url-pattern>
    </filter-mapping>
	<filter-mapping>
		<filter-name>springSecurityFilterChain</filter-name>
		<url-pattern>/vf/rest/*</url-pattern>
	</filter-mapping>
    <filter-mapping>
        <filter-name>AccessControlAllowOriginFilter</filter-name>
        <url-pattern>/vf/lite/*</url-pattern>
    </filter-mapping>
	<filter-mapping>
		<filter-name>springSecurityFilterChain</filter-name>
		<url-pattern>/vf/lite/*</url-pattern>
	</filter-mapping>

	<listener>
		<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
	</listener>

	<!-- Spring -->
	<servlet>
		<servlet-name>SpringDispatcher</servlet-name>
		<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
		<init-param>
			<param-name>contextClass</param-name>
			<param-value>org.springframework.web.context.support.AnnotationConfigWebApplicationContext</param-value>
		</init-param>
	</servlet>

	<!-- SmartTrace Spring -->
	<servlet-mapping>
		<servlet-name>SpringDispatcher</servlet-name>
		<url-pattern>/vf/*</url-pattern>
	</servlet-mapping>
	<servlet-mapping>
		<servlet-name>SpringDispatcher</servlet-name>
		<url-pattern>/shipment.do</url-pattern>
	</servlet-mapping>
</web-app>
