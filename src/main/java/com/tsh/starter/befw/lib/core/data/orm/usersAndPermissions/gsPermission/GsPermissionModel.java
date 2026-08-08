package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsPermission;

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
	name = GlobalTableName.GS_PERMISSION,
	uniqueConstraints = {
		@UniqueConstraint(name = GsPermissionModel.UK01, columnNames = {"PERM_NM"})
	}
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Audited
public class GsPermissionModel extends BaseModel {

	public static final String UK01 = "uk_gs_permission_01";

	@Column(name = "PERM_NM", length = 100, nullable = false)
	private String permNm;

	@Column(name = "DESCR", length = 255)
	private String descr;

	@Column(name = "RESOURCE", length = 50, nullable = false)
	private String resource;

	@Column(name = "ACTION", length = 50, nullable = false)
	private String action;

}
