package com.example.demo.Service;

import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;

/**
 * @author Witchcraft
 */

@Transactional
@Service

public class UserService {
    @Autowired
    private  UserRepository userRepository ;

    //用户名是否存在
    public boolean exists(User user){
        return userRepository.existsByUsername(user.getUsername());
    }

    //登录用
    public User findByNameAndPassword(User user)
    {
        //System.out.println(user.getUsername() +  user.getPassword());
        return userRepository.findByUsernameAndPassword(user.getUsername(), user.getPassword());
    }

    //注册后插入
    public boolean insert(User user)
    {
        userRepository.save(user);
        return true;
    }

    //更新用户信息
    public boolean update(User user)
    {
        if(userRepository.findById(user.getId()).isEmpty()){
            return false;
        }
        userRepository.save(user);
        return true;
    }
}