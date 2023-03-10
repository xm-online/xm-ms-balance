package com.icthh.xm.ms.balance.service;

import com.icthh.xm.commons.exceptions.BusinessException;
import com.icthh.xm.commons.exceptions.ErrorConstants;

public class StatusTransitionException extends BusinessException {
    public StatusTransitionException(String newStatus, String currentStatus, String balanceTypeKey) {
        super(ErrorConstants.ERR_VALIDATION, message(newStatus, currentStatus, balanceTypeKey));
    }

    private static String message(String newStatus, String currentStatus, String balanceTypeKey) {
        return "Type key: " + balanceTypeKey + " can not go from [" + currentStatus + "] to ["
            + newStatus + "]";
    }
}
