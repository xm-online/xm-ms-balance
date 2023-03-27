import com.icthh.xm.ms.balance.config.LepContext
import com.icthh.xm.ms.balance.domain.BalanceChangeEvent
import com.icthh.xm.ms.balance.service.BalanceHistoryService
import com.icthh.xm.ms.balance.service.dto.BalanceDTO
import groovy.util.logging.Slf4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

return new SpentAmount(lepContext as LepContext).execute()

@Slf4j
class SpentAmount {

    private LepContext lepContext
    private BalanceHistoryService balanceHistoryService

    SpentAmount(LepContext lepContext) {
        this.lepContext = lepContext
        this.balanceHistoryService = lepContext.services.balanceHistoryService
    }

    def execute() {
        BalanceDTO balanceDTO = lepContext.inArgs.balanceDTO
        log.debug("SpentAmount: execute: balanceDTO: {}", balanceDTO)

        Page<BalanceChangeEvent> changeEvents = balanceHistoryService.findBalanceChanges(
                "BALANCE-CHARGING",
                [
                        balanceId: balanceDTO.getId()
                ],
                Pageable.unpaged())

        return changeEvents.getContent().amountDelta.sum { it ?: 0 }
    }
}
