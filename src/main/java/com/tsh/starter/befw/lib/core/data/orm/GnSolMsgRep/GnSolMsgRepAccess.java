package com.tsh.starter.befw.lib.core.data.orm.GnSolMsgRep;

import com.tsh.starter.befw.lib.core.data.orm.common.access.AbstractCrudService;
import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GnSolMsgRepAccess extends AbstractCrudService<GnSolMsgRepModel, String> {

    @Autowired
    GnSolMsgRepRepo repo;


    @Override
    protected BaseJpaRepository<GnSolMsgRepModel, String> getRepository() {
        return repo;
    }
}
