package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.mapper.LeaderMapper;
import com.hello.mapper.UserDeliveryMapper;
import com.hello.model.user.Leader;
import com.hello.model.user.UserDelivery;
import com.hello.service.LeaderService;
import com.hello.utils.BeanCopyUtils;
import com.hello.vo.user.LeaderAddressVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LeaderServiceImpl extends ServiceImpl<LeaderMapper, Leader> implements LeaderService {
    @Resource
    UserDeliveryMapper userDeliveryMapper;

    /**
     * 根据用户id获取团长信息
     **/
    @Override
    public LeaderAddressVo getLeaderAddressVoByUserId(Long userId) {
        LambdaQueryWrapper<UserDelivery> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDelivery::getUserId, userId);
        queryWrapper.eq(UserDelivery::getIsDefault, 1);//默认值(默认地址)
        UserDelivery userDelivery = userDeliveryMapper.selectOne(queryWrapper);
        if(userDelivery == null) return null;
        Leader leader = getById(userDelivery.getLeaderId());
        LeaderAddressVo leaderAddressVo = BeanCopyUtils.copyBean(leader, LeaderAddressVo.class);
        leaderAddressVo.setUserId(userId);
        leaderAddressVo.setLeaderId(leader.getId());
        leaderAddressVo.setLeaderName(leader.getName());
        leaderAddressVo.setLeaderPhone(leader.getPhone());
        leaderAddressVo.setWareId(userDelivery.getWareId());
        leaderAddressVo.setStorePath(leader.getStorePath());
        return leaderAddressVo;
    }
}
