package com.myStudy.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.myStudy.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.annotation.WebFilter;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * check whether user complete login
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1. get URI for this request

        String requestURI = request.getRequestURI();

        log.info("request is {}", requestURI);

        //define request that doesn't need to handle
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"
        };

        //2.check if request need to be handled
        boolean check = check(urls, requestURI);

        //3. if doesn't need to handle, pass
        if(check == true){
            filterChain.doFilter(request, response);
            log.info("no need to handle {}", requestURI);
            return ;
        }

        //4. check login status, if logged in, pass
        if(request.getSession().getAttribute("employee") != null){
            log.info("user has logged in, id is {}",request.getSession().getAttribute("employee"));
            filterChain.doFilter(request,response);
            return;
        }

        //if not logged in, return
        log.info("user hasn't login");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return ;


    }
    public boolean check(String[] urls, String requestURI){
        for (String url : urls) {
            boolean match =PATH_MATCHER.match(url,requestURI);
            if(match)
                return true;
        }
        return false;
    }
}
