package net.northking.atp.db.impl;

import net.northking.atp.db.adapter.ReServiceConfigSrvAdapter;
import net.northking.atp.db.dao.ReServiceConfigDao;
import net.northking.atp.db.persistent.ReServiceConfig;
import net.northking.atp.db.service.ReServiceConfigService;
import net.northking.db.BasicDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReServiceConfigServiceImpl
  extends ReServiceConfigSrvAdapter
  implements ReServiceConfigService
{
  private static final Logger logger = LoggerFactory.getLogger(ReServiceConfigServiceImpl.class);
  @Autowired
  private ReServiceConfigDao reServiceConfigDao;
  
  protected BasicDao<ReServiceConfig> getDao()
  {
    return this.reServiceConfigDao;
  }
}
