package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsRolePermission;

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
	name = GlobalTableName.GS_ROLE_PERMISSION,
	uniqueConstraints = {
		@UniqueConstraint(name = GsRolePermissionModel.UK01, columnNames = {"ROLE_ID", "PERMISSION_ID"})
	}
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Audited
public class GsRolePermissionModel extends BaseModel {

	public static final String UK01 = "uk_gs_role_permission_01";

	@Column(name = "ROLE_ID", length = 100, nullable = false)
	private String roleId;

	@Column(name = "PERMISSION_ID", length = 100, nullable = false)
	private String permissionId;

}
