package com.example.library.service;

import com.example.library.dto.MemberDTO;
import com.example.library.dto.MemberProfileDTO;
import com.example.library.dto.RegisterMemberRequest;
import com.example.library.entity.*;
import com.example.library.exception.MemberNotFoundException;
import com.example.library.repository.FineRepository;
import com.example.library.repository.HoldRepository;
import com.example.library.repository.MemberRepository;
import com.example.library.repository.NotificationRepository;
import com.example.library.util.MembershipNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MembershipNumberGenerator membershipNumberGenerator;

    @Autowired
    private FineRepository fineRepository;

    @Autowired
    private HoldRepository holdRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public Member register(RegisterMemberRequest req) {
        Member member = new Member();
        member.setMembershipNumber(membershipNumberGenerator.generate());
        member.setMembershipTier(MembershipTier.STANDARD);
        member.setJoinDate(LocalDate.now());
        member.setExpiryDate(LocalDate.now().plusYears(1));
        member.setActive(true);
        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public MemberDTO findById_DTO(Long id) {
        Member member = findById(id);
        return toDTO(member);
    }

    @Transactional(readOnly = true)
    public Member findByMembershipNumber(String number) {
        return memberRepository.findByMembershipNumber(number)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with membership number: " + number));
    }

    public Member update(Long id, MemberDTO dto) {
        Member member = findById(id);
        // Member entity fields that can be updated
        if (dto.getMembershipTier() != null) {
            member.setMembershipTier(MembershipTier.valueOf(dto.getMembershipTier()));
        }
        if (dto.getExpiryDate() != null) {
            member.setExpiryDate(dto.getExpiryDate());
        }
        return memberRepository.save(member);
    }

    public MemberDTO updateProfile(Long id, MemberProfileDTO profile) {
        Member member = findById(id);
        if (profile.getMembershipTier() != null) {
            member.setMembershipTier(MembershipTier.valueOf(profile.getMembershipTier()));
        }
        if (profile.getExpiryDate() != null) {
            member.setExpiryDate(profile.getExpiryDate());
        }
        return toDTO(memberRepository.save(member));
    }

    @Transactional(readOnly = true)
    public List<Loan> findLoans(Long memberId) {
        Member member = findById(memberId);
        return member.getLoans();
    }

    @Transactional(readOnly = true)
    public List<Hold> findHolds(Long memberId) {
        return holdRepository.findByMemberIdWithDetails(memberId);
    }

    @Transactional(readOnly = true)
    public List<Fine> findFines(Long memberId) {
        return fineRepository.findByMemberIdWithDetails(memberId);
    }

    @Transactional(readOnly = true)
    public List<Notification> findNotifications(Long memberId) {
        return notificationRepository.findByMember_IdOrderBySentDateDesc(memberId);
    }

    private MemberDTO toDTO(Member member) {
        MemberDTO dto = new MemberDTO();
        dto.setId(member.getId());
        dto.setMembershipNumber(member.getMembershipNumber());
        dto.setMembershipTier(member.getMembershipTier() != null ? member.getMembershipTier().name() : null);
        dto.setJoinDate(member.getJoinDate());
        dto.setExpiryDate(member.getExpiryDate());
        dto.setFineBalance(member.getFineBalance());
        dto.setActive(member.isActive());
        long activeLoans = member.getLoans() != null
                ? member.getLoans().stream().filter(l -> LoanStatus.ACTIVE.equals(l.getStatus())).count()
                : 0;
        dto.setCurrentLoans((int) activeLoans);
        long activeHolds = member.getHolds() != null
                ? member.getHolds().stream().filter(h -> HoldStatus.PENDING.equals(h.getStatus()) || HoldStatus.READY.equals(h.getStatus())).count()
                : 0;
        dto.setActiveHolds((int) activeHolds);
        return dto;
    }
}
