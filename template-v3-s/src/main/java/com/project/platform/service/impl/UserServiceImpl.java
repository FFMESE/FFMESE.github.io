package com.project.platform.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.project.platform.dto.CurrentUserDTO;
import com.project.platform.dto.RetrievePasswordDTO;
import com.project.platform.dto.UpdatePasswordDTO;
import com.project.platform.entity.User;
import com.project.platform.exception.CustomException;
import com.project.platform.mapper.UserMapper;
import com.project.platform.service.UserService;
import com.project.platform.utils.CurrentUserThreadLocal;
import com.project.platform.vo.PageVO;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Value("${resetPassword}")
    private String resetPassword;

    /**
     * 分页模糊查询
     *
     * @param query
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageVO<User> page(Map<String, Object> query, Integer pageNum, Integer pageSize) {
        PageVO<User> page = new PageVO();
        List<User> list = userMapper.queryPage((pageNum - 1) * pageSize, pageSize, query);
        page.setList(list);
        page.setTotal(userMapper.queryCount(query));
        return page;
    }

    /**
     * 列表返回
     *
     * @return
     */
    @Override
    public List<User> list() {
        return userMapper.list();
    }

    /**
     * 登录
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public CurrentUserDTO login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new CustomException("用户名或密码错误");
        }
        if ("禁用".equals(user.getStatus())) {
            throw new CustomException("用户已禁用");
        }
        CurrentUserDTO currentUserDTO = new CurrentUserDTO();
        BeanUtils.copyProperties(user, currentUserDTO);

        return currentUserDTO;
    }

    /**
     * 注册
     *
     * @param data
     */
    @Override
    public void register(JSONObject data) {
        User user = new User();
        user.setUsername(data.getString("username"));
        user.setPassword(data.getString("password"));
        user.setNickname(data.getString("nickname"));
        user.setAvatarUrl(data.getString("avatarUrl"));
        user.setCreateTime(LocalDateTime.now());
        user.setStatus("启用");
        insert(user);

    }

    /**
     * 更新当前用户信息
     *
     * @param currentUserDTO
     */
    @Override
    public void updateCurrentUserInfo(CurrentUserDTO currentUserDTO) {
        User user = userMapper.selectById(currentUserDTO.getId());
        user.setId(currentUserDTO.getId());
        user.setNickname(currentUserDTO.getNickname());
        user.setAvatarUrl(currentUserDTO.getAvatarUrl());
        user.setTel(currentUserDTO.getTel());
        user.setEmail(currentUserDTO.getEmail());
        userMapper.updateById(user);
    }

    /**
     * 修改当前用户密码
     *
     * @param updatePassword
     */
    @Override
    public void updateCurrentUserPassword(UpdatePasswordDTO updatePassword) {
        User user = userMapper.selectById(CurrentUserThreadLocal.getCurrentUser().getId());
        if (!user.getPassword().equals(updatePassword.getNewPassword())) {
            throw new CustomException("旧密码错误");
        }
        user.setPassword(updatePassword.getNewPassword());
        userMapper.updateById(user);
    }

    /**
     * 重置密码
     *
     * @param id
     */
    @Override
    public void resetPassword(Integer id) {
        User user = userMapper.selectById(id);
        user.setPassword(resetPassword);
        userMapper.updateById(user);
    }

    /**
     * 忘记密码
     *
     * @param retrievePasswordDTO
     */
    @Override
    public void retrievePassword(RetrievePasswordDTO retrievePasswordDTO) {
        User user = userMapper.selectByTel(retrievePasswordDTO.getTel());
        if (user == null) {
            throw new CustomException("手机号不存在");
        }
        user.setPassword(retrievePasswordDTO.getPassword());
        userMapper.updateById(user);
    }

    /**
     * 查询当前用户信息
     *
     * @param id
     * @return
     */
    @Override
    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    /**
     * 通过用户名查询
     *
     * @param username
     * @return
     */
    @Override
    public User selectByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    /**
     * 新增
     *
     * @param entity
     */
    @Override
    public void insert(User entity) {
        check(entity);
        entity.setCreateTime(LocalDateTime.now());
        if (entity.getPassword() == null) {
            entity.setPassword(resetPassword);
        }
        userMapper.insert(entity);
    }

    /**
     * 编辑
     *
     * @param entity
     */
    @Override
    public void updateById(User entity) {
        check(entity);
        userMapper.updateById(entity);
    }

    /**
     * 删除
     *
     * @param ids
     */
    @Override
    public void removeByIds(List<Integer> ids) {
        userMapper.removeByIds(ids);
    }


    private void check(User entity) {
        User user = userMapper.selectByUsername(entity.getUsername());
        if (user != null && user.getId() != entity.getId()) {
            throw new CustomException("用户名已存在");
        }
    }


}
