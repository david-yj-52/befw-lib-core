package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsGroup;

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
	name = GlobalTableName.GS_GROUP,
	uniqueConstraints = {
		@UniqueConstraint(name = GsGroupModel.UK01, columnNames = {"GROUP_NM"})
	}
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Audited
public class GsGroupModel extends BaseModel {

	public static final String UK01 = "uk_gs_group_01";

	@Column(name = "GROUP_NM", length = 100, nullable = false)
	private String groupNm;

	@Column(name = "DESCR", length = 255)
	private String descr;

}
