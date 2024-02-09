package com.gabrielcosta.certification_nlw.modules.students.useCases;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gabrielcosta.certification_nlw.modules.questions.entities.QuestionEntity;
import com.gabrielcosta.certification_nlw.modules.questions.repositories.QuestionRepository;
import com.gabrielcosta.certification_nlw.modules.students.dto.StudentCertificationAnswerDTO;
import com.gabrielcosta.certification_nlw.modules.students.dto.VerifyIfHasCertificationDTO;
import com.gabrielcosta.certification_nlw.modules.students.entities.AnswersCertificationEntity;
import com.gabrielcosta.certification_nlw.modules.students.entities.CertificationStudentEntity;
import com.gabrielcosta.certification_nlw.modules.students.entities.StudentEntity;
import com.gabrielcosta.certification_nlw.modules.students.repositories.CertificationStudentRepository;
import com.gabrielcosta.certification_nlw.modules.students.repositories.StudentRepository;

@Service
public class StudentCertificationAnswerUseCase {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CertificationStudentRepository certificationStudentRepository;

    @Autowired
    private VerifyIfHasCertificationUseCase verifyIfHasCertificationUseCase;

    public CertificationStudentEntity execute(StudentCertificationAnswerDTO dto) throws Exception{

        // Find student
        var hasCertification = this.verifyIfHasCertificationUseCase
                .execute(new VerifyIfHasCertificationDTO(dto.getEmail(), dto.getTechnology()));

        if (hasCertification) {
            throw new Exception("User has alredy taken certification");
        }

        List<QuestionEntity> questionsEntity = questionRepository.findByTechnology(dto.getTechnology());
        List<AnswersCertificationEntity> answersCertifications = new ArrayList<>();
        
        AtomicInteger correctAnswers = new AtomicInteger(0);

        dto.getQuestionAnswer()
                .stream().forEach(questionAnswer -> {
                    var question = questionsEntity.stream()
                            .filter(q -> q.getId().equals(questionAnswer.getQuestionId())).findFirst().get();

                    var findCorrectAlternative = question.getAlternatives().stream()
                            .filter(alternative -> alternative.isCorrect()).findFirst().get();

                    if (findCorrectAlternative.getId().equals(questionAnswer.getAlternativeId())) {
                        questionAnswer.setCorrect(true);
                        correctAnswers.incrementAndGet();
                    } else {
                        questionAnswer.setCorrect(false);
                    }

                    var answerrsCertificationsEntity = AnswersCertificationEntity.builder()
                            .answerId(questionAnswer.getAlternativeId())
                            .questionId(questionAnswer.getQuestionId())
                            .isCorrect(questionAnswer.isCorrect()).build();

                    answersCertifications.add(answerrsCertificationsEntity);
                });

        // Verificar se existe student pelo email
        var student = studentRepository.findByEmail(dto.getEmail());
        UUID studentID;
        if (student.isEmpty()) {
            var studentCreated = StudentEntity.builder().email(dto.getEmail()).build();
            studentCreated = studentRepository.save(studentCreated);
            studentID = studentCreated.getId();
        } else {
            studentID = student.get().getId();
        }

        CertificationStudentEntity certificationStudentEntity = CertificationStudentEntity.builder()
                .technology(dto.getTechnology())
                .studentID(studentID)
                .grade(correctAnswers.get())
                .build();

        var certificationStudentCreated = certificationStudentRepository.save(certificationStudentEntity);

        answersCertifications.stream().forEach(answerCertification -> {
            answerCertification.setCertificationId(certificationStudentEntity.getId());
            answerCertification.setCertificationStudentEntity(certificationStudentEntity);
        });

        certificationStudentEntity.setAnswersCertificationEntity(answersCertifications);

        certificationStudentRepository.save(certificationStudentEntity);

        return certificationStudentCreated;
        // Salvar as informações da certificação
        
    }
    
}
