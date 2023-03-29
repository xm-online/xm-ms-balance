import com.icthh.xm.commons.exceptions.BusinessException
import com.icthh.xm.ms.balance.config.LepContext
import com.icthh.xm.ms.balance.service.PocketQueryService
import com.icthh.xm.ms.balance.service.dto.BalanceDTO
import com.icthh.xm.ms.balance.service.dto.PocketCriteria
import com.icthh.xm.ms.balance.service.dto.PocketDTO
import groovy.util.logging.Slf4j
import io.github.jhipster.service.filter.InstantFilter
import io.github.jhipster.service.filter.LongFilter

import java.time.Clock
import java.time.Instant

import static java.time.Instant.now

return new ExpireByDateTime(lepContext as LepContext).execute()

@Slf4j
class ExpireByDateTime {

    private LepContext lepContext
    private PocketQueryService pocketQueryService
    private Clock clock = Clock.systemDefaultZone()

    ExpireByDateTime(LepContext lepContext) {
        this.lepContext = lepContext
        this.pocketQueryService = lepContext.services.pocketQueryService
    }

    def execute() {
        BalanceDTO balanceDTO = lepContext.inArgs.balanceDTO
        Map<String, String> params = lepContext.inArgs.params
        log.debug("execute: balanceDTO: {}, params: {}", balanceDTO, params)

        Instant expireByDateTime
        try {
            expireByDateTime = Instant.parse(params?.get("expireByDateTime"))
            log.debug("ExpiredByDate: execute: expireByDateTime: {}", expireByDateTime)
        } catch (Exception e) {
            log.error("expireByDateTime parsing failure, {}", e)
            throw new BusinessException("error.balance.params.parsing.failure",
                    "Could not parse expireByDateTime parameter")
        }

        LongFilter balanceIdFilter = new LongFilter()
        balanceIdFilter.setEquals(balanceDTO.getId())

        InstantFilter dateTimeFilter = new InstantFilter()
        dateTimeFilter.setLessThan(expireByDateTime)
        dateTimeFilter.setGreaterThan(now(clock))

        PocketCriteria criteria = new PocketCriteria()
        criteria.setBalanceId(balanceIdFilter)
        criteria.setEndDateTime(dateTimeFilter)

        List<PocketDTO> pockets = pocketQueryService.findByCriteria(criteria, null)
        return pockets.amount.sum { it ?: 0 }
    }
}
