package com.webank.wedpr.components.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedpr.components.user.entity.WedprGroup;
import com.webank.wedpr.components.user.mapper.WedprGroupMapper;
import com.webank.wedpr.components.user.service.WedprGroupService;
import org.springframework.stereotype.Service;

/**
 * 服务实现类
 *
 * @author caryliao
 * @since 2024-07-15
 */
@Service
public class WedprGroupImplService extends ServiceImpl<WedprGroupMapper, WedprGroup>
        implements WedprGroupService {}
