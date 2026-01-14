package com.example.demo.domain.role;

import com.example.demo.core.generic.AbstractServiceImpl;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Getter@Setter
@Service
public class RoleServiceImpl extends AbstractServiceImpl<Role> implements RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        super(roleRepository);
        this.roleRepository = roleRepository;
    }
}
