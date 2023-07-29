package com.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.mapper.ActivityRuleMapper;
import com.hello.model.activity.ActivityRule;
import com.hello.model.activity.ActivitySku;
import com.hello.result.Result;
import com.hello.service.ActivityRuleService;
import com.hello.service.ActivitySkuService;
import com.hello.vo.activity.ActivityRuleVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ActivityRuleServiceImpl extends ServiceImpl<ActivityRuleMapper, ActivityRule>
        implements ActivityRuleService {

}
