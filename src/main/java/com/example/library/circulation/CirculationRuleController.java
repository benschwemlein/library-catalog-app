package com.example.library.circulation;

import com.example.library.entity.MembershipTier;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/circulation")
@RequiredArgsConstructor
@Slf4j
public class CirculationRuleController {

    private final CirculationRuleService service;
    private final CirculationRulesEngine engine;

    @GetMapping("/rules")
    public List<CirculationRuleDTO> getAllRules() {
        return service.getAllActiveRules().stream()
                .map(CirculationRuleDTO::from)
                .toList();
    }

    @GetMapping("/rules/{id}")
    public CirculationRuleDTO getRule(@PathVariable Long id) {
        return CirculationRuleDTO.from(service.getRule(id));
    }

    @PostMapping("/rules")
    public CirculationRuleDTO createRule(@RequestBody @Valid CirculationRuleRequest request) {
        return CirculationRuleDTO.from(service.createRule(request));
    }

    @PutMapping("/rules/{id}")
    public CirculationRuleDTO updateRule(@PathVariable Long id,
                                         @RequestBody @Valid CirculationRuleRequest request) {
        return CirculationRuleDTO.from(service.updateRule(id, request));
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        service.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the most applicable circulation rule for the given tier, item type, and optional branch.
     *
     * @param tier      membership tier name (e.g. STANDARD, PREMIUM, STUDENT)
     * @param itemType  item type name (e.g. BOOK, REFERENCE, AUDIOVISUAL)
     * @param branchId  optional branch id; omit to query global rules only
     */
    @GetMapping("/rules/applicable")
    public CirculationRuleDTO getApplicableRule(@RequestParam String tier,
                                                 @RequestParam String itemType,
                                                 @RequestParam(required = false) Long branchId) {
        MembershipTier membershipTier = MembershipTier.valueOf(tier.toUpperCase());
        ItemType itemTypeEnum = ItemType.valueOf(itemType.toUpperCase());

        CirculationRule rule = service.findApplicableRule(membershipTier, itemTypeEnum, branchId);
        return CirculationRuleDTO.from(rule);
    }

    @PostMapping("/rules/{id}/clone")
    public CirculationRuleDTO cloneForBranch(@PathVariable Long id,
                                              @RequestParam Long branchId) {
        return CirculationRuleDTO.from(service.cloneRuleForBranch(id, branchId));
    }
}
