package com.icthh.xm.ms.balance.web.rest;

import com.icthh.xm.commons.config.client.repository.TenantListRepository;
import com.icthh.xm.commons.gen.api.TenantsApiDelegate;
import com.icthh.xm.commons.gen.model.Tenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantResource implements TenantsApiDelegate {

    private final TenantListRepository tenantListRepository;

    @Override
    @PreAuthorize("hasPermission({'tenant':#tenant}, 'BALANCE.TENANT.CREATE')")
    public ResponseEntity<Void> addTenant(Tenant tenant) {
        tenantListRepository.addTenant(tenant.getTenantKey().toLowerCase());
        return ResponseEntity.ok().build();
    }

    @Override
    @PreAuthorize("hasPermission({'tenantKey':#tenantKey}, 'BALANCE.TENANT.DELETE')")
    public ResponseEntity<Void> deleteTenant(String tenantKey) {
        tenantListRepository.deleteTenant(tenantKey.toLowerCase());
        return ResponseEntity.ok().build();
    }

    @Override
    @PostAuthorize("hasPermission(null, 'BALANCE.TENANT.GET_LIST')")
    public ResponseEntity<List<Tenant>> getAllTenantInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    @PostAuthorize("hasPermission({'returnObject': returnObject.body}, 'BALANCE.TENANT.GET_LIST.ITEM')")
    public ResponseEntity<Tenant> getTenant(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    @PreAuthorize("hasPermission({'tenant':#tenant, 'state':#state}, 'BALANCE.TENANT.UPDATE')")
    public ResponseEntity<Void> manageTenant(String tenant, String state) {
        tenantListRepository.updateTenant(tenant.toLowerCase(), state.toUpperCase());
        return ResponseEntity.ok().build();
    }
}
