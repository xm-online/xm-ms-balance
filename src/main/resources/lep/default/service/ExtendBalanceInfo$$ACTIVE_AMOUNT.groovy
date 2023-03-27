import com.icthh.xm.ms.balance.config.LepContext
import com.icthh.xm.ms.balance.service.dto.BalanceDTO
import groovy.util.logging.Slf4j

return new ActiveAmount(lepContext as LepContext).execute()

@Slf4j
class ActiveAmount {

    private LepContext lepContext

    ActiveAmount(LepContext lepContext) {
        this.lepContext = lepContext
    }

    def execute() {
        BalanceDTO balanceDTO = lepContext.inArgs.balanceDTO
        log.debug("ActiveAmount: execute: balanceDTO: {}", balanceDTO)

        return balanceDTO.getAmount()
    }
}
