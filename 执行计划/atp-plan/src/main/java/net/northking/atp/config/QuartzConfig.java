package net.northking.atp.config;

import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfig {

    @Autowired
    private TaskSchedulerFactory taskSchedulerFactory;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        System.out.println("schedulerFactoryBean被创建");
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setStartupDelay(5);
        schedulerFactoryBean.setJobFactory(taskSchedulerFactory);
        return schedulerFactoryBean;
    }

    @Bean
    public Scheduler scheduler() {
        System.out.println("scheduler被创建");
        return schedulerFactoryBean().getScheduler();
    }

    /*@Bean
    public TaskSchedulerFactory taskSchedulerFactory() {
        System.out.println("taskSchedulerFactoryb被创建");
        return new TaskSchedulerFactory();
    }*/
}
