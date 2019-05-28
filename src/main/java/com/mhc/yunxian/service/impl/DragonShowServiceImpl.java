package com.mhc.yunxian.service.impl;

import com.mhc.yunxian.dao.DragonShowDao;
import com.mhc.yunxian.enums.VersionEnum;
import com.mhc.yunxian.service.DragonShowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DragonShowServiceImpl implements DragonShowService {

    @Autowired
    private DragonShowDao dragonShowDao;

    @Autowired
    private Environment environment;

    @Override
    public Integer selectDragonShow(Integer version) {
        return dragonShowDao.selectDragonShow(version);
    }

    @Override
    public void updateDragonShow(Integer show, Integer version) {
        /*String port = environment.getProperty("server.port");
        log.info("Environment in DragonShowService------getServerPort = ",port);

        if (port.equals("8889")){  //release版
            dragonShowDao.updateDragonShow(show, VersionEnum.RELEASE.getCode());
        }
        if (port.equals("8080")) {  //prodduct版
            dragonShowDao.updateDragonShow(show, VersionEnum.PRODUCT.getCode());
        }*/
        dragonShowDao.updateDragonShow(show, version);

    }

    @Override
    public Integer selectBtnShow(Integer version) {
        return dragonShowDao.selectBtnShow(version);
    }
}
