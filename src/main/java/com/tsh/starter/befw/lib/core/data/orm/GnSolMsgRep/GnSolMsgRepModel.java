package com.tsh.starter.befw.lib.core.data.orm.GnSolMsgRep;


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
import org.hibernate.envers.Audited;

@Entity
@Table(
        name = GlobalTableName.GN_SOL_MSG_REP,
        uniqueConstraints = {
                @UniqueConstraint(name = GnSolMsgRepModel.UK01, columnNames = {"serviceNm", "traceId"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Audited
public class GnSolMsgRepModel  extends BaseModel {

    public static final String UK01 = "uk_sol_msg_rep_01";

    @Column(name = "SERVICE_NM")
    private String serviceNm;

    @Column(name = "TRACE_ID")
    private String traceId;

    @Column(name = "RECV_EVNT_NM")
    private String recvEvntNm;

    @Column(name = "RECV_TOPIC_NM")
    private String recvTopicNm;

    @Column(name = "SELECTOR_KEY")
    private String selectorKey;


}
