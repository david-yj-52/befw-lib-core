package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsUserRole;

import org.hibernate.envers.Audited;

import com.tsh.starter.befw.lib.core.constant.GlobalTableName;
import com.tsh.starter.befw.lib.core.data.orm.common.model.BaseModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
	name = GlobalTableName.GS_USER_ROLE,
	uniqueConstraints = {
		@UniqueConstraint(name = GsUserRoleModel.UK01, columnNames = {"USER_ID", "ROLE_ID"})
	}
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Audited
public class GsUserRoleModel extends BaseModel {

	public static final String UK01 = "uk_gs_user_role_01";

	@Column(name = "USER_ID", length = 100, nullable = false)
	private String userId;

	@Column(name = "ROLE_ID", length = 100, nullable = false)
	private String roleId;

}
