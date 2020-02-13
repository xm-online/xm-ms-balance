package com.icthh.xm.ms.balance.web.rest;

import com.icthh.xm.commons.gen.api.TenantsApiDelegate;
import com.icthh.xm.commons.gen.model.Tenant;
import com.icthh.xm.commons.permission.annotation.PrivilegeDescription;
import com.icthh.xm.commons.tenantendpoint.TenantManager;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantResource implements TenantsApiDelegate {

    private final TenantManager tenantManager;

    @Override
    @PreAuthorize("hasPermission({'tenant':#tenant}, 'BALANCE.TENANT.CREATE')")
    @PrivilegeDescription("Privilege to add a new balance tenant")
    public ResponseEntity<Void> addTenant(Tenant tenant) {
        tenantManager.createTenant(tenant);
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasPermission({'tenantKey':#tenantKey}, 'BALANCE.TENANT.DELETE')")
    @PrivilegeDescription("Privilege to delete balance tenant by tenantKey")
    public ResponseEntity<Void> deleteTenant(String tenantKey) {
        tenantManager.deleteTenant(tenantKey);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostAuthorize("hasPermission(null, 'BALANCE.TENANT.GET_LIST')")
    @PrivilegeDescription("Privilege to get all balance tenants")
    public ResponseEntity<List<Tenant>> getAllTenantInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    @PostAuthorize("hasPermission({'returnObject': returnObject.body}, 'BALANCE.TENANT.GET_LIST.ITEM')")
    @PrivilegeDescription("Privilege to get balance tenant")
    public ResponseEntity<Tenant> getTenant(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    @PreAuthorize("hasPermission({'tenant':#tenant, 'state':#state}, 'BALANCE.TENANT.UPDATE')")
    @PrivilegeDescription("Privilege to update balance tenant")
    public ResponseEntity<Void> manageTenant(String tenant, String state) {
        tenantManager.manageTenant(tenant, state);
        return ResponseEntity.ok().build();
    }
}
