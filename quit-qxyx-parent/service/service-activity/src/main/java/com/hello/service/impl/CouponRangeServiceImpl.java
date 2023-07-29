package com.hello.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hello.mapper.CouponRangeMapper;
import com.hello.model.activity.CouponRange;
import com.hello.service.CouponRangeService;
import org.springframework.stereotype.Service;

@Service
public class CouponRangeServiceImpl extends ServiceImpl<CouponRangeMapper, CouponRange>
        implements CouponRangeService {
}
