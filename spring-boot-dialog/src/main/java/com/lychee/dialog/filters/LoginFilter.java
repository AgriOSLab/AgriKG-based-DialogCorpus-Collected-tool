package com.lychee.dialog.filters;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 必须在启动类加入注解  @ServletComponentScan
 */
@WebFilter(urlPatterns = {"/collect-dialog-corpus"}, dispatcherTypes = DispatcherType.REQUEST)
public class LoginFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        final HttpSession session = req.getSession();

        final Object username = session.getAttribute("username");
        if (session.getAttribute("username")==null){
            String uri = "/login";
            servletRequest.getRequestDispatcher(uri).forward(servletRequest, servletResponse);
            return;
        }

        filterChain.doFilter(req, res);
        return;
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
