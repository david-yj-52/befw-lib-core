package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsGroupMember;

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
	name = GlobalTableName.GS_GROUP_MEMBER,
	uniqueConstraints = {
		@UniqueConstraint(name = GsGroupMemberModel.UK01, columnNames = {"GROUP_ID", "USER_ID"})
	}
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Audited
public class GsGroupMemberModel extends BaseModel {

	public static final String UK01 = "uk_gs_group_member_01";

	@Column(name = "GROUP_ID", length = 100, nullable = false)
	private String groupId;

	@Column(name = "USER_ID", length = 100, nullable = false)
	private String userId;

}
