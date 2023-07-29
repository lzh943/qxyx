package com.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hello.model.user.Leader;
import com.hello.vo.user.LeaderAddressVo;

public interface LeaderService extends IService<Leader> {
    /**
     * 根据用户id获取团长信息
     * @param userId
     * @return
     */
    LeaderAddressVo getLeaderAddressVoByUserId(Long userId);
}
