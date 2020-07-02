package net.northking.atp.config.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.northking.atp.db.persistent.ReServiceConfig;
import net.northking.atp.db.service.ReServiceConfigService;
import net.northking.db.OrderBy;
import net.northking.db.Pagination;
import net.northking.db.mybatis.SqlOrderBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.util.StringUtils;

public class DatabasesEnvironmentRepository
  implements EnvironmentRepository
{
  private static final Logger logger = LoggerFactory.getLogger(DatabasesEnvironmentRepository.class);
  private static final String GLOBAL_APP = "GLOBAL";
  private static final int PAGE_SIZE = 100;
  @Autowired
  private ReServiceConfigService service;
  
  public Environment findOne(String application, String profile, String label)
  {
    Environment environment = null;
    if ((StringUtils.isEmpty(application)) || (StringUtils.isEmpty(profile)))
    {
      logger.warn("应用程序名称为Null，未获取任何环境参数！");
      return null;
    }
    String sourceName = application + "-" + profile + "-" + label + ".properties";
    logger.debug("开始获取环境参数，应用程序名称={}，环境名称={}，版本={}", new Object[] { application, profile, label });
    Map<String, String> properties = findProperties(application, profile, label);
    if (properties.size() > 0)
    {
      environment = new Environment(application, new String[] { profile }, label, "", "");
      environment.add(new PropertySource(sourceName, properties));
    }
    logger.info("获取环境参数完成，应用程序名称={}，环境名称={}，版本={}", new Object[] { application, profile, label });
    return environment;
  }
  
  private Map<String, String> findProperties(String application, String profile, String label)
  {
    Map<String, String> propMap = new HashMap();
    
    List<ReServiceConfig> globalConfigs = findGlobalConfigs(profile, label);
    for (Iterator localIterator = globalConfigs.iterator(); localIterator.hasNext();)
    {
      ReServiceConfig config = (ReServiceConfig)localIterator.next();
      
      propMap.put(config.getPropKey(), config.getPropValue());
      logger.debug("获取全局参数：{}={}", config.getPropKey(), config.getPropValue());
    }
   
    List<ReServiceConfig> applicationConfigs = findApplicationConfigs(application, profile, label);
    for (ReServiceConfig config : applicationConfigs)
    {
      propMap.put(config.getPropKey(), config.getPropValue());
      logger.debug("获取应用程序参数：{}={}", config.getPropKey(), config.getPropValue());
    }
    return propMap;
  }
  
  private List<ReServiceConfig> findGlobalConfigs(String profile, String label)
  {
    return findApplicationConfigs("GLOBAL", profile, label);
  }
  
  private List<ReServiceConfig> findApplicationConfigs(String application, String profile, String label)
  {
    int pageNo = 1;
    int pageCount = 1;
    List<ReServiceConfig> cloudConfigs = new ArrayList();
    
    ReServiceConfig example = new ReServiceConfig();
    example.setAppName(application);
    example.setProfile(profile);
    example.setAppLabel(label);
    
    OrderBy orderBy = new SqlOrderBy();
    orderBy.addOrderBy("catalog", "ASC");
    while (pageNo <= pageCount)
    {
      Pagination<ReServiceConfig> result = this.service.query(example, orderBy, pageNo++, 100);
      pageCount = result.getPageCount();
      cloudConfigs.addAll(result.getRecords());
    }
    return cloudConfigs;
  }
}
