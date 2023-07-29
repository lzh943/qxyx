package com.hello.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.constant.RedisConst;
import com.hello.enums.UserType;
import com.hello.exception.SysException;
import com.hello.mapper.UserDeliveryMapper;
import com.hello.mapper.UserMapper;
import com.hello.model.user.Leader;
import com.hello.model.user.User;
import com.hello.model.user.UserDelivery;
import com.hello.result.Result;
import com.hello.result.ResultCodeEnum;
import com.hello.service.LeaderService;
import com.hello.service.UserService;
import com.hello.utils.ConstantPropertiesUtil;
import com.hello.utils.HttpClientUtils;
import com.hello.utils.JwtHelper;
import com.hello.vo.user.LeaderAddressVo;
import com.hello.vo.user.UserLoginVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private LeaderService leaderService;
    @Resource
    private UserDeliveryMapper userDeliveryMapper;
    @Resource
    private RedisTemplate redisTemplate;
    /**
     * 用户微信登录授权
     */
    @Override
    public Result loginWx(String code) {
        if (StringUtils.isEmpty(code)) {
            throw new SysException(ResultCodeEnum.ILLEGAL_CALLBACK_REQUEST_ERROR);
        }
        String appId = ConstantPropertiesUtil.WX_OPEN_APP_ID;
        String appSecret = ConstantPropertiesUtil.WX_OPEN_APP_SECRET;
        StringBuffer url=new StringBuffer()
                .append("https://api.weixin.qq.com/sns/jscode2session")
                .append("?appId=%s")
                .append("&secret=%s")
                .append("&js_code=%s")
                .append("&grant_type=authorization_code");
        String tokenUrl = String.format(url.toString(), appId, appSecret, code);
        String result=null;
        try {
            result = HttpClientUtils.get(tokenUrl);
        } catch (Exception e) {
            throw new SysException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        String sessionKey = jsonObject.getString("session_key");
        String openId = jsonObject.getString("openid");
        User user = getUserByOpenId(openId);
        if(user==null){
            user=new User();
            user.setOpenId(openId);
            user.setNickName(openId);
            user.setPhotoUrl("");
            user.setUserType(UserType.USER);
            user.setIsNew(0);
            save(user);
        }
        LeaderAddressVo leaderAddressVo = leaderService.getLeaderAddressVoByUserId(user.getId());
        String token = JwtHelper.createToken(user.getId(), user.getNickName());
        UserLoginVo userLoginVo=getUSerLoginVo(user.getId());
        redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + user.getId(),
                    userLoginVo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
        HashMap<String,Object> map=new HashMap<>();
        map.put("user", user);
        map.put("token", token);
        map.put("leaderAddressVo",leaderAddressVo);
        return Result.ok(map);
    }
    //获取用户有关的详细信息
    private UserLoginVo getUSerLoginVo(Long userId) {
        UserLoginVo userLoginVo = new UserLoginVo();
        User user = getById(userId);
        userLoginVo.setNickName(user.getNickName());
        userLoginVo.setUserId(userId);
        userLoginVo.setPhotoUrl(user.getPhotoUrl());
        userLoginVo.setOpenId(user.getOpenId());
        userLoginVo.setIsNew(user.getIsNew());
        LambdaQueryWrapper<UserDelivery> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDelivery::getUserId, userId);
        queryWrapper.eq(UserDelivery::getIsDefault, 1);
        UserDelivery userDelivery = userDeliveryMapper.selectOne(queryWrapper);
        if(null != userDelivery) {
            userLoginVo.setLeaderId(userDelivery.getLeaderId());
            userLoginVo.setWareId(userDelivery.getWareId());
        } else {
            userLoginVo.setLeaderId(1L);
            userLoginVo.setWareId(1L);
        }
        return userLoginVo;
    }

    //根据openId判断用户是否已经存在
    private User getUserByOpenId(String openId){
        LambdaQueryWrapper<User> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenId, openId);
        User user = getOne(wrapper);
        return user;
    }
}
