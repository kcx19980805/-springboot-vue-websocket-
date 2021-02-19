package com.kcx.controller;

import com.kcx.mapper.HouseMapper;
import com.kcx.mapper.OrderMapper;
import com.kcx.pojo.House;
import com.kcx.pojo.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    HouseMapper houseMapper;
    /*得到用户或房东的订单*/
    @GetMapping("/getOrderList/{username}/{customerId}")
    public List<Order> getOrderList(@PathVariable("username") String username,@PathVariable("customerId") String customerId){
        if(username.equals("null")) username=null;
        if(customerId.equals("null")) customerId=null;
        return orderMapper.queryOrderList(username,customerId);
    }

    /*查询某个房东的某个房屋正在进行的所有订单*/
    @GetMapping("/getOrderByTitle/{username}/{title}")
    public List<Order> getOrderByTitle(@PathVariable("username") String username,@PathVariable("title") String title){
        return  orderMapper.queryOrderByTitle(username,title);
    }

    /*添加订单*/
    @PostMapping("/addOrder")
    @Transactional(readOnly = false,timeout = -1,propagation = Propagation.REQUIRED,isolation = Isolation.REPEATABLE_READ)  //事务的注解
    public String addOrder(@RequestBody Order order){
        //自己不能订自己的房子
        if(order.getUsername().equals(order.getCustomerId())){
            return "error";
        }
        //如果该时段该房屋没有订单则生成订单
        if(orderMapper.queryOrder(order)==null&&orderMapper.addOrder(order)==1){
            //如果存在房屋并且状态是发布中
            House house =houseMapper.queryHouse(order.getTitle(),order.getUsername()).get(0);
            if(house!=null&&house.getState()==1){
                return "success";
            }
            else{
                //强制手动事务回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "error";
            }
        }
        return "error";
    }
    /*修改订单*/
    @PutMapping("/updateOrder")
    public String updateOrder(@RequestBody Order order){
        if(orderMapper.updateOrder(order)==1)
            return "success";
        return "error";
    }
    /*取消订单*/
    @PostMapping("/cancelOrder")
    @Transactional  //事务的注解
    public String cancelOrder(@RequestBody Order order){
        House house=houseMapper.queryHouse(order.getTitle(),order.getUsername()).get(0);
        if(house==null){
            return "error";
        }
        order.setState(3);
        if(orderMapper.updateOrder(order)==1){
            return "success";
        }
        else{
            //强制手动事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "error";
        }
    }
    /*删除订单*/
    @DeleteMapping("/deleteOrder/{id}")
    public void deleteOrder(@PathVariable("id")Integer id){
        orderMapper.deleteOrder(id);
    }
}
