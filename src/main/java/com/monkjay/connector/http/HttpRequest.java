package com.monkjay.connector.http;

import com.monkjay.util.Enumerator;
import com.monkjay.util.HttpUtil;
import com.monkjay.util.ParameterMap;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.security.Principal;
import java.util.*;

/**
 * @author monkJay
 * @description
 * @date 2020/2/12 21:43
 */
public class HttpRequest implements HttpServletRequest {

    private String contentType;
    private int contentLength;
    private InetAddress inetAddress;
    private InputStream input;
    private String protocol;
    private String queryString;
    private String requestURI;
    private String serverName;
    private int serverPort;
    private boolean requestedSessionCookie;
    private String requestedSessionId;
    private boolean requestedSessionURL;
    /**
     * 请求的方法 GET / POST / PUT / DELETE...
     */
    private String method;
    /**
     * 请求的上下文路径
     */
    private String contextPath;
    /**
     * cookie数组
     */
    ArrayList<Cookie> cookies = new ArrayList<>();
    /**
     * 请求头信息
     */
    private Map<String, String> headers;
    /**
     * 请求头属性
     */
    private Map<String, String> attribute;
    /**
     * 请求参数是否已经解析过了
     */
    private boolean parsed = false;

    /**
     * 当前请求的参数 ，懒加载，只有需要用的时候才会去解析
     */
    private ParameterMap<String, String[]> parameters;

    /**
     * 添加一个cookie 加锁，保证并发安全
     * @param cookie Cookie
     */
    public void addCookie(Cookie cookie) {
        synchronized (this){
            cookies.add(cookie);
        }
    }

    protected void parseParameters() {
        // 如果参数已经解析过了，直接返回
        if (parsed){
            return;
        }
        ParameterMap<String, String[]> parameters = this.parameters;
        if (parameters == null) {
            parameters = new ParameterMap<>(16);
        }
        // 把锁关闭，使该映射类可以修改元素
        parameters.setLocked(false);
        // 解析请求参数
        HttpUtil.parseParameters(parameters, getQueryString());

        // TODO 解析POST方法用URL传递的参数

        // 把锁打开，保存资源不被修改
        parameters.setLocked(true);
        parsed = true;
        this.parameters = parameters;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }
    public void setInput(InputStream input) {
        this.input = input;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setRequestedSessionCookie(boolean requestedSessionCookie) {
        this.requestedSessionCookie = requestedSessionCookie;
    }

    public void setRequestedSessionId(String requestedSessionId) {
        this.requestedSessionId = requestedSessionId;
    }

    public void setRequestedSessionURL(boolean requestedSessionURL) {
        this.requestedSessionURL = requestedSessionURL;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public long getDateHeader(String name) {
        return 0;
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String name) {
        return 0;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return requestedSessionId;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getAttribute() {
        return attribute;
    }

    public void setAttribute(Map<String, String> attribute) {
        this.attribute = attribute;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return contentLength;
    }

    @Override
    public long getContentLengthLong() {
        return contentLength;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getParameter(String name) {
        // 先解析参数
        parseParameters();
        String[] values = this.parameters.get(name);
        if (values != null) {
            return values[0];
        } else {
            return null;
        }
    }

    @Override
    public Enumeration<String> getParameterNames() {
        parseParameters();
        return (new Enumerator(parameters));
    }

    @Override
    public String[] getParameterValues(String name) {
        parseParameters();
        return this.parameters.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return this.parameters;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String name, Object o) {

    }

    @Override
    public void removeAttribute(String name) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }
}