package com.hello.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.mapper.CouponUseMapper;
import com.hello.model.activity.CouponUse;
import com.hello.service.CouponUseService;
import org.springframework.stereotype.Service;

@Service
public class CouponUseServiceImpl extends ServiceImpl<CouponUseMapper, CouponUse>
        implements CouponUseService {
}
