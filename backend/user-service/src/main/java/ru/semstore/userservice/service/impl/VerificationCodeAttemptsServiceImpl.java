package ru.semstore.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.semstore.userservice.exception.ErrorCode;
import ru.semstore.userservice.exception.NotFoundException;
import ru.semstore.userservice.model.VerificationCode;
import ru.semstore.userservice.repository.VerificationCodeRepository;
import ru.semstore.userservice.service.VerificationCodeAttemptsService;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class VerificationCodeAttemptsServiceImpl implements VerificationCodeAttemptsService {
    private final VerificationCodeRepository codeRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementAttempts(UUID codeId) {
        VerificationCode code = codeRepository.findById(codeId)
                .orElseThrow(() -> new NotFoundException("Code not found", ErrorCode.CODE_NOT_FOUND));
        code.setAttempts(code.getAttempts() + 1);
    }
}
