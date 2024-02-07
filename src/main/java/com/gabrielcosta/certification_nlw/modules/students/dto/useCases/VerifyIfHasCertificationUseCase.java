package com.gabrielcosta.certification_nlw.modules.students.dto.useCases;

import org.springframework.stereotype.Service;

import com.gabrielcosta.certification_nlw.modules.students.dto.VerifyIfHasCertificationDTO;

@Service
public class VerifyIfHasCertificationUseCase {

    public boolean execute(VerifyIfHasCertificationDTO dto){
        return true;
    }
    
}
