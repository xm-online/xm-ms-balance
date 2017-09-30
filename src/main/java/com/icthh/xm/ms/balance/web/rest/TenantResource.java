package com.icthh.xm.ms.balance.web.rest;

import com.icthh.xm.commons.config.client.repository.TenantListRepository;
import com.icthh.xm.commons.gen.api.TenantsApiDelegate;
import com.icthh.xm.commons.gen.model.Tenant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantResource implements TenantsApiDelegate {

    private final TenantListRepository tenantListRepository;

    @Override
    public ResponseEntity<Void> addTenant(Tenant tenant) {
        tenantListRepository.addTenant(tenant.getTenantKey().toLowerCase());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteTenant(String tenantKey) {
        tenantListRepository.deleteTenant(tenantKey.toLowerCase());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<Tenant>> getAllTenantInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<Tenant> getTenant(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<Void> manageTenant(String tenant, String state) {
        tenantListRepository.updateTenant(tenant.toLowerCase(), state.toUpperCase());
        return ResponseEntity.ok().build();
    }
}
