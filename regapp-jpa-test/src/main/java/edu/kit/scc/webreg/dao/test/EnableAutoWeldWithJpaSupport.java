package edu.kit.scc.webreg.dao.test;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.context.RequestScoped;

import org.jboss.weld.junit5.auto.ActivateScopes;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.extension.ExtendWith;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
@Inherited
@EnableAutoWeld
@AddPackages({ EntityManagerProducer.class })
@ActivateScopes({ RequestScoped.class })
@ExtendWith(JtaEnvironmentExtension.class)
public @interface EnableAutoWeldWithJpaSupport {

}
