import com.icthh.xm.commons.exceptions.BusinessException
import com.icthh.xm.ms.balance.config.LepContext
import com.icthh.xm.ms.balance.domain.BalanceChangeEvent
import com.icthh.xm.ms.balance.domain.BalanceChangeEvent_
import com.icthh.xm.ms.balance.service.OperationType
import com.icthh.xm.ms.balance.service.mapper.FilterMapper
import groovy.util.logging.Slf4j
import io.github.jhipster.service.QueryService
import io.github.jhipster.service.filter.LongFilter
import io.github.jhipster.service.filter.StringFilter
import org.springframework.data.jpa.domain.Specification

return new BalanceCharging(lepContext as LepContext).execute()

@Slf4j
class BalanceCharging extends QueryService {

    private LepContext lepContext

    BalanceCharging(LepContext lepContext) {
        this.lepContext = lepContext
    }

    def execute() {
        Long balanceId = lepContext.inArgs.params?.balanceId
        log.debug("execute: balanceId: {}", balanceId)

        FilterMapper filterMapper = new FilterMapper()

        Specification<BalanceChangeEvent> idSpecification = Optional.ofNullable(balanceId)
                .map { id ->
                    LongFilter longFilter = new LongFilter()
                    longFilter.setEquals(id)
                    return longFilter
                }
                .map { idFilter -> buildRangeSpecification(idFilter, BalanceChangeEvent_.balanceId) }
                .orElseThrow{ throw new BusinessException("Could not define balance id specification")}

        Specification<BalanceChangeEvent> operationTypeSpec = Optional.of("CHARGING")
                .map { it ->
                    StringFilter operationTypeFilter = new StringFilter()
                    operationTypeFilter.setEquals(it)
                    return operationTypeFilter
                }
                .map { operationTypeFilter ->
                    filterMapper.toEnumFilter(operationTypeFilter, OperationType.class)
                }
                .map { operationTypeFilter ->
                    buildSpecification(operationTypeFilter, BalanceChangeEvent_.operationType) }
                .orElseThrow{throw new BusinessException("Could not define balance operation type specification")}

        return idSpecification & operationTypeSpec
    }
}
