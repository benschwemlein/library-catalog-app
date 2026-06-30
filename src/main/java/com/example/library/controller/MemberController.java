package com.example.library.controller;

import com.example.library.dto.*;
import com.example.library.entity.*;
import com.example.library.entity.MembershipTier;
import com.example.library.repository.MemberRepository;
import com.example.library.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(memberRepository.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Member>> searchMembers(@RequestParam(required = false) String q) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.ok(memberRepository.findAll());
        }
        String lower = q.toLowerCase();
        List<Member> results = memberRepository.findAll().stream()
                .filter(m -> m.getMembershipNumber().toLowerCase().contains(lower)
                        || (m.getUser() != null && (
                            m.getUser().getFirstName().toLowerCase().contains(lower)
                            || m.getUser().getLastName().toLowerCase().contains(lower)
                            || m.getUser().getEmail().toLowerCase().contains(lower))))
                .toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody MemberDTO memberDTO) {
        return ResponseEntity.ok(memberService.update(id, memberDTO));
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<Member> suspendMember(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        Member member = memberService.findById(id);
        member.setActive(false);
        return ResponseEntity.ok(memberRepository.save(member));
    }

    @PostMapping("/{id}/reactivate")
    public ResponseEntity<Member> reactivateMember(@PathVariable Long id) {
        Member member = memberService.findById(id);
        member.setActive(true);
        return ResponseEntity.ok(memberRepository.save(member));
    }

    @PutMapping("/{id}/tier")
    public ResponseEntity<Member> updateMemberTier(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Member member = memberService.findById(id);
        String tier = body.get("tier");
        if (tier != null) {
            member.setMembershipTier(MembershipTier.valueOf(tier.toUpperCase()));
        }
        return ResponseEntity.ok(memberRepository.save(member));
    }

    @GetMapping("/{id}/loans")
    public ResponseEntity<List<Loan>> getMemberLoans(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.findLoans(id));
    }

    @GetMapping("/{id}/holds")
    public ResponseEntity<List<Hold>> getMemberHolds(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.findHolds(id));
    }

    @GetMapping("/{id}/fines")
    public ResponseEntity<List<Fine>> getMemberFines(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.findFines(id));
    }

    @GetMapping("/{id}/notifications")
    public ResponseEntity<List<Notification>> getMemberNotifications(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.findNotifications(id));
    }

    @GetMapping("/membership/{membershipNumber}")
    public ResponseEntity<Member> getByMembershipNumber(@PathVariable String membershipNumber) {
        return ResponseEntity.ok(memberRepository.findByMembershipNumber(membershipNumber)
                .orElseThrow(() -> new RuntimeException("Member not found: " + membershipNumber)));
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<Member> getMemberProfile(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.findById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<Member> getCurrentMember(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        String email = authentication.getName();
        return memberRepository.findAll().stream()
                .filter(m -> m.getUser() != null && email.equals(m.getUser().getEmail()))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<Member> updateMemberProfile(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Member member = memberService.findById(id);
        if (member.getUser() != null) {
            if (updates.containsKey("firstName")) {
                member.getUser().setFirstName((String) updates.get("firstName"));
            }
            if (updates.containsKey("lastName")) {
                member.getUser().setLastName((String) updates.get("lastName"));
            }
        }
        return ResponseEntity.ok(memberRepository.save(member));
    }
}
