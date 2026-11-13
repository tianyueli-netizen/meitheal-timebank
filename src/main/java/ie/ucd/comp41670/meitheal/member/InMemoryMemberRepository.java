package ie.ucd.comp41670.meitheal.member;

import ie.ucd.comp41670.meitheal.contract.MemberRepository;
import ie.ucd.comp41670.meitheal.domain.MemberId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryMemberRepository implements MemberRepository {
    private final Map<MemberId, Member> storage = new ConcurrentHashMap<>();

    @Override
    public synchronized void save(Member member) {
        Objects.requireNonNull(member, "Member to save cannot be null");
        // 若存在同邮箱但不同 ID 的记录，拒绝覆盖以防凭空多出账户
        findByEmail(member.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(member.getId())) {
                throw new IllegalStateException("Email already registered to another member: " + member.getEmail());
            }
        });
        storage.put(member.getId(), member);
    }

    @Override
    public Optional<Member> findById(MemberId id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        String normalized = email.trim();
        return storage.values().stream()
                .filter(m -> m.getEmail().equalsIgnoreCase(normalized))
                .findFirst();
    }

    @Override
    public synchronized Member registerMember(String name, String email) {
        if (email != null && findByEmail(email).isPresent()) {
            throw new IllegalStateException("R3 Violation: Member with email already registered: " + email.trim());
        }
        MemberId newId = new MemberId("MEM-" + UUID.randomUUID().toString().substring(0, 8));
        Member member = new Member(newId, name, email);
        storage.put(member.getId(), member);
        return member;
    }
    @Override
    public List<Member> findAll() {
        // R12: 使用 List.copyOf 返回不可变视图
        return List.copyOf(storage.values());
    }

    @Override
    public int count() {
        return storage.size();
    }
}