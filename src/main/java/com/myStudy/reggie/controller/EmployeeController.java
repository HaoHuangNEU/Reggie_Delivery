package com.myStudy.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myStudy.reggie.common.R;
import com.myStudy.reggie.entity.Employee;
import com.myStudy.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){


        //1.将页面提交的密码进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2.根据页面提交的用户名查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3.如果没有查询到则返回登录失败结果
        if (emp == null){
            return R.error("Login failed");
        }

        //4.密码比对，如果不一样返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("Login failed");
        }

        //5.查看员工状态, 如果是已禁用的状态，则返回员工已禁用结果
        if(emp.getStatus() == 0){
            return R.error("account suspended");
        }

        //6.登录成功，将员工id存入Session并返回登陆成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("logout success");
    }
    @PostMapping
        public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("new employee is {}", employee.toString());
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());

//        Long empId = (Long)request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        employeeService.save(employee);
        return R.success("create employee successfully");
    }

    /**
     * @param page
     * @param pageSize
     * @param name
     * @return
     *
     * */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        Page pageInfo = new Page(page,pageSize);

        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName,name);

        queryWrapper.orderByDesc(Employee::getUpdateTime);

        employeeService.page(pageInfo,queryWrapper);

         return R.success(pageInfo);

    }
    /***
    *
    */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        //log.info(employee.toString());
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empId);
        employeeService.updateById(employee);
        return R.success("update employee information successfully");
    }
    @GetMapping("/{id}")
    public  R<Employee> getById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        return R.success(employee);
    }


}
