package edu.kit.scc.webreg.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity(name = "UserProvisionerEntity")
@Table(name = "user_provisioner")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class UserProvisionerEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

}
