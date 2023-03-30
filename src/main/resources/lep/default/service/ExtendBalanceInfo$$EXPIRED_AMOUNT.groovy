import com.icthh.xm.ms.balance.config.LepContext
import com.icthh.xm.ms.balance.service.PocketQueryService
import com.icthh.xm.ms.balance.service.dto.BalanceDTO
import com.icthh.xm.ms.balance.service.dto.PocketCriteria
import com.icthh.xm.ms.balance.service.dto.PocketDTO
import groovy.util.logging.Slf4j
import io.github.jhipster.service.filter.InstantFilter
import io.github.jhipster.service.filter.LongFilter

import java.time.Clock

import static java.time.Instant.now

return new ExpiredAmount(lepContext as LepContext).execute()

@Slf4j
class ExpiredAmount {

    private Clock clock = Clock.systemDefaultZone()
    private LepContext lepContext
    private PocketQueryService pocketQueryService

    ExpiredAmount(LepContext lepContext) {
        this.lepContext = lepContext
        this.pocketQueryService = lepContext.services.pocketQueryService
    }

    def execute() {
        BalanceDTO balanceDTO = lepContext.inArgs.balanceDTO
        log.debug("execute: balanceDTO: {}", balanceDTO)

        LongFilter balanceIdFilter = new LongFilter()
        balanceIdFilter.setEquals(balanceDTO.getId())

        InstantFilter dateTimeFilter = new InstantFilter()
        dateTimeFilter.setLessThan(now(clock))

        PocketCriteria criteria = new PocketCriteria()
        criteria.setBalanceId(balanceIdFilter)
        criteria.setEndDateTime(dateTimeFilter)

        List<PocketDTO> pockets = pocketQueryService.findByCriteria(criteria, null)
        return pockets.amount.sum { it ?: 0 }
    }
}
