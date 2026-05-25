package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsUser;

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
	name = GlobalTableName.GS_USER,
	uniqueConstraints = {
		@UniqueConstraint(name = GsUserModel.UK01, columnNames = {"EMAIL"})
	}
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Audited
public class GsUserModel extends BaseModel {

	public static final String UK01 = "uk_gs_user_01";

	@Column(name = "EMAIL", length = 255, nullable = false)
	private String email;

	@Column(name = "USER_NM", length = 100, nullable = false)
	private String userNm;

	@Column(name = "AVATAR_URL", length = 500)
	private String avatarUrl;

	@Column(name = "PWD_HASH", length = 255, nullable = false)
	private String pwdHash;

}
