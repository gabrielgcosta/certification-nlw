package com.gabrielcosta.certification_nlw.modules.students.dto.useCases;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gabrielcosta.certification_nlw.modules.students.dto.VerifyIfHasCertificationDTO;
import com.gabrielcosta.certification_nlw.modules.students.repositories.CertificationStudentRepository;

@Service
public class VerifyIfHasCertificationUseCase {

    @Autowired
    CertificationStudentRepository certificationStudentRepository;

    public boolean execute(VerifyIfHasCertificationDTO dto){
        var result = certificationStudentRepository.findByStudentEmailAndTechnology(dto.getEmail(), dto.getTechnology());

        if(!result.isEmpty()){
            return true;
        }
        return false;
    }
    
}
